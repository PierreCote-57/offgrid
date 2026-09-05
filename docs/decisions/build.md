# Decisions — Build

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

The stack, the pom and the path from this source tree to a working site on FullHost.

## 2026-08-24 — Repo

`offgrid`, private, under `PierreCote-57`. Private costs nothing on a personal account;
public would only buy unlimited Actions minutes and Pages, neither of which is in use.

Name chosen over `gettinglost*` because it is short, matches the content, and is a legal
Java package segment with no hyphen to strip.

## 2026-08-24 — Java 21 (Amazon Corretto)

FullHost's published Java stack tops out at 21 LTS (23 non-LTS, no 25), so 21 is the
newest version that is both LTS and runnable there. 25 was the first pick until their
stack list was read.

Non-LTS releases stop getting security patches roughly six months after they ship, which
rules them out for a server.

Their published list looks stale, but Pierre confirmed with FullHost on 2026-08-24 that
Corretto 21 is available. The pin holds.

## 2026-08-24 — Spring Boot 4.1.1

Current release on start.spring.io. Requires Java 17+, so 21 sits inside its range with
room ahead.

## 2026-08-24 — Maven, not Gradle

The project was generated with Gradle first and migrated. FullHost's build node runs
Maven, and building from GitHub was judged worth the switch: a deploy then needs nothing
from Pierre's machine — push from anywhere, including a phone, and the site updates. Same
shape as the GettingLost pipeline.

The cost accepted: the build runs on their JDK and Maven, so a build can fail there and
pass locally. Their build log is where that shows up.

IntelliJ is indifferent between the two.

## 2026-08-24 — Fat jar

`./mvnw package` produces one self-contained jar. Nothing is deployed as a war into a
container.

## 2026-08-24 — Shape of the application

Two controllers: a Client controller serving pages, and an MCP controller. Thymeleaf for
server-rendered pages, Alpine for client-side behavior, plus static content.

Package `com.lc.offgrid`, artifact `offgrid`.

## 2026-08-25 — Content lives in the repo

JSON and HTML content ship in the repo and reach the server the way the code does: push,
FullHost builds, site updates. No mounted volume, no Mountain Duck licence, and the
question of which container path survives a redeploy stops mattering.

The cost accepted: a typo fix is a push, a Maven build on their node and a restart —
minutes, and the site blips. Worth it while content changes in batches.

Images are **not** covered by this and are deliberately left undecided; git keeps every
version of every binary forever, which is the one place the repo stops being free.

## 2026-08-25 — The application class sits at the package root

`OffgridApplication` moved from `com.lc.offgrid.spring` to `com.lc.offgrid`. Component
scanning starts at the annotated class's own package, so anything outside `spring/` — the
first case was `misc/imaging` — was never scanned and could not become a bean.

`scanBasePackages` would have fixed the one case. Moving the class fixes every future one,
and the failure it prevents is nasty: an injection error somewhere unrelated to the class
that was actually invisible.

## 2026-08-31 — The JSON ships inside the jar, beside its HTML

The page data is a resource like the template it belongs to. `resources/data/` mirrors
`resources/templates/`, both are packaged into the fat jar, and content reaches the server the
way the code does. Nothing sits on disk beside the jar and nothing is mounted.

This closes the question the *Content lives in the repo* entry left open. A data file is not a
separate kind of thing to be deployed on its own terms — it belongs where its HTML belongs.
