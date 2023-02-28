# build stage
FROM registry.access.redhat.com/ubi8/openjdk-11 AS builder
COPY . .
USER root
RUN mvn clean install -DskipTests

# runtime stage
FROM registry.access.redhat.com/ubi8/openjdk-11
COPY --from=builder /home/jboss/target/*.jar /home/jboss/java-logger.jar
WORKDIR /home/jboss
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "java-logger.jar"]