package org.oldskooler.simplelogger4j.appenders;

import java.io.Closeable;

public interface Appender extends Closeable {
    void println(String line);
    void printStackTrace(Throwable t);
    void flush();
    @Override
    void close();
}

