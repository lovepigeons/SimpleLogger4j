package org.oldskooler.simplelogger4j.appenders;

import org.oldskooler.simplelogger4j.LogConfig;
import org.oldskooler.simplelogger4j.ansi.AnsiStripper;

public class ConsoleAppender implements Appender {
    private final boolean enableAnsi;
    private final LogConfig config;

    public ConsoleAppender(LogConfig config) {
        this.config = config;
        this.enableAnsi = config.isConsoleColour();
    }

    @Override
    public void println(String line) {
        if(enableAnsi) {
            System.out.println(line);
        } else {
            System.out.println(AnsiStripper.strip(line));
        }
    }

    @Override
    public void printStackTrace(Throwable t) {
        t.printStackTrace(System.out);
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public void close() {
        // No-op for console
    }
}
