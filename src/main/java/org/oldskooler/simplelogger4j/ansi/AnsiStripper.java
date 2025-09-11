package org.oldskooler.simplelogger4j.ansi;

public class AnsiStripper {
    private static final String ANSI_REGEX = "\\u001B\\[[;\\d]*m";

    public static String strip(String s) {
        return s.replaceAll(ANSI_REGEX, "");
    }
}