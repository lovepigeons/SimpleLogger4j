package org.oldskooler.simplelogger4j.test;

import org.oldskooler.simplelogger4j.LogLevel;
import org.oldskooler.simplelogger4j.PatternEngine;
import org.oldskooler.simplelogger4j.PrintJob;
import org.oldskooler.simplelogger4j.ansi.AnsiColour;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;

public class Colours {
    public static void main(String[] args) {
        // Fake PrintJob
        PrintJob j = new PrintJob(
                "com.example.Demo",
                "com.example",
                42L,
                LogLevel.INFO,
                "Hello, World ttttttttttttttttttttttttttttttttt hfghtghfghhtr4 4 324324322132122222222!",
                Thread.currentThread().getName(),
                LocalDateTime.now(),
                null,
                false
        );

        // Example palette
        EnumMap<LogLevel, AnsiColour> palette = new EnumMap<>(LogLevel.class);
        palette.put(LogLevel.INFO, AnsiColour.GREEN);
        palette.put(LogLevel.ERROR, AnsiColour.RED);

        // --- Examples ---
        // Default engine formatter
        DateTimeFormatter tf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // 1) SIMPLE, NON-COLOURED (uses engine's default formatter)
        String p1 = "%{timestamp} %{level} %{message}";
        System.out.println(PatternEngine.renderPlain(p1, tf, j));

        // 2) NON-COLOURED with a datetime override (dt) + tiny chain
        //    dt(...) overrides just this token's format; padRight aligns the ts column.
        String p2 = "%{timestamp:datetime('yyyy-MM-dd HH:mm:ss')|padRight(20)} %{level} - %{message}";
        System.out.println(PatternEngine.renderPlain(p2, tf, j));

        // 3) NON-COLOURED with locale, substring clip + padding
        String p3 = "%{timestamp:datetime('EEEE d MMM yyyy HH:mm', 'fr-FR')|padRight(28)} "
                + "%{level:padRight(5)} %{message:substring(0,30)|padRight(30,' ')}";
        System.out.println(PatternEngine.renderPlain(p3, tf, j));

        // ─────────────────────────────────────────────────────────────
        // 4) COLOURED: colour the LEVEL token based on palette
        //    Only %{level:LEVEL} is coloured; everything else stays default.
        String c1 = "%{timestamp:datetime('HH:mm:ss')} %{level:LEVEL} %{message}";
        System.out.println(PatternEngine.renderColoured(c1, tf, j, palette));

        // 5) COLOURED: mix LEVEL + fixed colour (BRIGHT_BLUE for thread)
        //    Note: LEVEL applies palette colour for that token; BRIGHT_BLUE is a named ANSI colour.
        String c2 = "%{timestamp:datetime('HH:mm:ss')|padRight(12)} "
                + "[%{thread:BRIGHT_BLUE}] "
                + "%{level:LEVEL|padRight(5)} "
                + "%{message}";
        System.out.println(PatternEngine.renderColoured(c2, tf, j, palette));

        // 6) COLOURED + CHAINED SPECS on multiple tokens
        //    - timestamp: compact time, then right-pad to column
        //    - level: palette colour via LEVEL, then padRight for alignment
        //    - message: clip to 40 chars, pad to fixed width for neat columns
        String c3 = "%{timestamp:datetime('HH:mm:ss')|padRight(12)} "
                + "%{level:LEVEL|padRight(5)} "
                + "%{message:substring(0,40)|padRight(40,' ')} "
                + "(%{class:BRIGHT_BLACK})";
        System.out.println(PatternEngine.renderColoured(c3, tf, j, palette));

        // 7) ADVANCED: columnar layout + locale date + chain everywhere
        //    - date in ISO date only (override), pad to 12
        //    - time in 'HH:mm:ss', pad to 10
        //    - level coloured by palette and fixed width
        //    - package dimmed, class bright, message clipped
        String c4 = "%{timestamp:datetime('yyyy-MM-dd')|padRight(12)} "
                + "%{timestamp:datetime('HH:mm:ss')|padRight(10)} "
                + "%{level:LEVEL|padRight(5)} "
                + "%{package:DIM_WHITE}"
                + ".%{class:BRIGHT_WHITE} - "
                + "%{message:substring(0,60)|padRight(60,' ')} "
                + "#%{sequence}";
        System.out.println(PatternEngine.renderColoured(c4, tf, j, palette));

        // 8) MIXED COLOURING on the same line (fixed colour vs palette)
        //    - thread in CYAN, level uses LEVEL (palette), message left plain
        String c5 = "[%{thread:CYAN}] %{level:LEVEL} :: %{message}";
        System.out.println(PatternEngine.renderColoured(c5, tf, j, palette));
    }
}
