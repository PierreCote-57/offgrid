# offgrid — working agreement

## READ THIS AT THE START OF EVERY SESSION

Follow the rules in this file and in the file it points to. All of them, every time.

## 1. How to work with Pierre

**`~/Claude/working-with-pierre.md`** — loaded automatically in every project via
`~/.claude/CLAUDE.md`. FIND ≠ FIX, plan before implementing, answer short, opinions vs
verdicts, park small findings, work from fresh data, code conventions. Those rules apply
here in full and are not repeated below.

## 2. Where this project's knowledge lives

- **[docs/decisions.md](docs/decisions.md)** — what was decided and why. Read it before
  proposing a change to the stack, the build, or the deployment path.
- **[docs/todo.md](docs/todo.md)** — parked work. Side issues found mid-task go here.

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
