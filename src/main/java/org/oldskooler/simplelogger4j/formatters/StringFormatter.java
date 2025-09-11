package org.oldskooler.simplelogger4j.formatters;

import org.oldskooler.simplelogger4j.Formatter;

public class StringFormatter implements Formatter {
    @Override
    public String formatMessage(String message) {
        return message;
    }
}
