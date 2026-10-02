# Decisions — Build

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

The stack, the pom and the path from this source tree to a working site on FullHost.

## 2026-08-24 — Repo

**Superseded 2026-10-02** — the repo is public, so FullHost can download release jars without
signing in. See *Built on GitHub, deployed from a release* below.

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

**Extended 2026-10-02** — GitHub Actions now runs the Maven build, and FullHost's build node is
gone. Maven stays: the workflows call `./mvnw`.

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

**Extended 2026-10-02** — "FullHost builds" is now "GitHub builds a release and FullHost deploys
it"; the rest holds.

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
first case was `common/misc/imaging` — was never scanned and could not become a bean.

`scanBasePackages` would have fixed the one case. Moving the class fixes every future one,
and the failure it prevents is nasty: an injection error somewhere unrelated to the class
that was actually invisible.

## 2026-08-31 — The JSON ships inside the jar, beside its HTML

The page data is a resource like the template it belongs to. `resources/data/` mirrors
`resources/templates/`, both are packaged into the fat jar, and content reaches the server the
way the code does. Nothing sits on disk beside the jar and nothing is mounted.

This closes the question the *Content lives in the repo* entry left open. A data file is not a
separate kind of thing to be deployed on its own terms — it belongs where its HTML belongs.

## 2026-09-05 — Three package roots, two mains

**Extended 2026-09-15** — `pingapp` is a fourth root with a third main,
`OffgridApplicationPing`, sitting inside it rather than beside the other two. See
`ping.md`. Everything else here still holds.

`com.lc.offgrid` holds nothing but the site and CLI launchers. Below it, `webapp` is what the site needs,
`cliapp` is what a command-line run needs, and `common` is what both need. Each launcher is a
`@SpringBootApplication` naming its own `scanBasePackages` — common plus its own — so neither
scans the package the launchers sit in and neither picks the other up. That is what makes a
second `@SpringBootApplication` safe: while both were reachable from one scan, a
`CommandLineRunner` written for the CLI would have fired every time the site started.

This does not repeat the *application class sits at the package root* entry above, it refines
it. That entry moved the class so the default scan would reach everything; naming the roots
explicitly costs one line per launcher and buys two contexts that cannot see each other's
beans. The price it names still stands: naming any root replaces the default, so a root left
off the list is silently invisible.

**`common` must not import `webapp`.** The direction is the whole point of the split, and it
is one grep to check. Two classes crossed it on the first pass and both had a better home:
`readFile` was a filesystem-then-classpath JSON read sitting on `BaseWebProcessor` and moved
to `BaseFileHandler`, and the chat wire shapes moved into `webapp/pojo` beside the endpoint
that answers them.

**The web type comes from the classpath, not from the class you launch.** The webmvc starter
is a compile dependency, so a CLI main that does not set `WebApplicationType.NONE` starts
Tomcat. With NONE, nothing holds a non-daemon thread, so the JVM exits when `main` returns.

**Two mains in one jar.** Against the build tree either class runs as an ordinary main. Out of
the repackaged jar the `Main-Class` is Boot's launcher and the `Start-Class` is
`OffgridApplicationWeb`, so the second main is reached through `PropertiesLauncher` with
`-Dloader.main` — to be validated against Boot 4.1, which moved the loader classes.

The `start-class` property in the pom is what names it. The repackage goal otherwise looks for
a single main class and fails on two, and the site is the one a plain `java -jar` should get:
the CLI is the run you have to ask for.

**One test context, all three roots.** `src/test`'s `OffgridTestApplication` scans `common`,
`webapp` and `cliapp`, so a test reaches any bean whichever launcher owns it in production.
Two `@SpringBootApplication` classes sit in `com.lc.offgrid`, so a bare `@SpringBootTest` no
longer finds one configuration by searching upward: every `@SpringBootTest` names its class.
`OffgridApplicationTests` names `OffgridApplicationWeb`, which is what keeps that test a check
that the site's own context starts.

## 2026-09-05 — Mockito is excluded from the test starters

No test in `src/test` uses it. It arrived transitively — both `-test` starters pull
`spring-boot-starter-test`, which carries `mockito-core` and `mockito-junit-jupiter` — and
announced itself on every run by self-attaching a Byte Buddy agent to the live JVM, which the
JDK then warned about four times. The exclusion names the whole `org.mockito` group on both
starters, since either path alone would bring it back.


## 2026-09-24 — `application-host.yaml` waits for the FullHost deploy

**Nothing new goes into `application-host.yaml` until the site is deployed to FullHost.** Its
values — host name, ports, protocol — are only known once it runs there, so a setting added
for the local profile gets its `-host` counterpart at deploy time, not before.

## 2026-10-02 — Built on GitHub, deployed from a release

**GitHub Actions builds the jar and publishes it as a release; FullHost deploys it by URL.** The
two workflows are the master: `build-release.yml` (build + pre-release) and `release.yml`
(promote a pre-release, move the pom to the next version). FullHost's `DeployArchive` takes the
release's download URL.

Why not FullHost's own build node: deploying a chosen build there needs the archive's download
URL, and Deployment Manager lists its archives only through a dashboard script (`GetArchives`,
run by `development.Scripting.Eval`) that an API token cannot be granted. A GitHub release has a
URL anyone can read off it. The build node was then unused, about CA$1.40 a month, and was
removed.

The repo went public for this: FullHost downloads without signing in. The only secret in its
history, the Google Maps key, is one every visitor's browser already receives.

**Jar and release share one name.** `finalName` is `og-<version>-<yyyyMMdd-HHmmss>` (UTC), which
is also the pre-release's tag and title; a release is `og-<version>`. `og-` keeps the name short
enough for GitHub's release lists. The `-SNAPSHOT` changelist was dropped: GitHub's Pre-release
flag carries that distinction, and a release is renamed, not rebuilt, so a qualifier baked into
the jar would have said SNAPSHOT on a release.
