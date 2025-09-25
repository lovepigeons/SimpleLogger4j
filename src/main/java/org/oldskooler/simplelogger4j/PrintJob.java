package org.oldskooler.simplelogger4j;

import java.time.LocalDateTime;

public class PrintJob {
    final long sequence;
    final LogLevel level;
    final String message;
    final String thread;
    final LocalDateTime timestamp;
    final Throwable throwable;
    final boolean isFlush;
    final String name;

    public PrintJob(String name, long seq, LogLevel lvl, String msg,
                    String thread, LocalDateTime ts, Throwable th, boolean flush) {
        this.name = name;
        this.sequence = seq;
        this.level = lvl;
        this.message = msg;
        this.thread = thread;
        this.timestamp = ts;
        this.throwable = th;
        this.isFlush = flush;
    }

    static PrintJob of(String name, LogLevel lvl, String msg, Throwable th) {
        long s = LoggerBus.nextSeq();
        return new PrintJob(name, s, lvl, msg, Thread.currentThread().getName(),
                LocalDateTime.now(), th, false);
    }

    static PrintJob flush() {
        return new PrintJob(null, -1, LogLevel.INFO, null, "flush", LocalDateTime.now(), null, true);
    }
}
