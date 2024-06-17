package com.musigma.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log {
    private static final Logger LOGGER = getLogger(Log.class);
    private static final LogManager logManager = LogManager.getLogManager();

    static{
        try {
            logManager.readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
        } catch (IOException e) {
            LOGGER.severe("Cannot read configuration file");
        }
    }

    public static <T> Logger getLogger(Class<T> klass) {
        return Logger.getLogger(klass.getName());
    }
}
