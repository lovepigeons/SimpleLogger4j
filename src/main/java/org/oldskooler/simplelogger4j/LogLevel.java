package org.oldskooler.simplelogger4j;

import java.util.Locale;
import java.util.Objects;

public enum LogLevel {
    DEBUG(0), INFO(1), WARN(2), ERROR(3), SUCCESS(4), CRITICAL(5);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static LogLevel parse(String s) {
        return LogLevel.valueOf(Objects.requireNonNull(s).trim().toUpperCase(Locale.ROOT));
    }
}