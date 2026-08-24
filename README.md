# offgrid

A Spring Boot application serving the offgrid site.

## Requirements

- JDK 21 (Amazon Corretto 21)
- No Gradle install needed — use the `gradlew` wrapper in this repo

## Build

    ./gradlew bootJar

Produces `build/libs/offgrid-0.0.1-SNAPSHOT.jar`, a self-contained fat jar.

## Run

    java -jar build/libs/offgrid-0.0.1-SNAPSHOT.jar

Serves on http://localhost:8080.

From IntelliJ: open `build.gradle` as a project, then run `OffgridApplication`.

## Layout

| Path | Holds |
| --- | --- |
| `src/main/java/com/logicielcote/offgrid` | Application and controllers |
| `src/main/resources/templates` | Thymeleaf templates |
| `src/main/resources/static` | Static assets |
| `src/main/resources/application.properties` | Configuration |

## Stack

Spring Boot 4.1.1, Java 21, Thymeleaf, Gradle 9.5.1 (Groovy DSL).
