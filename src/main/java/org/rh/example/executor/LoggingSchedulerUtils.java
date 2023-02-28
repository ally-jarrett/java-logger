package org.rh.example.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class LoggingSchedulerUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSchedulerUtils.class);

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private LoggingExecutor execServicePerformance;

    @Autowired
    ScheduledExecutorService scheduledExecutorService;

    public void scheduleLoggingGeneration(int runtime, int polltime, int messageLength) {

        logger.info("Starting log generator to run for : '{}' Seconds", runtime);

        // Kickoff Log Generator
        execServicePerformance.setRun(true);
        execServicePerformance.doSomeLogging(messageLength);

        // Schedule shutdown task
        scheduledExecutorService.schedule(this.killLogGeneratorTask, runtime, TimeUnit.SECONDS);

        // Schedule poll task stats
        scheduledExecutorService.scheduleAtFixedRate(this.pollLogGeneratorStats, 0, polltime, TimeUnit.SECONDS);
    }

    Runnable pollLogGeneratorStats = new Runnable() {
        @Override
        public void run() {
            if (execServicePerformance.isRun()) {
                logger.info("Logging TaskScheduler Successfully executed '{}' times",
                        execServicePerformance.getCount());
            }
        }
    };

    Runnable killLogGeneratorTask = new Runnable() {
        @Override
        public void run() {

            logger.info("Killing off LoggingTaskScheduler ");
            execServicePerformance.setRun(false);

            try {
                // Kill Scheduler
                logger.info("Attempting to shutdown log generator executor...");
                threadPoolExecutor.shutdown();
                threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.info("tasks interrupted");

            } finally {
                // Force Shutdown executor if its still faffing about
                if (!threadPoolExecutor.isTerminated()) {
                    threadPoolExecutor.shutdownNow();
                }

                logger.info("Log generator executor has been killed...");
                logger.info("Total Logs Generated: '{}' in '{}' Seconds",
                        execServicePerformance.getCount(), execServicePerformance.getTimeElapsedInSeconds());
            }
        }
    };
}
