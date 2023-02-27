package org.rh.example.config;

import org.rh.example.executor.LoggingExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


@Configuration
@ComponentScan(
        basePackages = "org.rh.example.executor",
        basePackageClasses = {LoggingExecutor.class})
public class ThreadPoolTaskExecutorConfig {

    @Value("${java.logger.executor.thread_pool_size:1}")
    private int executorThreadCount;

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTaskExecutorConfig.class);

    //NOTE: Use same value for core & max pool sizes to keep things simple for now.
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        logger.debug("Bootstrapping ThreadpoolExecutor with {} Threads & a bounded task queue of 10k", executorThreadCount);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(executorThreadCount, executorThreadCount, 0, MILLISECONDS,
                new ArrayBlockingQueue<>(10000),
                new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}