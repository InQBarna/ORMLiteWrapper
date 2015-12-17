package com.inqbarna.inqorm;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 2/10/15
 */
public class DBLogger {
    public interface DBLoggerClient {
        void debug(String msg, Throwable throwable);

        void error(String msg, Throwable throwable);

        void warn(String msg, Throwable throwable);
    }

    private static DBLoggerClient loggerClient;

    public static final void setLoggerClient(DBLoggerClient client) {
        loggerClient = client;
    }


    public static void debug(String msg) {
        debug(msg, null);
    }

    public static void debug(String msg, Throwable throwable) {
        if (null != loggerClient) {
            loggerClient.debug(msg, throwable);
        }
    }

    public static final void error(String msg) {
        error(msg, null);
    }

    public static void error(String msg, Throwable throwable) {
        if (null != loggerClient) {
            loggerClient.error(msg, throwable);
        }
    }


    public static void warn(String msg) {
        warn(msg, null);
    }
    public static void warn(String msg, Throwable throwable) {
        if (null != loggerClient) {
            loggerClient.warn(msg, throwable);
        }
    }
}
