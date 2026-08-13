FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25 AS maven

RUN mkdir -p /workdir

WORKDIR /workdir/java

FROM maven AS maven-package

COPY .mvn ./.mvn
COPY pom.xml rewrite.yaml ./
COPY libraries libraries
COPY services services

RUN mvn \
    --define 'user.home=/workdir' \
    --define 'maven.repo.local=/workdir/.m2/repository' \
    --define 'quarkus.package.jar.type=uber-jar' \
    package

FROM maven-package AS package-with-config

ENV AUD_DATA_LOCAL_FILE_USE_LOCAL_DATA=true
ENV AUD_DATA_LOCAL_FILE_LOCAL_DATA_DIRECTORY=/tmp/yggy-data
ENV QUARKUS_AZURE_STORAGE_BLOB_ENABLED=false
ENV QUARKUS_LOG_CONSOLE_JSON_ENABLED=false

# Default target for `docker build .`
FROM package-with-config AS default

CMD ["java", "-XX:+UseDynamicNumberOfGCThreads","-XX:+UseStringDeduplication", "-XX:InitialRAMPercentage=75", "-XX:MaxRAMPercentage=85", "-XX:+AlwaysPreTouch", "-XX:+UseCompactObjectHeaders", "-jar", "/workdir/java/services/service-http-apis/target/service-http-apis-1.0.0-SNAPSHOT-runner.jar"]
