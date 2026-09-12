# Decisions — APIs

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

The endpoints that answer JSON to a program rather than HTML to a browser: `/mcp` and
`/rest`.

## 2026-09-01 — The MCP server is Spring AI's, not ours

The hand-rolled endpoint was deleted. Spring AI's MCP server starter carries the transport,
the sessions, the JSON-RPC envelopes, the protocol errors and the `tools/list` publishing —
every one of which we had written by hand — and generates a tool's input schema from the
method it annotates. What survives is what was ours to begin with: what each tool knows.

**What made the switch worth it was the schema.** A tool's arguments are a class, and the
schema comes from the class rather than from a constructor that declares each argument by
hand. Field descriptions ride on Jackson's `@JsonPropertyDescription`, not `@McpToolParam`,
which is not applied to the fields of an object parameter — spring-ai#2866. An enum argument
publishes its constant names, so a vocabulary reaching a tool needs `@JsonProperty` for the
word the site writes, beside the `@SerializedName` Gson already reads.

The version is 2.0.1, pinned in `pom.xml` beside the other pinned dependency rather than
through the Spring AI BOM: it is the line built against Spring Boot 4.1.1, which its own
pom names. 1.1.8 is the Boot 3.5 line.

Not decided here: how `/mcp` is protected — #42 in `docs/todo.md`.

## 2026-09-01 — The first MCP pass, and the property that has to be stated

Everything new lives in `com.lc.offgrid.mcp`, one class per thing published, and every answer
is hard-coded — the pass exists to show what the framework looks like and what adding a tool
costs, not to answer anything. Where the real answers come from is #6 in `docs/todo.md`.

**An image is addressed by a URI template, not a fixed URI.** `offgrid://image` reads back the
file names and `offgrid://image/{fileName}` reads back one image, which is the shape the
domain has: there is no "the sample image", there is a set. The cost, seen in the run: a
template is published under `resources/templates/list` rather than `resources/list`, and a
client that lists only resources does not show it — reading a matching URI still works.

**`spring.ai.mcp.server.protocol` must be written out even though `STREAMABLE` is the
properties class's own default.** `McpServerAutoConfiguration.EnabledStreamableServerCondition`
is a `@ConditionalOnProperty` with `matchIfMissing = false`, so it reads the environment and
never sees the Java default. Absent property, no transport is installed and `/mcp` answers 404
while the site keeps serving normally — which is exactly what it did.

A tool method returning a String gets no output schema and answers as text: `SyncMcpToolProvider`
skips schema generation for simple value types. An argument is a `@McpToolParam` on a plain
parameter; the object-parameter case, where the description has to ride on Jackson, is what
spring-ai#2866 is about.

## 2026-09-02 — The chat page, and the REST controller it arrives on

**`OffgridRestController` is the site's REST controller, not the chat's.** It is a peer of
`OffgridWebController` in `webapp/spring/site` — same shape, same `getBeanFactory().getBean(...)` per
request — and answers JSON where the other names a view. Chat is its first endpoint, at
`/rest/chat`. `OffgridRestProcessor` backs it, a peer of `OffgridWebProcessor` that never
touches a `Model`. Rejected: putting the method on `OffgridWebProcessor`, which would have made
a view processor carry work that has no view.

**The page needs no Java.** `/info/chat` is served by the existing `info()` mapping, so it is
a template, its `chat.json`, and one `<li>` in the Info submenu.

**The whole transcript goes on the wire every turn**, `{"messageList":[{"role","text"},…]}`,
oldest first. The server keeps nothing between turns: a reload starts a new conversation and
there is no session state to expire. The echo reads only the last line. Rejected: posting
just the new message, which is smaller now and buys a contract change or server-side state
the day an LLM needs the history.

**`role` is an enum, not a string.** Two constants, `USER` and `ASSISTANT`, each carrying its
wire spelling on a `@JsonProperty` — the same shape as the Gson enums in `pojo/part`, with
the annotation Jackson reads. An unrecognised role is then a rejected request rather than a
string that flows on unnoticed.

**The answer shows the server's timing.** `ChatAnswer extends RestBaseAnswer`, so
`durationText` comes for free and is drawn under the bubble. It is the visible proof the
reply came from the server: without it a JS bug that never calls the server looks exactly
like success.

