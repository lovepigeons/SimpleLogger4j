package org.oldskooler.simplelogger4j;

import org.oldskooler.simplelogger4j.ansi.AnsiColour;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class PatternEngine {
    private static final String RESET = AnsiColour.RESET.getCode();

    public static String renderPlain(String pattern, DateTimeFormatter tf, PrintJob j) {
        return substitute(pattern, tf, j, null, null);
    }

    public static String renderColoured(String pattern, DateTimeFormatter tf, PrintJob j, Map<LogLevel, AnsiColour> palette) {
        return substitute(pattern, tf, j, palette, AnsiColour.RESET);
    }

    private static String substitute(String pattern, DateTimeFormatter tf, PrintJob j,
                                     Map<LogLevel, AnsiColour> palette, AnsiColour resetIfAny) {
        // Supported tokens: timestamp, level, thread, sequence, message, class, package
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < pattern.length();) {
            char c = pattern.charAt(i);
            if (c == '%' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '{') {
                int end = pattern.indexOf('}', i + 2);
                if (end > 0) {
                    String inside = pattern.substring(i + 2, end); // token[:SPEC]
                    String token, spec = null;
                    int colon = inside.indexOf(':');
                    if (colon > 0) {
                        token = inside.substring(0, colon);
                        spec = inside.substring(colon + 1);
                    } else {
                        token = inside;
                    }

                    String value;
                    boolean isTimestamp = false;

                    switch (token) {
                        case "timestamp":
                            value = j.timestamp.format(tf);
                            isTimestamp = true;
                            break;
                        case "level":
                            value = j.level.name();
                            break;
                        case "thread":
                            value = j.thread;
                            break;
                        case "sequence":
                            value = Long.toString(j.sequence);
                            break;
                        case "message":
                            value = j.message;
                            break;
                        case "name":
                            value = j.name;
                            break;
                        default:
                            value = "%{" + inside + "}"; // passthrough
                            break;
                    }

                    if (spec != null) {
                        out.append(applySpec(value, spec, j, isTimestamp, tf, palette));
                    } else {
                        out.append(value);
                    }

                    i = end + 1;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        // Only append a reset if we actually coloured something (palette provided and ESC seen)
        if (palette != null && out.indexOf("\u001B[") >= 0 && resetIfAny != null) {
            out.append(resetIfAny.getCode());
        }
        return out.toString();
    }

    /**
     * Applies a chain of spec operations separated by '|'.
     * Recognized operations (case-insensitive):
     *  - padLeft(width[, 'fill'])
     *  - padRight(width[, 'fill'])
     *  - substring(start[, end])        // end is exclusive; negatives are clamped to 0
     *  - datetime(pattern)              // only affects %{timestamp}; falls back to tf if invalid
     *  - LEVEL                          // colourize with palette by log level (only if palette != null)
     *  - <ANSI_NAME>                    // e.g. RED, BRIGHT_BLUE (only if palette != null)
     */
    private static String applySpec(String value, String specChain,
                                    PrintJob j, boolean isTimestamp,
                                    DateTimeFormatter tf, Map<LogLevel, AnsiColour> palette) {
        String result = value;
        String[] parts = specChain.split("\\|"); // allow chaining

        // Defer colour application: remember the last selected colour (if any)
        AnsiColour deferredColour = null;

        for (String rawPart : parts) {
            if (rawPart == null) continue;
            String part = rawPart.trim();
            String upper = part.toUpperCase(Locale.ROOT);

            // --- padding ---
            if (upper.startsWith("PADLEFT")) {
                int l = part.indexOf('(');
                int r = part.lastIndexOf(')');
                if (l >= 0 && r > l) {
                    String args = part.substring(l + 1, r);
                    Object[] parsed = parseWidthAndFill(args);
                    int width = (Integer) parsed[0];
                    char fill = (Character) parsed[1];
                    result = padLeft(result, width, fill);
                    continue;
                }
            } else if (upper.startsWith("PADRIGHT")) {
                int l = part.indexOf('(');
                int r = part.lastIndexOf(')');
                if (l >= 0 && r > l) {
                    String args = part.substring(l + 1, r);
                    Object[] parsed = parseWidthAndFill(args);
                    int width = (Integer) parsed[0];
                    char fill = (Character) parsed[1];
                    result = padRight(result, width, fill);
                    continue;
                }
            }
            // --- substring ---
            else if (upper.startsWith("SUBSTRING")) {
                int l = part.indexOf('(');
                int r = part.lastIndexOf(')');
                if (l >= 0 && r > l) {
                    String args = part.substring(l + 1, r).trim();
                    int start = 0;
                    Integer end = null;
                    if (!args.isEmpty()) {
                        String[] nums = args.split(",");
                        try {
                            if (nums.length >= 1) start = Integer.parseInt(nums[0].trim());
                            if (nums.length >= 2) end = Integer.parseInt(nums[1].trim());
                        } catch (NumberFormatException ignored) {
                            // keep current result
                        }
                    }
                    result = substringSafe(result, start, end);
                    continue;
                }
            }
            // --- datetime (for timestamp only) ---
            else if (upper.startsWith("DATETIME") && isTimestamp) {
                int l = part.indexOf('(');
                int r = part.lastIndexOf(')');
                if (l >= 0 && r > l) {
                    String fmt = part.substring(l + 1, r).trim();
                    if ((fmt.startsWith("'") && fmt.endsWith("'")) || (fmt.startsWith("\"") && fmt.endsWith("\""))) {
                        fmt = fmt.substring(1, fmt.length() - 1);
                    }
                    try {
                        DateTimeFormatter customTf = DateTimeFormatter.ofPattern(fmt);
                        result = j.timestamp.format(customTf);
                        continue;
                    } catch (IllegalArgumentException ignored) {
                        // keep result as-is
                    }
                }
            }

            // --- colour specs: don't wrap now; just remember which colour to apply ---
            if (palette != null) {
                if ("LEVEL".equals(upper)) {
                    deferredColour = palette.get(j.level); // may be null
                    continue;
                }
                AnsiColour a = AnsiColour.of(upper);
                if (a != null) {
                    deferredColour = a;
                    continue;
                }
            }
            // Unknown spec: ignore
        }

        // Apply the single, final colour (if any) exactly once
        if (deferredColour != null) {
            result = deferredColour.getCode() + result + AnsiColour.RESET.getCode();
        }
        return result;
    }

    // Parses args like: "10", "10,'0'", "10,  *", "10,\"·\""
    private static Object[] parseWidthAndFill(String args) {
        int width = 0;
        char fill = ' ';
        if (args != null && !args.isEmpty()) {
            String[] parts = args.split(",");
            try {
                if (parts.length >= 1) width = Integer.parseInt(parts[0].trim());
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
            if (parts.length >= 2) {
                String f = parts[1].trim();
                if ((f.startsWith("'") && f.endsWith("'") && f.length() >= 3)
                        || (f.startsWith("\"") && f.endsWith("\"") && f.length() >= 3)) {
                    fill = f.charAt(1);
                } else if (!f.isEmpty()) {
                    fill = f.charAt(0);
                }
            }
        }
        return new Object[]{width, fill};
    }

    // --- helpers ---

    private static String padLeft(String s, int width, char fill) {
        if (s == null) s = "";
        int len = s.length();
        if (width <= len) return s;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0, n = width - len; i < n; i++) sb.append(fill);
        sb.append(s);
        return sb.toString();
    }

    private static String padRight(String s, int width, char fill) {
        if (s == null) s = "";
        int len = s.length();
        if (width <= len) return s;
        StringBuilder sb = new StringBuilder(width);
        sb.append(s);
        for (int i = 0, n = width - len; i < n; i++) sb.append(fill);
        return sb.toString();
    }

    private static String substringSafe(String s, int start, Integer endExclusiveOrNull) {
        if (s == null) return "";
        int len = s.length();
        if (start < 0) start = 0;
        if (start > len) start = len;
        int end = (endExclusiveOrNull == null) ? len : endExclusiveOrNull;
        if (end < start) end = start;
        if (end > len) end = len;
        return s.substring(start, end);
    }
}
