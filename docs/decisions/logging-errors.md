# Decisions — Logging and errors

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

Log4j2, the app log and the visit log, and what the site does when a request fails.

## 2026-08-25 — A user who edits the URL is on their own

"If you try to write foolproof software, nature will invent a better fool." The site is
clean and bug free for the URLs it publishes. A URL nobody linked to is not a case to be
designed for.

What a hand-edited URL owes: **survive, and leave a developer enough to diagnose it.** It
must not corrupt anything and it must not die silently. It owes the person who typed it
nothing beyond that — no friendly wording, no guess at what they meant, no recovery path.

The concrete instance: a parameterized route matches any name in its shape, so
`/info/useful-anything` reaches `readFile`, throws, and is caught into the `exception`
view. That is the correct outcome, not a hole to plug.

`templates/exception.html` was written 2026-08-31 and is the site's own page: a fixed
sentence saying nothing was changed, the menu to leave by, and `errorMessage` in the
`.message-error` strip for whoever is diagnosing it. `BaseWebController` sets `PageName` in
the catch so the heading and the tab have something to read. A failure landing before
`processDefault` runs still leaves the rest of the chrome empty, which is accepted until it
is seen.

## 2026-09-02 — Logging: Log4j2, two files, and one row per page view

**The stack is Log4j2, not Logback.** `log4j2.xml` had been sitting in `src/main/resources`
doing nothing: `spring-boot-starter-logging` binds Logback, and `log4j-to-slf4j` routes the
Log4j2 API calls `BasicLogger` makes into it, so the file was read by nobody. Making it live
took `spring-boot-starter-log4j2` plus an exclusion of `spring-boot-starter-logging` on every
starter that pulls it — the four Boot ones and `spring-ai-starter-mcp-server-webmvc`, which
brings `spring-boot-starter-web` and with it the whole logging starter again.

**The file has to be named `log4j2-spring.xml`.** `${spring:folder.local}` is Boot's own lookup
(`SpringEnvironmentLookup`, plugin name `spring`), and it needs the Environment attached to
the LoggerContext, which Boot only does for the `-spring` name. Under the plain name Log4j2
finds the file first, at the first logger call, and the lookup throws.

**The files go under the `folder.local` root, in `logs/`.** Locally that is
`~/Working/Offgrid/logs`, holding `app/` and `visit/`. The host value waits for FullHost —
#1 in `docs/todo.md`.

**The visit log is its own file, its own logger and its own format.** One row per page view,
written by `logVisit` in `BaseWebController.processRequest`, on the logger `offgrid.visit`
with `additivity="false"` so nothing else lands in it and it lands nowhere else. Tab
separated rather than comma, because a comma is legal inside a query string and a tab is not,
which is also why the file is `.tsv`: time, address, method, path, view, milliseconds. A
rolling file has nowhere to put a header row, so the column names belong to whatever reads
it. Rejected: Tomcat's access log valve, which is one property and would have logged every
asset request alongside the page views, and cannot know the view name.

Its duration measures the model build, not the render — `logVisit` runs before the controller
returns and Thymeleaf renders after that. Accepted as such rather than moving the timing into
a filter.

**`org.apache.tomcat.util.net.NioEndpoint` is off.** Its acceptor logs an ERROR with a
`SocketException: Invalid argument` for each connection whose peer is already gone when the
socket options are applied — Tomcat calls `setSoLinger` on every accepted socket because
`AbstractProtocol`'s constructor sets `connectionLinger` to -1, so no configuration turns the
call off. Nothing of ours is on that stack and no request is lost.

**A prototype processor has to be asked for, not injected.** `OffgridWebProcessor` is
`@Scope("prototype")`, but an `@Autowired` field on the singleton controller is built once at
startup, so one instance was serving every request — carrying `m_model`, the message lists and
the timer the footer prints, which made that timer read as process uptime. The controller now
calls `getBeanFactory().getBean(OffgridWebProcessor.class)` per request. That also restored the
`"Done processing"` checkpoint, which had never run: the constructor registers the processor
with the `PageContext`, and at startup there was none.

## 2026-09-03 — A 404 is thrown, not returned, and the error page renders it

**`ResponseEntity.notFound().build()` was answering nothing.** It writes the status and
commits the response, which never reaches the container's error dispatch — so the three
endpoints that bypass `processRequest` (`/image/`, `/document/`, `/shared/browser/data/`)
put a blank page on the screen. `makeNotFound(format, args)` returns a
`ResponseStatusException(NOT_FOUND, message)` and each site throws it. The factory hands
back the exception rather than throwing it itself, so the call site reads `throw` and the
compiler's flow analysis stays honest — a method declared to return a value that never
returns makes every caller read `return` on a line that cannot return.

