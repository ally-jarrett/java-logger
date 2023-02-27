package org.rh.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Basic single threaded scheduler for polling and shutdown tasks
 * Note: Separate Executor so that tasks dont get lost in the LoggingExecutor task queue
 */
@Configuration
public class SchedulerConfiguration implements SchedulingConfigurer {

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }
}