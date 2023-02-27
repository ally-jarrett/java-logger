package org.rh.example.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class LoggingSchedulerUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSchedulerUtils.class);

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private LoggingExecutor execServicePerformance;

    @Scheduled(initialDelay = 0, fixedRateString = "${java.logger.poll_time:10}", timeUnit = TimeUnit.SECONDS)
    public void pollLoggingProgress() {
        if (execServicePerformance.isRun()) {
            logger.info("Logging TaskScheduler Successfully executed '{}' times",
                    execServicePerformance.getCount());
        }
    }

    /**
     * Default exec time of 60 seconds then the app is killed.
     */
    @Scheduled(initialDelayString = "${java.logger.execution_time:60}", fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void finishLoggingExec() {
        logger.info("Killing off LoggingTaskScheduler ");
        execServicePerformance.setRun(false);

        // Executor Shutdown
        try {
            // Kill Scheduler
            logger.info("attempt to shutdown executor");
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.info("tasks interrupted");

        } finally {
            // Force Shutdown executor if its still faffing about
            if (!threadPoolExecutor.isTerminated()) {
                threadPoolExecutor.shutdownNow();
            }

            logger.info("Total Logs Generated: '{}' in '{}' Seconds",
                    execServicePerformance.getCount(), execServicePerformance.getTimeElapsedInSeconds());
        }
    }
}
