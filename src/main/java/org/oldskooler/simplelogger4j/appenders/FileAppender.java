package org.oldskooler.simplelogger4j.appenders;

import org.oldskooler.simplelogger4j.LogConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileAppender implements Appender {
    private final LogConfig config;
    private final PrintWriter out;

    public FileAppender(LogConfig config) throws FileNotFoundException {
        this.config = config;

        File logFile = new File(this.config.getFilePath());
        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new FileNotFoundException("Could not create log directory: " + parentDir);
            }
        }

        FileOutputStream fos = new FileOutputStream(this.config.getFilePath(), this.config.isFileAppend());
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(osw, Math.max(1024, this.config.getFileBufferSize()));
        this.out = new PrintWriter(bw, false);
    }

    @Override
    public void println(String line) {
        out.println(line);
    }

    @Override
    public void printStackTrace(Throwable t) {
        t.printStackTrace(out);
    }

    @Override
    public void flush() {
        out.flush();
    }

    @Override
    public void close() {
        try {
            out.flush();
            out.close();
        } catch(Exception ignore) {
            // Ignore close exceptions
        }
    }
}
