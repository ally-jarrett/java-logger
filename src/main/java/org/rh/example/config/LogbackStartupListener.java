package org.rh.example.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Append Container Name to Log File if `JAVA_LOGGER_CONTAINER_NAME` has been set
 */
@Configuration
public class LogbackStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogbackStartupListener.class);

    private static final String DEFAULT_LOG_FILE = "java-logger";

    private boolean started = false;

    @Override
    public void start() {

        if (started) return;

        logger.info("Setting log dir ");
        String logFileName = System.getenv("JAVA_LOGGER_CONTAINER_NAME");

        logFileName = (logFileName != null && logFileName.length() > 0) ?
                logFileName + '_' + DEFAULT_LOG_FILE :
                DEFAULT_LOG_FILE;

        Context context = getContext();
        context.putProperty("JAVA_LOGGER_LOG_NAME", logFileName);

        started = true;
    }

    @Override
    public boolean isResetResistant() {
        return false;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {

    }

    @Override
    public void onReset(LoggerContext loggerContext) {

    }

    @Override
    public void onStop(LoggerContext loggerContext) {

    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
