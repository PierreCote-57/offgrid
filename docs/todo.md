# Todo

**next id: 53**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#1 FullHost. Three parts, one job:
 - The account is signed up but locked out. The password reset sends nothing to the address
   that verified it and there is no phone support; the way in is a guest ticket at
   https://manage.fullhost.com/submitticket.php or the sales form, both of which work without
   logging in. Worth asking whether the account was ever fully provisioned — a signup that
   stalled after email verification leaves no client record to reset against.
 - Deploy the skeleton, to prove their build node builds this repo and runs the jar while
   nothing is invested in it. Check what `getClass().getResource("/")` answers inside the
   packaged jar — `ResourceFileManager` builds its root from it and `pom.xml` names no
   packaging, so the build is a Boot fat jar while the IDE runs off a directory.
 - Get the images and the documents onto the server. `folder.local` names the app's own root
   on the machine, holding `images/`, `documents/` and `logs/`; the first two live outside
   the resource tree and have no delivery path, while JSON and HTML arrive by push and
   rebuild.

#4 Build the blog listing and wire the menu. `fragments/site/menu.html` still has
`href="#"` for Blog, while the six posts already link back to `/blog` and a post is served at
`/posts/{name}`. Everything else in the menu is wired and served.

#6 Answer the MCP tools and resources from the data. Spring AI serves `/mcp` now, and every
answer under `com.lc.offgrid.webapp.mcp` is hard-coded: the image list and the image itself, the
worst road in to a destination, and what the van and the Bronco are due for. The real
answers: the limiting leg that `Access.getRoadLimitingLeg` builds, and the earliest entry
still outstanding in `m-van.json` and `m-bronco.json`.

#7 Author `templates/hardware/howto/water.html` — it is still the placeholder text the port
left behind. Everything else that came from GettingLost is in.

#22 Make `/image/{imageName}` honour the `size` it is handed. `processImage` still answers the
native file whatever it is asked for — that call site is the whole of what is left in the
server. `ImageSize` now carries each size's box and knows how to produce one (`resize` writes a
file, `toResource` answers bytes, `Native` overrides both to leave the original alone), and
`ImageCompressor` does the work; see the two 2026-09-05 entries in `docs/decisions/`.

Open with it: on the fly or from disk. On the fly costs a full-resolution decode per request —
a 6000x4000 shot is about 96MB of heap held for the length of one request, and a gallery page
fires one request per thumbnail, concurrently. It also repeats on every reload, because
`makeResponseOk` sets the content type and no `Cache-Control` or `ETag`. Serving from disk is
what `resize` already writes.

Then, in the same feature: `open` ignores EXIF orientation, so a phone portrait is served
sideways; and the suffix form keeps the source extension while `toResource` always writes JPEG.

#42 Decide how `/mcp` is protected before it is deployed. Locally a client reaches it over
loopback and nothing else can; on FullHost it is on the open internet, and a tool answers
anyone who posts to it. The question outlives the implementation — it has to be answered for
whatever serves `/mcp`, not for the server that was removed.

#48 Build the info page "The sky above you" — the solar system chart with the rise/set table
below it. On hold as of 2026-09-01. The two skills in `docs/skills/` port to Java with no
runtime dependency on Claude: the shared core is the JPL element table, the Kepler solve and
`helio()`, with the rise/set version keeping the `z` the chart discards. The proposed split
is astronomy math under `com.lc.basics.tools.astronomy` (a `Body` enum carrying each planet's
twelve elements and its own horizon altitude) and the screen-coordinate work — orbit radii,
dot radii, label placement, arrowheads — under `com.lc.offgrid`, with the template drawing
what it is handed. Decided: the observer defaults to the 50th parallel marker in Campbell
River with `America/Vancouver`, and JS upgrades it from the browser — the timezone from
`Intl.DateTimeFormat().resolvedOptions().timeZone` synchronously, the coordinates from
`navigator.geolocation` behind its permission prompt, with `navigator.permissions.query` used
first so a visitor who already granted it is never prompted again. Open: chart rule 5 ends
with "look at the result and override where it reads badly", which the server cannot do, so
crowded dates can render two labels visually stacked.

#49 Fix `README.md`'s Layout table. It names the package
`src/main/java/com/logicielcote/offgrid`; the tree is `com/lc/offgrid`. The table also
predates `src/main/resources/data`, the profile yamls beside `application.properties`, and
the `folder.local` root holding `images/`, `documents/` and `logs/`. Read the whole table
against the tree rather than fixing the one row.

#50 Build the CLI. `com.lc.offgrid.OffgridApplicationCLI`, peer of `OffgridApplication`, a
`@SpringBootApplication` with `scanBasePackages` naming `common` and `cliapp`, setting
`WebApplicationType.NONE` and adding the `local` profile so `folder.local` resolves; the
working code goes in `cliapp` as a `@Component` with `ImageFileManager` constructor-injected,
which `main` takes off the context and calls. `cliapp` is an empty folder today. Neither
launcher's `scanBasePackages` is written yet — both still run on the default scan, which
reaches everything, so nothing is broken and nothing is separated either.

#51 Decide whether the lightbox stays at the Large box. The overlay is capped at 75vh, and
Large is the biggest thing served — a visitor paging a gallery with the arrow keys pays it per
step. Medium's box is the alternative.

#52 Point the lightbox's "Full size" link at the bare image URL. It asks for `?size=large`
today, while the pattern everywhere else is that no size means the original — which is what a
modified click on a gallery thumbnail already gets. Agreed 2026-09-04, not yet made. It leaves
Large with no caller, which is the subject of #51.
