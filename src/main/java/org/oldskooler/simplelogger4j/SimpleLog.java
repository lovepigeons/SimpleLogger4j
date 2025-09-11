package org.oldskooler.simplelogger4j;

import org.oldskooler.simplelogger4j.formatters.StringFormatter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleLog<T> {
    private final Formatter formatter;
    private final LogLevel minLogLevel;
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private final AtomicLong droppedMessages = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    private final Class<T> type;

    // ===== Constructors / factories =====
    public static <T> SimpleLog<T> of(Class<T> clazz) throws IOException {
        return fromXml("simplelogger4j.xml", clazz);
    }

    public static <T> SimpleLog<T> fromXml(String xmlPath, Class<T> clazz) throws IOException {
        return fromXml(xmlPath, new StringFormatter(), clazz);
    }

    public static <T> SimpleLog<T> fromXml(String xmlPath, Formatter formatter, Class<T> clazz) throws IOException {
        LogConfig cfg = LogConfig.fromXml(xmlPath);
        return new SimpleLog<>(cfg, formatter, clazz);
    }

    private SimpleLog(LogConfig cfg, Formatter formatter, Class<T> clazz) throws IOException {
        LoggerBus.initIfNeeded(cfg);
        this.formatter = formatter;
        this.minLogLevel = cfg.getMinLevel();
        this.type = clazz;
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public void log(LogLevel level, String message, Throwable throwable) {
        if(disposed.get() || level.getPriority() < minLogLevel.getPriority()) return;
        totalMessages.incrementAndGet();
        String msg = formatter.formatMessage(message);

        String className = this.type.getSimpleName();
        String packageName = this.type.getCanonicalName();

        boolean result = LoggerBus.offer(PrintJob.of(className, packageName, level, msg, throwable), droppedMessages);

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