**`templates/error.html` is what the dispatch renders**, and it uses none of the site
fragments. That dispatch runs without a processor, so `Timer`, `SiteName`, `WelcomeMessage`
and `SiteVersion` are absent, and the footer's `${Timer.elapsedTime}` would throw — a page
whose job is to appear when things fail cannot depend on a model nobody set. Giving it the
header and footer would need an `ErrorController` that calls `processDefault` first, and
that is deliberately not done: it would make the page that appears when things are broken
depend on something having worked. The link home is the part that matters.

**`server.error.include-message` is per profile**: `always` locally, `on-param` on the host.
Boot's default drops the reason entirely, so a message passed to `ResponseStatusException`
would never reach the page. `on-param` shows it only for a request carrying `message=true`,
which keeps an internal exception message off a visitor's screen while a diagnosis can still
ask for it. The key lives in the profile yamls and not in `application.properties`: both sit
in the same location, and a key in both makes the winner depend on load order.

**The exception page renders because `processDefault` runs first.** `processPage` used to
read the page json before calling it, so a failure in the read left `Timer` unset and the
exception view died mid-render — the whitelabel page, not the site's. `processDefault` now
runs first and `PageName` is replaced afterwards, by `pageData.getName()` on the way out or
by `BaseWebController`'s catch on the way to the exception view.

**A row whose `file` pointer will not read is logged and nothing more.** It was briefly an
`addErrorMessage`, which lands in a processor that dies with the request: the JSON endpoint
never calls `processDefault`, so no model and no session ever carries it. A broken pointer
is a content defect in the repo, which the log is the right place for, and the row goes out
unhydrated with a 200.

**A missing image answers a drawn image, not a 404.** An error page is the wrong answer to
an `<img>`: the reader gets a broken-image icon and the reason goes nowhere. `/image/` now
returns an SVG saying "Not found" over the name asked for, at 200 — the status is what makes
the browser draw it, and nothing on this site reads the status. It is SVG rather than a
`BufferedImage`: the browser draws the text with its own fonts, so the server needs no fonts
installed, and the picture scales to whatever box it lands in. Both lines state a
`textLength`, which is what makes a long name squeeze instead of running past the edge.
`makeMessageImage(line1, line2)` takes the two lines rather than the failure, so the caller
owns the word "Not found". `/document/` and `/shared/browser/data/` still throw: those are
read as pages, where error.html is the right answer.

**`processRequest` lets a `ResponseStatusException` through.** Its `catch (Exception)` would
otherwise turn a deliberate 404 into the exception page at status 200, with the thrower's
message replaced by "Failed to process request" — the reason a page URL with no json behind
it needed both a null check in `readPageJson` and a rethrow ahead of the general catch. That
path logs INFO and writes its own visit-log row, so a not-found is not missing from the log
and is not dressed as a server failure.

**A defect in a json file this repo owns is left to land on the exception page.** A missing
`location` under a `googleMap`, a `datasets.json` that will not parse, an entry in it with no
`id`: each gives the reader "Something went wrong" and puts the exception, the method, the
full URL and the parameters in the log. Guarding them would trade a stack trace pointing at
the line for a message that says less. The browser page needs no guard either — `fetchJson`
already reports the URL and the status where the list would be, and its page render reads
`datasets.json` first, so a broken file stops the page before the data call is ever made.

**INFO is a URL issue, WARN is a content issue.** Neither is the server's own trouble, which
is what ERROR is for, so the split is about whose mistake the log is reporting. A name that
resolves nothing — `/image/{imageName}`, `/document/{documentName}`,
`/shared/browser/data/{id}` are all path variables — came from a hand-edited URL and is INFO.
A file this repo owns that is wrong — a blog entry with no date, a dataset row whose `file`
pointer will not read — is WARN, because it is a defect someone here has to go and fix.

## 2026-09-25 — Two AI logs: one row per question, one per MCP call

**Two files in `logs/ai/`, each shaped like the visit log**: its own logger and appender,
`additivity="false"`, tab separated, the time in front. They are two files rather than one
with a kind column because the two rows share no columns — one file would need empty fields
or a parser per kind.

**`offgrid-claude.tsv`, logger `offgrid.claude`: one row per question, written by the caller
of `send`** (`chat` today, through `ClaudeManager.logClaudeCall`), not once per round — the
tools a question ran are the MCP log's rows. Columns: who asked, the model that answered, the
stop reason, the effective tokens of every round as a whole number with thousands separators,
the seconds the whole call took to three decimals, then the question. The two numbers tell
themselves apart by their shape — a whole count, a time with a decimal point — so neither
carries a label; the separators follow the machine's locale. The question is last because it is the one column that can hold anything; its tabs and
line breaks become spaces. The stop reason stays although it is nearly always `end_turn`: the
other values are the rows where the visitor got no answer.

**`offgrid-mcp.tsv`, logger `offgrid.mcp`: one row per call, written by the tool or resource
itself** through `AbstractOffgridMCP.logMcpCall`, as `methodName(parameters)`. Who asked is not
recorded: a call from the chat and one from an outside client over `/mcp` leave the same row,
because the class answering is the one place both pass through. Rejected: logging in
`callUseBlock`, which sees only the chat's calls.
