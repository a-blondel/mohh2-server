# Build image
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /usr/local/app
# Copy project into image
COPY ./ /usr/local/app/
# Build
RUN mvn clean package -DskipTests


# Run image
FROM ibm-semeru-runtimes:open-17-jre
# Copy jar
COPY --from=build /usr/local/app/target/ea-nation-server-*.jar /ea-nation-server.jar

EXPOSE 8080

# Start command
ENTRYPOINT ["java", "-jar", "/ea-nation-server.jar"]
