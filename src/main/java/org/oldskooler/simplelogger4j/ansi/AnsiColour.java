package org.oldskooler.simplelogger4j.ansi;

import java.util.Locale;

public enum AnsiColour {
    // Reset
    RESET("\u001B[0m"),

    // Regular Colours
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),

    // Bright Colours
    BRIGHT_BLACK("\u001B[90m"),
    BRIGHT_RED("\u001B[91m"),
    BRIGHT_GREEN("\u001B[92m"),
    BRIGHT_YELLOW("\u001B[93m"),
    BRIGHT_BLUE("\u001B[94m"),
    BRIGHT_MAGENTA("\u001B[95m"),
    BRIGHT_CYAN("\u001B[96m"),
    BRIGHT_WHITE("\u001B[97m"),

    // Background Colours
    BG_BLACK("\u001B[40m"),
    BG_RED("\u001B[41m"),
    BG_GREEN("\u001B[42m"),
    BG_YELLOW("\u001B[43m"),
    BG_BLUE("\u001B[44m"),
    BG_MAGENTA("\u001B[45m"),
    BG_CYAN("\u001B[46m"),
    BG_WHITE("\u001B[47m"),

    // Bright Background Colours
    BG_BRIGHT_BLACK("\u001B[100m"),
    BG_BRIGHT_RED("\u001B[101m"),
    BG_BRIGHT_GREEN("\u001B[102m"),
    BG_BRIGHT_YELLOW("\u001B[103m"),
    BG_BRIGHT_BLUE("\u001B[104m"),
    BG_BRIGHT_MAGENTA("\u001B[105m"),
    BG_BRIGHT_CYAN("\u001B[106m"),
    BG_BRIGHT_WHITE("\u001B[107m");

    private final String code;

    AnsiColour(String code) {
        this.code = code;
    }

    public static AnsiColour of(String s) {
        try {
            return AnsiColour.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch(Exception e) {
            return null;
        }
    }

    public String getCode() {
        return code;
    }
}
