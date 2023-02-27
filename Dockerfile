FROM openjdk:11
COPY target/java-logger.jar java-logger.jar
ENTRYPOINT ["java","-jar","/java-logger.jar"]