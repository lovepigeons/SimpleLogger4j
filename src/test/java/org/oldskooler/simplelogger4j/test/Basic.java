package org.oldskooler.simplelogger4j.test;

import org.oldskooler.simplelogger4j.SimpleLog;

public class Basic {
    public static void main(String[] args) throws Exception {
        SimpleLog logger = SimpleLog.of(Basic.class);

        logger.info("Test arguments: {} {}{}", 123, 123.456, "testing");

        logger.info("This is logged at info level");
        logger.warn("This is logged at warn level");
        logger.success("This is logged at success level");
        logger.debug("This is logged at debug level");
        logger.error("This is logged at error level");
        logger.critical("This is logged at critical level");

        try {
            throw new RuntimeException("Forced error!");
        }
        catch (Exception ex) {
            logger.error("error", ex);
        }

        try {
            throw new RuntimeException("Forced critical error!");
        }
        catch (Exception ex) {
            logger.critical("error", ex);
        }
    }
}
