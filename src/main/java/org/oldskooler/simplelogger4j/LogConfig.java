package org.oldskooler.simplelogger4j;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;

import org.oldskooler.simplelogger4j.ansi.AnsiColour;
import org.w3c.dom.*;

public class LogConfig {
    private LogLevel minLevel = LogLevel.INFO;
    private int queueSize = 8192;
    private String timeFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    private String pattern = "[%{timestamp:BRIGHT_BLACK}] [%{level:LEVEL}] [%{thread:CYAN}] %{message}";
    private int flushEvery = 10;
    private boolean consoleEnabled = true;
    private boolean fileEnabled = true;
    private String filePath = "logs/app-%d{yyyy-MM-dd}.log";
    private int fileBufferSize = 8192;
    private boolean fileAppend = true;
    /** Enable ANSI on console (auto-stripped for file). */
    private boolean consoleColour = true;
    /** Level→colour used when pattern asks for LEVEL-based colour (default: DEBUG gray, INFO none, WARN yellow, ERROR red). */
    public Map<LogLevel, AnsiColour> levelPalette = defaultPalette();

    public static LogConfig fromXml(String xmlPath) {
        LogConfig cfg = new LogConfig();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlPath));
            doc.getDocumentElement().normalize();
            cfg.minLevel   = getEnum(doc,"minLevel",cfg.minLevel);
            cfg.queueSize  = getInt(doc,"queueSize",cfg.queueSize);
            cfg.timeFormat = getText(doc,"timeFormat",cfg.timeFormat);
            cfg.pattern    = getText(doc,"pattern",cfg.pattern);
            cfg.flushEvery = getInt(doc,"flushEvery",cfg.flushEvery);
            String consoleColourTxt = getText(doc,"consoleColour", Boolean.toString(cfg.consoleColour));
            if(consoleColourTxt != null) cfg.consoleColour = Boolean.parseBoolean(consoleColourTxt);

            Node appenders = doc.getElementsByTagName("appenders").item(0);
            if(appenders != null) {
                NodeList list = appenders.getChildNodes();
                for(int i = 0; i < list.getLength(); i++) {
                    Node n = list.item(i);
                    if(n.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element e = (Element)n;
                    switch(e.getTagName()) {
                        case "console": {
                            cfg.consoleEnabled = getBoolAttr(e, "enabled", true);
                            break;
                        }
                        case "file": {
                            cfg.fileEnabled = getBoolAttr(e, "enabled", true);
                            if (e.hasAttribute("path")) cfg.filePath = e.getAttribute("path");
                            if (e.hasAttribute("bufferSize"))
                                cfg.fileBufferSize = Integer.parseInt(e.getAttribute("bufferSize"));
                            if (e.hasAttribute("append"))
                                cfg.fileAppend = Boolean.parseBoolean(e.getAttribute("append"));
                            break;
                        }
                    }
                }
            }
            // Optional: levelPalette overrides
            Node lp = doc.getElementsByTagName("levelPalette").item(0);
            if(lp instanceof Element) {
                Element el = (Element) lp;
                Map<LogLevel, AnsiColour> p = new EnumMap<>(LogLevel.class);
                for(LogLevel lv: LogLevel.values()) {
                    String txt = el.getAttribute(lv.name().toLowerCase(Locale.ROOT));
                    if(!txt.isEmpty()) {
                        AnsiColour a = AnsiColour.of(txt);
                        if(a != null) p.put(lv, a);
                    }
                }
                if(!p.isEmpty()) {
                    cfg.levelPalette.putAll(p);
                }
            }
        } catch(Exception e) {
            System.err.println("Logger XML load failed; using defaults. Reason: " + e.getMessage());
        }
        return cfg;
    }

    private static Map<LogLevel, AnsiColour> defaultPalette() {
        Map<LogLevel, AnsiColour> m = new EnumMap<>(LogLevel.class);
        m.put(LogLevel.DEBUG, AnsiColour.BLUE);
        m.put(LogLevel.INFO, null); // no colour
        m.put(LogLevel.WARN, AnsiColour.BRIGHT_YELLOW);
        m.put(LogLevel.ERROR, AnsiColour.RED);
        m.put(LogLevel.SUCCESS, AnsiColour.GREEN);
        m.put(LogLevel.CRITICAL, AnsiColour.BG_BRIGHT_RED);
        return m;
    }

    private static String getText(Document d, String tag, String def) {
        NodeList nl = d.getElementsByTagName(tag);
        return (nl.getLength() == 0) ? def : nl.item(0).getTextContent().trim();
    }

    private static int getInt(Document d, String tag, int def) {
        try {
            return Integer.parseInt(getText(d, tag, Integer.toString(def)));
        } catch(Exception e) {
            return def;
        }
    }

    private static LogLevel getEnum(Document d, String tag, LogLevel def) {
        try {
            return LogLevel.parse(getText(d, tag, def.name()));
        } catch(Exception e) {
            return def;
        }
    }

    private static boolean getBoolAttr(Element e, String name, boolean def) {
        return e.hasAttribute(name) ? Boolean.parseBoolean(e.getAttribute(name)) : def;
    }

    public LogLevel getMinLevel() {
        return minLevel;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getPattern() {
        return pattern;
    }

    public int getFlushEvery() {
        return flushEvery;
    }

    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public boolean isFileEnabled() {
        return fileEnabled;
    }

    public String getFilePath() {
        if (filePath.contains("%d{")) {
            int start = filePath.indexOf("%d{");
            int end = filePath.indexOf("}", start);
            if (end > start) {
                String pattern = filePath.substring(start + 3, end);
                String formatted = new SimpleDateFormat(pattern).format(new Date());
                return filePath.substring(0, start) + formatted + filePath.substring(end + 1);
            }
        }
        return filePath;
    }

    public int getFileBufferSize() {
        return fileBufferSize;
    }

    public boolean isFileAppend() {
        return fileAppend;
    }

    public boolean isConsoleColour() {
        return consoleColour;
    }

    public Map<LogLevel, AnsiColour> getLevelPalette() {
        return levelPalette;
    }
}