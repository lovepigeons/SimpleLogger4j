package org.oldskooler.simplelogger4j;

import org.oldskooler.simplelogger4j.ansi.AnsiStripper;
import org.oldskooler.simplelogger4j.appenders.Appender;
import org.oldskooler.simplelogger4j.appenders.ConsoleAppender;
import org.oldskooler.simplelogger4j.appenders.FileAppender;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class LoggerBus {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static BlockingQueue<PrintJob> queue;
    private static ExecutorService writer;
    private static final AtomicBoolean shutdown = new AtomicBoolean(false);
    private static final AtomicLong globalSeq = new AtomicLong(0);
    private static int flushEvery;
    private static List<Appender> appenders;
    private static LogConfig cfg;
    private static DateTimeFormatter timeFmt;

    static void initIfNeeded(LogConfig c) throws IOException {
        if(INITIALIZED.compareAndSet(false, true)) {
            cfg = c;
            queue = new ArrayBlockingQueue<>(c.getQueueSize());
            timeFmt = DateTimeFormatter.ofPattern(c.getTimeFormat());
            flushEvery = Math.max(1, c.getFlushEvery());
            appenders = new ArrayList<>();

            if(c.isConsoleEnabled()) appenders.add(new ConsoleAppender(c));
            if(c.isFileEnabled()) appenders.add(new FileAppender(c));
            if(appenders.isEmpty()) appenders.add(new ConsoleAppender(c));

            writer = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Logger-Writer");
                t.setDaemon(true);
                return t;
            });
            writer.submit(LoggerBus::loop);
            Runtime.getRuntime().addShutdownHook(new Thread(LoggerBus::shutdown));
        }
    }

    static long nextSeq() {
        return globalSeq.getAndIncrement();
    }

    static boolean offer(PrintJob job, AtomicLong dropped) {
        if (!queue.offer(job)) {
            dropped.incrementAndGet();
            return false;
        }

        return true;
    }

    static void flushAsync() {
        try {
            queue.put(PrintJob.flush());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void shutdown() {
        if(shutdown.compareAndSet(false, true)) {
            try {
                writer.shutdown();
                writer.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                writer.shutdownNow();
            } finally {
                for(Appender a: appenders) {
                    try {
                        a.flush();
                        a.close();
                    } catch(Exception ignore) {
                        // Ignore cleanup exceptions
                    }
                }
            }
        }
    }

    private static void loop() {
        long sinceFlush = 0;
        while(!shutdown.get() || !queue.isEmpty()) {
            try {
                PrintJob j = queue.poll(100, TimeUnit.MILLISECONDS);
                if(j == null) continue;

                if(j.isFlush) {
                    appenders.forEach(Appender::flush);
                    sinceFlush = 0;
                    continue;
                }

                String plain = PatternEngine.renderPlain(cfg.getPattern(), timeFmt, j);
                for(Appender a: appenders) {
                    if(a instanceof ConsoleAppender) {
                        ConsoleAppender ca = (ConsoleAppender) a;
                        ca.println(PatternEngine.renderColoured(cfg.getPattern(), timeFmt, j, cfg.levelPalette));
                    } else {
                        a.println(AnsiStripper.strip(plain));
                    }
                    if(j.throwable != null) a.printStackTrace(j.throwable);
                }

                if(++sinceFlush >= flushEvery || (j.sequence % flushEvery == 0)) {
                    appenders.forEach(Appender::flush);
                    sinceFlush = 0;
                }
            } catch(InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch(Exception ex) {
                System.err.println("Logger error: " + ex.getMessage());
            }
        }
        appenders.forEach(a -> {
            try {
                a.flush();
                a.close();
            } catch(Exception ignore) {
                // Ignore cleanup exceptions
            }
        });
    }
}