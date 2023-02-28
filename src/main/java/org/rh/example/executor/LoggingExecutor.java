package org.rh.example.executor;

import org.rh.example.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class LoggingExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingExecutor.class);

    private int count = 0;
    private boolean run;

    private long timeElapsedInSeconds;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private LoggerUtils loggerUtils;

    /**
     * Kick off Logging Task Executor after App has started
     * Populate a Bounded Queue of 10K tasks, these tasks will
     * execute as fast as the logging config will allow..
     */
    @Async
    public void doSomeLogging(int logMessageLength) {

        Instant start = Instant.now();

        while (this.isRun()) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    String logMessage = "Runnable Task with messageId '" + count++ + "' on thread '"
                            + Thread.currentThread().getName() + "'";

                    // Add additional Char padding if specified via -Djava.logger.log.char_count
                    logMessage = loggerUtils.padLoggingStringToCharCount(logMessage, logMessageLength);
                    logger.debug(logMessage);
                }
            });
        }

        Instant stop = Instant.now();
        this.setTimeElapsedInSeconds(Duration.between(start, stop).toSeconds());
        logger.info("Logging Generator ran for : {} Seconds", this.getTimeElapsedInSeconds());
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTimeElapsedInSeconds() {
        return timeElapsedInSeconds;
    }

    public void setTimeElapsedInSeconds(long timeElapsedInSeconds) {
        this.timeElapsedInSeconds = timeElapsedInSeconds;
    }

}

