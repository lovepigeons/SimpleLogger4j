package org.oldskooler.simplelogger4j;

import org.oldskooler.simplelogger4j.formatters.StringFormatter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleLog {
    private final Formatter formatter;
    private final LogLevel minLogLevel;
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private final AtomicLong droppedMessages = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final String name;

    // ===== Constructors / factories =====
    public static <T> SimpleLog of(Class<T> clazz) {
        return fromXml("simplelogger4j.xml", clazz.getCanonicalName());
    }

    public static <T> SimpleLog of(String name)  {
        return fromXml("simplelogger4j.xml", name);
    }

    public static <T> SimpleLog fromXml(String xmlPath, String name) {
        return fromXml(xmlPath, new StringFormatter(), name);
    }

    public static <T> SimpleLog fromXml(String xmlPath, Formatter formatter, String name) {
        LogConfig cfg = LogConfig.fromXml(xmlPath);
        return new SimpleLog(cfg, formatter, name);
    }

    private SimpleLog(LogConfig cfg, Formatter formatter, String name) {
        try {
            LoggerBus.initIfNeeded(cfg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.formatter = formatter;
        this.minLogLevel = cfg.getMinLevel();
        this.name = name;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if(disposed.get() || level.getPriority() < minLogLevel.getPriority()) return;
        totalMessages.incrementAndGet();

        String msg = formatter.formatMessage(message);

        boolean result = LoggerBus.offer(PrintJob.of(name, level, msg, throwable), droppedMessages);

    }

    // String conveniences
    public void debug(String msg) { log(LogLevel.DEBUG, msg); }
    public void info(String msg) { log(LogLevel.INFO, msg); }
    public void warn(String msg) { log(LogLevel.WARN, msg); }
    public void success(String msg) { log(LogLevel.SUCCESS, msg); }
    public void warn(String msg, Throwable t) { log(LogLevel.WARN, msg, t); }
    public void error(String msg) { log(LogLevel.ERROR, msg); }
    public void error(String msg, Throwable t) { log(LogLevel.ERROR, msg, t); }
    public void critical(String msg) { log(LogLevel.CRITICAL, msg); }
    public void critical(String msg, Throwable t) { log(LogLevel.CRITICAL, msg, t); }

    public void debug(String msg, Object... args) {
        log(LogLevel.DEBUG, format(msg, args));
    }

    public void info(String msg, Object... args) {
        log(LogLevel.INFO, format(msg, args));
    }

    public void warn(String msg, Object... args) {
        log(LogLevel.WARN, format(msg, args));
    }

    public void success(String msg, Object... args) {
        log(LogLevel.SUCCESS, format(msg, args));
    }

    public void warn(String msg, Throwable t, Object... args) {
        log(LogLevel.WARN, format(msg, args), t);
    }

    public void error(String msg, Object... args) {
        log(LogLevel.ERROR, format(msg, args));
    }

    public void error(String msg, Throwable t, Object... args) {
        log(LogLevel.ERROR, format(msg, args), t);
    }

    public void critical(String msg, Object... args) {
        log(LogLevel.CRITICAL, format(msg, args));
    }

    public void critical(String msg, Throwable t, Object... args) {
        log(LogLevel.CRITICAL, format(msg, args), t);
    }

    /**
     * Simple formatter that replaces {} with arguments in order.
     */
    private String format(String template, Object... args) {
        if (template == null || args == null || args.length == 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int cur = 0;
        int brace;
        while ((brace = template.indexOf("{}", cur)) != -1 && argIndex < args.length) {
            sb.append(template, cur, brace);

            Object argument = args[argIndex++];
            sb.append(argument == null ? "null" : argument.toString());

            cur = brace + 2;
        }
        sb.append(template.substring(cur));
        return sb.toString();
    }

    /** enqueue a flush sentinel (blocks briefly if queue is full) */
    public void flush() {
        try {
            LoggerBus.flushAsync();
        } catch (Exception ignore) {
            // Ignore flush exceptions
        }
    }

    /** no-op for additional instances; the global writer stops once */
    public void shutdown() {
        if(disposed.compareAndSet(false, true)) {
            /* instance disposed; do not stop bus here */
        }
    }

    // ===== Metrics =====
    public long getDroppedMessageCount() {
        return droppedMessages.get();
    }

    public long getTotalMessageCount() {
        return totalMessages.get();
    }

    public double getDropRate() {
        long total = totalMessages.get();
        return total > 0 ? (double)droppedMessages.get() / total * 100.0 : 0.0;
    }
}
