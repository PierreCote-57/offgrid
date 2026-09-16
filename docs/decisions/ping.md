# Decisions — Ping

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

The ping app: `com.lc.offgrid.pingapp`, its own `main`, its own folder under `folder.local`,
and `ping.sh` at the repo root. It owns nothing else.

## 2026-09-15 — A separate app, scanning only its own package

`OffgridApplicationPing` is a second Spring Boot `main` beside `OffgridApplicationCLI`, in
the `pingapp` package rather than beside it in `com.lc.offgrid`, because the app owns one
folder in the source tree and one under `folder.local` and nothing outside them.

`scanBasePackages` names `com.lc.offgrid.pingapp` alone — the app reads its own folder and
needs none of the site's beans. That narrows component scanning only. Auto-configuration is
selected from the classpath, so `spring-ai-starter-mcp-server-webmvc` still starts an MCP
server with an empty tool list and logs a WARN about it. It costs a few startup lines and
nothing else; `spring.ai.mcp.server.enabled=false` would silence it if it ever matters.

`PingFileManager` does **not** extend `LocalFileManager`. That base walks its folder into a
name→File map once at startup — a snapshot, which is the opposite of a host list re-read
every pass and a log appended to. It is a plain `@Component` with its own
`@Value("${folder.local}")`, the same idiom without the cataloguing.

## 2026-09-15 — What is measured: the network, not the hosts

The question the app answers is "can I talk to anything from here", so a host that stops
answering is noise and every host stopping at once is the signal.

One ping per pass, one host, drawn at random from the list each pass. An earlier version
pinged all twenty in parallel; it was dropped for load. A single silent minute is therefore
ambiguous by design — the draw moves on, and a dead link goes quiet across hosts while a
dead host does not.

The host list is a text file, re-read at the top of every pass so it can be edited while the
monitor runs. Blank lines and `#` comments are dropped. It is seeded on first use with the
gateway (read from `route -n get default`) plus ten resolver addresses and eight names.

**Half the seed is raw addresses on purpose**: a name needs DNS first, so a resolver outage
would otherwise read as a network outage.

**A host earns its place by answering, not by reputation.** amazon.com, netflix.com and
microsoft.com are all out — none of them answers ICMP. microsoft.com survived the first cut
because it was verified with a single packet that happened to come back; one packet is not
verification for a host that sits in the pool for months. As 1 of 20 in the draw it was
costing a permanent ~5% off every stay's success rate, scattered as single misses that never
looked like an outage.

## 2026-09-15 — Ping is time or failure, and that is the whole vocabulary

An HTTP version came first, with 200 / 504 / 0 for replied, timed out and never reached. It
was replaced by the OS `ping` command through `CommandExecutor`, and with a real ping there
is no middle: a reply carries a round trip or there is no reply. `PingResult` is therefore a
boolean and a double, no status code, and the log leaves `ms` empty rather than writing a
stand-in number a reader would have to know to discount.

`ping -c 1 -W 5000` — the flags are macOS, where `-W` is milliseconds. Linux reads it as
seconds, so running anywhere but here needs a branch. Success is read from `time=` in the
output, not from the exit code, because `CommandExecutor` does not expose one.

## 2026-09-15 — The monitor stops on Return, with no second thread

`System.in.available()` goes non-zero exactly when Return is pressed on a line-buffered
terminal, so the pass loop polls it in 250 ms slices and drains the bytes on a hit, which
keeps the newline out of the next menu prompt. The cost is that Return is noticed at a pass
boundary rather than instantly; 5 seconds was accepted for it.

This is also why `ping.sh` runs a plain `java` after `./mvnw compile` instead of
`spring-boot:run`, which forks its own JVM.

## 2026-09-15 — Statistics are reported per stay

A *stay* is a run of log rows. What ends it is the `PingReport` being printed.

**A hole in the rows does not end it.** A five-minute gap did, until an overnight run came
back as 40 rows for two networks: the Mac naps, so the same network split every sixteen
minutes at nothing the network did. The gap says the monitor was off, which is not an answer
to "how is the wifi here" — and that is the only question this report asks.

The same network reached twice is therefore one stay, and the Duration column spans the hours
the monitor was down. "How was the wifi at that place last Tuesday" is a per-visit question
and gets a report of its own.

**Oldest first.** Newest-first was the first instinct and it is wrong for a terminal: the
newest block would scroll off the top.

The wifi name is read fresh every pass, from `BasicTools.getWifiName()`, which is what makes
the grouping possible at all.

Outage runs — three or more consecutive misses — were built and then removed. Success
percentage is sufficient at this level; outage analysis is a different report.

**Answered, Pings and Success are three columns, not one.** The row is `printf` at a fixed
width, so the report pastes into Excel as three cells a formula can reach — `2,847`, `2,912`,
`97.8%` — where `2,847 / 2,912 (97.8%)` would arrive as one string to split first.

Duration is left-aligned, alone among the numbers, because `TimeUnits.MS.format` returns
ragged text: right-aligning lines up one row's `sec` with another's `min`.

## 2026-09-16 — The interval is chosen, and a report is an enum constant

The pass length is a `PingInterval`, asked for with `queryEnum` when Monitor starts rather than
held as a constant, because it is tuned by hand: a long stay wants the minute, a fault being
chased wants ten seconds.

A statistics report is a `PingReport` constant, and it carries both halves of what varies —
what ends a stay, and the columns the stays print in. Nothing else differs, so a third report
is a third constant.

**Overview** ends a stay when the wifi name changes, and leads with Network, Start, End,
Duration. **ByDay** ends it at midnight and leads with the date alone: no start, end or
duration, because the date already says when and a day is a day. ByDay's last column is every
network the day held, which is the one thing the day grouping loses and the only place it is
wanted.

Success, Failure, Total, Rate, Min and Max sit between the two, as one shared format string in
the same place in both. Rate is Success/Total; the only other reading would be its complement,
the packet loss `ping` itself prints. Min and Max are over the rows that answered, and a stay
nothing answered prints two blanks — the same reason the log leaves `ms` empty rather than
writing a stand-in.

`PingStay` holds its rows and nothing else. It answers the name it opened on and every name it
touched from them, rather than carrying a copy of what the rows already say.

Start and End are `HH:mm`. A stay is hours long, so the seconds were noise in the column.