**Both serializers stay.** Jackson answers HTTP, reading getters, which is how a computed
value like `durationText` reaches the wire; Gson reads the data files, where the fields are
the file's shape. Switching MVC to Gson was considered and rejected: it would rename the wire
keys to `RestBaseAnswer`'s `m_`-prefixed fields, and Spring AI would keep using Jackson for
`/mcp` regardless, so there would still be two.

**The wire shapes live in `webapp/pojo/chat`, the controller and processor in `webapp/spring/site`.**
`ChatRequest`, `ChatMessage` and `ChatAnswer` are data, so they sit with the other POJOs;
they are the first ones there annotated for Jackson rather than Gson, because they are the
only ones that travel over HTTP rather than out of a file.

**`OffgridController` and `OffgridProcessor` gained a `Web`.** With a REST pair beside them
the bare names said nothing, and the bases they extend were already `BaseWebController` and
`BaseWebProcessor`. Earlier entries in this file were rewritten to the new names, so every
name here is one that can be found in the tree.

**`chat.js` is listed BEFORE `alpine.min.js`, and every future `Alpine.data` file must be.**
Alpine's last line is `queueMicrotask(() => Alpine.start())`, and microtasks drain between
deferred scripts, so `alpine:init` has already fired by the time a script listed after Alpine
runs. Registered too late, the component silently does not exist: `x-data="chat"` resolves to
nothing and the page renders correctly with dead controls. The inline `x-data` objects in
`menu.html` are unaffected, which is why nothing caught this earlier.

**The chat page is the one page whose height is capped, not floored.** Everywhere else `body`
is `min-height:100vh` and the page grows with its content. That is why an inner box cannot
scroll: `main`'s `flex:1` has no free space to receive in an auto-height container, so it
takes its content's height and the box grows with it. `body:has(.og-chat-page)` sets
`height:100dvh`, and `min-height:0` at each level below lets the log shrink and scroll
instead. On a viewport too short for the log's 260px floor the page scrolls again, accepted
as the degradation. Rejected: `calc(100vh - …)` on the log, which hard-codes the height of
the header, the `h1`, the input row and the footer.

**The log is a surface above the page, drawn with the site's existing device** — white fill,
1px `--rule` border, and `.og-lb-panel`'s shadow, the same treatment as the menu and the
filter panel. The answer bubble went off-white so it stays visible against it. macOS hides an
overlay scrollbar until you scroll, which left no sign there was anything above the top, so
the log states `::-webkit-scrollbar` and reserves its gutter.

**An exchange is a question and the answer under it, and they meet.** The space in the log is
between exchanges, not inside one. The visitor's line carries the wall clock, the answer
carries the server's `durationText` — one timing on each side, and the pair reads as one
event. `scrollToBottom` runs on `$nextTick` after each push; reading `scrollHeight` before
Alpine has drawn the bubble scrolls to where the log ended one message ago.

**The first pass answers by repeating the last line back.** What is being proven is the round
trip. The LLM behind it, and the in-process tools it would call, are not this pass — the MCP
server publishes those tools to an outside client and is not the way in for a chat running in
the same JVM.

## 2026-09-10 — The sky endpoint, and the shape a time takes on the wire

**`/rest/sky/data` on `OffgridRestController`, through `processRequest`.** The first path
proposed was `/info/sky/data`, which would have put it on the web controller beside the page:
a method mapping cannot escape the controller's class-level `/rest`, so the path decided the
controller. Going through `processRequest` is what `/sky/chart.svg` gives up — the answer
extends `RestBaseAnswer`, so it carries its own timing and a failure is logged and answered
500 in one place.

**The parameters — `timezone`, `lat`, `lng`, and later `epochSecond` — arrive as text, null
when absent.**
Rejected: `Double`, which is the natural REST shape and would refuse a malformed number with a
400. Spring's conversion runs before the handler, so that 400 never reaches `processRequest`
and the body is Spring's error rather than ours.

