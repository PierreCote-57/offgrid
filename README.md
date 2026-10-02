# offgrid

A Spring Boot application serving the offgrid site.

## Profiles
- dev: for local development
- test: for testing
- prod: for production

## Requirements

- JDK 21 (Amazon Corretto 21)
- No Maven install needed — use the `mvnw` wrapper in this repo

## Build

    ./mvnw package

Produces `target/og-<version>-<yyyyMMdd-HHmmss>.jar`, a self-contained fat jar.

## Run

    java -jar target/og-<version>-<yyyyMMdd-HHmmss>.jar

Serves on http://localhost:8080.

From IntelliJ: open `pom.xml` as a project, then run `OffgridApplicationWeb`.

The command-line run is `OffgridApplicationCLI`, the other main in `com.lc.offgrid`.

## Layout

| Path | Holds |
| --- | --- |
| `src/main/java/com/lc/offgrid` | The application: the two mains, and `cliapp`, `common`, `pingapp`, `webapp` |
| `src/main/java/com/lc/basics` | Tools that are not offgrid's: `container`, `monitoring`, `tools` |
| `src/main/resources/templates` | Thymeleaf templates |
| `src/main/resources/static` | The css, js and drawings the site ships |
| `src/main/resources/data` | The site's content, as JSON |
| `src/main/resources/external` | Files as their supplier published them, and the older shapes they replaced |
| `src/main/resources/application.properties` | Spring settings that hold whatever the profile |
| `src/main/resources/application.yaml` | Site settings, with `application-dev.yaml` and `application-test.yaml` per profile |
| `src/main/resources/log4j2-spring.xml` | Where the logs are written |
| `folder.local` | Outside the repo, named per profile: `images/`, `documents/` and `logs/` |

## Stack

Spring Boot 4.1.1, Java 21, Thymeleaf, Maven (wrapper pinned in `.mvn/wrapper`).

Built by the GitHub workflows in `.github/workflows/`, which publish the jar as a release;
FullHost deploys it from there. See `docs/decisions/build.md`.
