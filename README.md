# Java Logger

This is a very simple Java Spring app to generate logging load to help flush out Storage IO issues. 

* The App consists of 2 parts: 
  - LoggingExecutor 
  - LoggingSchedulerUtils 

## LoggingExecutor

This performs the log load generation, it uses a simple configurable thread pool TaskExecutor with a bounded queue size of 10k (to keep memory usage low). 
The app will keep the bounded queue full with logging tasks. The task executor will execute simple `log.debug(<TEXT>)` statements as fast as the disk can handle. 

This allows you to configure LogBack in various ways to test its speed and performance. 

---
**NOTE:**
- LogBack is currently configured with a simple synchronous blocking mechanism meaning the executor thread will be blocked for as long as it takes to persist the debug log to storage. <br/>
- LogBack is configured to have all **DEBUG** statements persisting to disk, whilst **INFO** and above goes to STDOUT and DISK
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

- **JAVA_LOGGER_EXEC_THREADS**=1: How many threads to use for Logging Executor

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

## Running Locally as Java App

1. Build the JAR : `$ mvn clean package` or alternatively you can run directly from maven `$ mvn spring-boot:run`
2. Run the JAR : `java -Djava.logger.executor.thread_pool_size=1 -jar target/java-logger.jar`
3. Execute the API: `curl -X POST 'http://localhost:8080/logs/generate?runtime=60&polltime=5'`

## Running Locally as Container

1. Build the container: `$ podman build --platform linux/amd64 -t java-logger .`
2. Run the container: `$ podman run --name java-logger -d -p 8080:8080 java-logger`
3. Execute the API: `curl -X POST 'http://localhost:8080/logs/generate?runtime=60&polltime=5'`
4. Inspect STDOUT: `$ podman logs java-logger`
5. Inspect container log file: 
    ```
    $ podman exec -t java-logger /bin/bash
    $ >> [container-bash] tail -f -100 /home/jboss/logs/<container_id>_java-logger.log
    ```

## Running on OCP 

For the sake of brevity we will build the image locally and push to a registry. 

1. (Optional) Build the Image

   -  `$ podman build --platform linux/amd64 -t java-logger .`
     - Optional (Test Build): 
       - `$ podman run --name java-logger -d -p 8080:8080 java-logger`
       - ```
          $ curl -X GET "http://localhost:8080/logs/ping"
          > Pong
          $ podman logs java-logger
         ... <JAVA Crap> ...
       ```

2. (Optional) Push to OCP Registry or Quay 

    ```bash
    $ podman tag java-logger quay.io/ajarrett/java-logger
    $ podman push quay.io/ajarrett/java-logger
    Getting image source signatures
    Copying blob sha256:...
    Writing manifest to image destination
    Storing signatures
    ```

3. Deploy to OCP  

    - There is a basic deployment config in the `/openshift` folder. 
    ```bash
    $ oc new-project java-logger
    $ oc apply -f openshift/deployment.yml
    ```
---
**NOTE:**
This creates Deployment, PVC, Service + Route.<br />
_**Please change PVC to match Storage reqs.**_
---


4. Start the Log Generator via API 

   - Execute the API: `curl -X POST 'http://<OCP_ROUTE_PATH>/logs/generate?runtime=60&polltime=5'`

---
Parameters for the API

**runtime:** Time in seconds to run the test

**message_length:** Length of padding for the log message size

**polltime:** How often to publish status to container STDOUT


**NOTE:**
Postman collection is available in Repo
---

5. Inspect STDOUT for Log Generator stats: `$ oc logs <POD>`

6. Inspect container log file:
    ```
    $ oc rsh <POD>
    $ >> [pod-bash] tail -f -100 /home/jboss/logs/<container_id>_java-logger.log
    ```


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.8/maven-plugin/reference/html/#build-image)

