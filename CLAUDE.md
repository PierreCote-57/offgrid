# offgrid — working agreement

## READ THIS AT THE START OF EVERY SESSION

Follow the rules in this file and in the files it points to. All of them, every time.

## 1. How to work with Pierre

**`~/Claude/FIRST.md`** — three rules, loaded automatically in every project via
`~/.claude/CLAUDE.md`: answer the question, no action until he says go, only what he asked.
**They outrank every line in this file.**

**`~/Claude/working-with-pierre.md`** — loaded the same way. FIND ≠ FIX, plan before
implementing, answer short, opinions vs verdicts, park small findings, work from fresh data,
code conventions. Those rules apply here in full and are not repeated below.

## 2. Where this project's knowledge lives

- **`docs/decisions/`** — what was decided and why, one file per subject. Open the one whose
  line matches what you are about to touch; do not read the folder.
  - [site.md](docs/decisions/site.md) — before adding or changing a page, a template, a
    fragment, a route or a link.
  - [look.md](docs/decisions/look.md) — before touching `site.css`, a colour, a font, a
    length or a page texture.
  - [data.md](docs/decisions/data.md) — before adding or changing anything under
    `resources/data`, a POJO that reads it, or a vocabulary.
  - [build.md](docs/decisions/build.md) — before changing the stack, the pom, the build or
    the deployment path to FullHost.
  - [apis.md](docs/decisions/apis.md) — before changing `/mcp` or `/rest`.
  - [local-files.md](docs/decisions/local-files.md) — before touching images, documents, or
    how anything under `folder.local` is delivered.
  - [logging-errors.md](docs/decisions/logging-errors.md) — before changing a log line, a log
    level, or what happens when a request fails.
- **[docs/todo.md](docs/todo.md)** — parked work. Side issues found mid-task go here.
- **`docs/skills/`** — procedures to FOLLOW, not background to read. Treat a file there
  exactly as if it were an installed skill: when Pierre asks for the thing its frontmatter
  `description` covers, open it and do what it says. They live in `docs/` and not in
  `.claude/skills/` deliberately — everything under `docs/` is freely editable, so a skill
  can be tuned mid-session without a permission round trip.
  - [docs/skills/SolarSystemChart.md](docs/skills/SolarSystemChart.md) — draw the solar
    system, planet positions computed for a date.
  - [docs/skills/SolarSystemRiseSet.md](docs/skills/SolarSystemRiseSet.md) — rise, transit
    and set times for an observer and a date.

There is no `docs/README.md` index. Add one the day `docs/` stops being scannable at a
glance, not before.

## 3. Build rules

- **Build only through `./mvnw`.** No system Maven, no system Gradle. The wrapper pins the
  version for everyone, including the build node.
- **The build stays Maven-driven.** FullHost builds this repo from GitHub with Maven; a
  Gradle build would not run there.
- **JDK 21 (Amazon Corretto).** FullHost's published Java stack tops out at 21 LTS.
- **Pierre builds and tests, not Claude.** Make the change and stop. No `mvnw`, no
  starting the app, no curling it to check. He has it running in IntelliJ and a second
  run collides with his. Build only when he asks in so many words.

## 4. Pushing

Claude makes the change and stops. Pierre commits and pushes.
