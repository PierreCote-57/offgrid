# offgrid

A Spring Boot application serving the offgrid site.

## Requirements

- JDK 21 (Amazon Corretto 21)
- No Maven install needed — use the `mvnw` wrapper in this repo

## Build

    ./mvnw package

Produces `target/offgrid-0.0.1-SNAPSHOT.jar`, a self-contained fat jar.

## Run

    java -jar target/offgrid-0.0.1-SNAPSHOT.jar

Serves on http://localhost:8080.

From IntelliJ: open `pom.xml` as a project, then run `OffgridApplication`.

## Layout

| Path | Holds |
| --- | --- |
| `src/main/java/com/logicielcote/offgrid` | Application and controllers |
| `src/main/resources/templates` | Thymeleaf templates |
| `src/main/resources/static` | Static assets |
| `src/main/resources/application.properties` | Configuration |

## Stack

Spring Boot 4.1.1, Java 21, Thymeleaf, Maven (wrapper pinned in `.mvn/wrapper`).

Built by FullHost from this repo — Maven is what their build node runs, so the build must
stay Maven-driven.
