# INDIGO-DataCloud Identity-Harmonization Service

This project provides the Identity Harmonization service as a Spring Boot application.

## Documentation

Documentation is available at [GitBook](https://www.gitbook.com/book/indigo-dc/identity-harmonization/details).

## Requirements

* JDK 1.8+
* [Maven 3+](https://maven.apache.org/)

## Build & Run & Configure

The project uses maven build automation tool that will build one fat jar Spring Boot application.

```
mvn clean package
```

The service can run without any additional server deployment (Tomcat, JBoss, etc.).

```
java -jar identity-harmonization-0.0.1.jar
