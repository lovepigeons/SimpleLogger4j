package org.oldskooler.simplelogger4j;

import java.util.Locale;
import java.util.Objects;

public enum LogLevel {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    SUCCESS(3),
    WARN(4),
    ERROR(5),
    CRITICAL(6);

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