**`OffgridUtil` holds what both processors need**, in `webapp/spring/site`: the defaults, the
limits, and the parse methods, all public static. It exists because the same reader and the
same constants were about to live in both processors, and a rule like "latitudes only below
85°" would then have been two edits. Its parse methods follow one pattern — one `try`, `catch
(Exception)` so a null text is the same path as a bad one, log only when the text was actually
stated, then fall back — and `parseLatitude`/`parseLongitude` wrap `parseDouble` so a caller
passes one argument. `parse`, not `read`: reading starts at an external source, and these are
handed the text.

**A failing log line or exception names the call, `methodName(parameter values)`.**
`parseDouble('foo', 50, 90) could not be parsed` says which caller it was and what it was
trying to do, without opening the code. Every parameter goes in, including the ones that look
like configuration.

**`SkyAnalyser` throws `BasicRuntimeException`, catching `Exception`.** `processRequest` takes
a `Supplier`, which cannot throw checked exceptions, so the checked pair `HorizonsEphemeris`
declares had to stop somewhere; nothing a caller states makes a missing ephemeris file
readable, so there is nothing for it to catch.

**A moment on the wire is an epoch second beside a zone id stated once.** JSON has no date
type and JavaScript's `Date` is an instant with no zone in it, so a `ZonedDateTime` serialized
by Jackson arrives as an ISO string with an offset and the zone's name is gone — the client
cannot say what the times mean. Every time in one answer is in the same zone, so the zone
travels once as its own value. A `long` and a `String` also owe nothing to a serializer:
Jackson writes `java.time` and Gson does not, since `java.time` is not open to Gson's
reflection.

**`SkyBodyDay` was ruled off the answer here, and that was reversed on 2026-09-11.** It is
what `SkyAnalyser` works in, and it turned out to be what the page needs as well.

## 2026-09-11 — What the sky answer carries

**A fourth parameter, `epochSecond`, states the moment; absent, it is now.** The zone is
already on the call, so the pair is exactly the `ZonedDateTime` `SkyAnalyser` takes, and
`OffgridUtil.parseZonedDateTime(epochSecondText, timeZoneText)` reads both and hands back the
one value the processor then holds. Seconds rather than milliseconds: nothing the sky answers
is finer than a second, and the parameter and the value that comes back are then the same
number. The cost is on the client, where a `Date` is built from milliseconds.

**Outside 1800–2050 the parse answers with the end it passed**, which is the rule already
stated on `SKY_FIRST_DATE`: outside the JPL elements' stated range the positions are wrong
rather than rough. It lands in `parseEpochSecond` because that is where a date first exists.

**`SkyInfoRestAnswer` is constructed from the `ZonedDateTime` and stores the two parts.**
Handing it the moment keeps the splitting in one place; storing `epochSecond` and `ZoneId` is
what goes on the wire, and the client puts them back together. The observer's `latitude` and
`longitude` ride beside them, so the answer states what the request was read as — every
parameter falls back silently, and without the echo a caller cannot tell a fallback from a
value it sent.

**The analyser's two maps go on the answer as they are**, `getSunAngleMap` and
`getSkyBodyDayMap`, through setters rather than the constructor: they are what the endpoint
was built to answer, and a setter keeps the constructor to the observer. Both are keyed by
`HorizonsBody`, so Jackson writes the constant names as the JSON keys.

## 2026-09-12 — What the browser needs the answer to state

**`bodyMap` joined the answer, keyed like the other two maps.** Its entries carry the body's
name and its `periodDay`, so a browser drawing the table names its own rows instead of holding
a list of names that has to be kept in step with the enum. `SkyBodyInfo` is the wire shape and
`SkyInfoRestAnswer` builds the map in its constructor from `HorizonsBody.values()`: nothing in
it depends on the request.

**`periodDay` went on `HorizonsBody` rather than into `SkyAnalyser`.** It does not change with
the date or the observer, which is all the analyser exists for, and a second enum mirroring the
same nine constants is one more place to keep in step. The enum's javadoc now says the name and
the period are ours, not Horizons': an ephemeris file states neither.

**`orbitRadius` joined `SkyBodyDay`, beside `distance`.** It is the length of the very vector
`getSunAngle` takes its direction from — body minus Sun, both geocentric — so the ephemeris
already had it and only the angle was being kept. `distance` is from the observer and
`orbitRadius` is from the Sun; for the Moon that is its distance from the Sun, not the size of
its own orbit.

