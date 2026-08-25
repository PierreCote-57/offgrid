# Decisions

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

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

Their list looks like it has not been refreshed in a while — confirm 21 when the account
is opened.

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

Package `com.logicielcote.offgrid`, artifact `offgrid`.

## 2026-08-24 — Template layout

Pages live at `templates/*.html`. Fragments live under `templates/fragments/`, one fragment
per file, so the tree says which is which and no file accumulates unrelated fragments.

Two fragment folders, split on who decides the markup appears:

- `fragments/site/` — chrome every page gets whether or not it asked for it: header, footer.
- `fragments/block/` — content a page deliberately places inside itself.

The day that boundary blurs is the day the split stops helping, so it is written down here
rather than left to be inferred from the folder names.

One fragment per file also means the `:: name` selector is unnecessary — a fragment
expression with no `::` includes the whole template, so `~{fragments/site/header}` is the
whole include. Fragment files therefore hold only their markup, with no `<html>`/`<body>`
wrapper.

## 2026-08-24 — Palette

Forest: bar `#1f5e42`, bar text `#eef3ef`, deep `#173f2d`, soft `#e7efe9`, body text
`#243027`, rules `#d6dfd8`. Declared as custom properties on `:root`, so the palette
changes in one place.

Header and footer wear the colour, the page between them is white.

**There is no contrasting accent colour.** Every colour on the site is a shade of the bar
green. An amber accent was tried and rejected — the button jumped off the page. Buttons are
outline style: soft fill, brand border, filling in on hover.

GettingLost's brown/Lora/Source Sans look was not carried over; a lighter, happier palette
was wanted instead.
