# Java Logger

This is a very simple Java Spring app to generate logging load to help flush out Storage IO issues. 

* The App consists of 2 parts: 
  - LoggingExecutor 
  - LoggingSchedulerUtils 

## LoggingExecutor

This performs the log load generation, it uses a simple single threaded TaskExecutor with a bounded queue size of 10k (to keep memory usage low). 
The app will keep the bounded queue full with logging tasks, whilst the single threaded executor will execute simple `log.debug(<TEXT>)` statements. 

This allows you to configure LogBack in various ways to test its speed and performance. 

---
**NOTE:**
LogBack is currently configured with a simple synchronous blocking mechanism meaning the executor thread will be blocked for as long as it takes to persist the debug log to storage. 
---

To increase performance, there are 2 things that can be done: 
1. Increase the number of thread in the executor, so that multiple threads are writing to disk at any one time
2. Utilise LogBacks AsyncAppenders to unblock the thread and place the logging job into an internal cache (queue) within the LogBack libs. 

---
**NOTE:**
Point 2 will introduce more intricacies such as having to manage filling up for the LogBack Cache if the storage is slow! 
---

The current Synchronous LogBack implementation will give a concise measure of the Storage IO without 'polluting' the logging architecture. 

## LoggingSchedulerUtils

There shouldn't be too much attention paid here. These are scheduled tasks that are performed whilst the Logging Executor is working. 
There are 2 types of tasks :
1. Logging Stats Poller - prints basic number of executed tasks to STDOUT every **X** Seconds (Defaults to 1 second)
2. LoggingController - This will shutdown the Log Execution after **X** number of seconds (60 by default)

## ENV Variables 

- **java.logger.poller.interval**=10: How often to print logging stats to console (Seconds)
- **java.logger.executor.shutdown**=60 : How long to run the Log Generator for (Seconds)
- **java.logger.executor.log_dir**=/logs
- **java.logger.executor.log_name**=java-logger.log
- **java.logger.log.char_count**=0: Specify log length - This pads the log length with '*'

---
**NOTE:**
- _PROP_=X denotes the properties default value
- _char_count_ : simply appends additional chars to the standard log message, it currently does not trim to a specific length
---

# Performance Measurements 

Example Log Message ~100-130 bytes (55-65 chars)
e.g. `Logging TaskScheduler Successfully executed '1' times`

On my local setup this application will generate the following:
- 60 Seconds & 1 Thread Executor = 6.9 million log (1.1gb) / 60 = **115000 IOPS** (18.3Mb/S)
- 60 Seconds & 2 Thread Executor = 10.5 million log (1.6gb) / 60 = **175000 IOPS** (26.6Mb/S)
- 60 Seconds & 4 Thread Executor = 8 million log (1.3gb) / 60 = **133333 IOPS** (21.6Mb/S)

Specs: 

- Mac M1 Pro (32GB) // Ventura 13.2
- Java:
```bash
  openjdk version "17.0.6" 2023-01-17
  OpenJDK Runtime Environment Temurin-17.0.6+10 (build 17.0.6+10)
  OpenJDK 64-Bit Server VM Temurin-17.0.6+10 (build 17.0.6+10, mixed mode)
```
- Maven
```bash
  Apache Maven 3.8.7 (b89d5959fcde851dcb1c8946a785a163f14e1e29)
  Maven home: /Users/<USER>/.sdkman/candidates/maven/current
  Java version: 17.0.6, vendor: Eclipse Adoptium, runtime: /Users/<USER>/.sdkman/candidates/java/17.0.6-tem
  Default locale: en_GB, platform encoding: UTF-8
  OS name: "mac os x", version: "13.2", arch: "aarch64", family: "mac"
```

# How To Run

## Running Locally

1. Build the JAR : `$ mvn clean package` or alternatively you can run directly from maven `$ mvn spring-boot:run`
2. Run the JAR : `java -Djava.logger.executor.thread_pool_size=1 -jar target/java-logger.jar`
3. Inspect logs: logs/java-logger.logs


## Running on OCP 

For the sake of brevity we will build the image locally and push to a registry. 

1. Build the Image
2. Push to OCP 
3. Configuration 
4. Start

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/#build-image)

