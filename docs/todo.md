# Todo

**next id: 67**

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

#48 Compute the rise, transit and set times. The page is built and served at `/info/sky`. The
positions come from JPL Horizons; the
three 2026-09-08 and 2026-09-09 entries in `docs/decisions/data.md` hold the file layout, the
class shapes and everything settled.

`SkyBodyAnalyser` in `common/misc/sky` computes all of it: built for a `ZonedDateTime` and a
latitude and longitude, it answers the chart's Sun angle and a `SkyBodyDay` per body, one body
at a time or as a map over the nine.

`sky.js` draws every block from the sky endpoints; the 2026-09-12 and 2026-09-15 entries in
`docs/decisions/site.md` and `apis.md` state how. #61 holds what the answer still has to carry.

One more that belongs here rather than in the implementation: the constellation is carried as
the three-letter code, and turning it into a name a reader knows is still to do.

#49 Fix `README.md`'s Layout table. It names the package
`src/main/java/com/logicielcote/offgrid`; the tree is `com/lc/offgrid`. The table also
predates `src/main/resources/data`, the profile yamls beside `application.properties`, and
the `folder.local` root holding `images/`, `documents/` and `logs/`. Read the whole table
against the tree rather than fixing the one row.

#51 Decide whether the lightbox stays at the Large box. The overlay is capped at 75vh, and
Large is the biggest thing served — a visitor paging a gallery with the arrow keys pays it per
step. Medium's box is the alternative. Since the "Full size" link went to the bare URL on
2026-09-06, nothing asks for Large at all.

#53 Low priority. Give a crowded chart label somewhere to go. The server's chart tried eight
positions and took the first that cleared; on days when two planets are in conjunction as seen
from above, none clears and the two texts print on top of each other. `sky.js` draws the chart
now and tries none of them, so the labels overlap on those days rather than moving. A sweep of 2026-01-01 to
2028-12-31 hit it on 60 days of 1096, longest run 3 to 23 February 2027 — Mercury/Venus and
Earth/Mars account for most of them. 10 February 2027 is a date to draw when checking it.

It is a width problem, not a placement one: the same sweep at a 1024 canvas fails on no day at
all, because the orbits scale and the 12px labels do not — draw one date at both widths to see
it.

#55 Convert the remaining `rem` lengths in `site.css` to px. `docs/decisions/look.md` rules
that all CSS lengths are px, and the conversion recorded there on 2026-08-25 left values
behind — run the count before deciding how big the pass is.

#56 Work the sky table's columns once #48 lands. The widths in `NARROW_COLUMN_LIST` and
`WIDE_COLUMN_LIST` in `sky.js` were eyeballed off the placeholder times the server used to send,
so the Transit cell is sized for a placeholder's shape rather than a computed one. Re-size them
against real values, and decide then what each column shows.

#57 Keep the sky date's transparent input out of the tab order without breaking dismissal.
`.og-sky-date-input` in `site.css` is 1px and transparent, and it has to stay focusable and take
pointer events or Safari will not close the calendar on Escape. Today that costs a tab stop on
something invisible: a keyboard user tabs to the button, then to nothing they can see.

#59 Decide where `normalise` and `clamp` live. `HorizonsEphemeris` and `SkyBodyAnalyser` now each
hold a private copy: the ephemeris needs them for the hour angle and the elevation, the analyser
for the ecliptic angle and the lit fraction. Two five-line helpers, duplicated because nothing in
`com.lc.basics.tools.math` offers them and `astronomy/planet` may not be reached into for utilities.

#60 Apply the rise and set offset for the Sun and the Moon. `SkyBodyAnalyser` crosses
`HORIZON_ELEVATION`, which is a point at geometric zero: an almanac's sunrise carries refraction
and half a disc with it, so the two differ by minutes at the 50th parallel. Until this is in, a
test cannot assert an almanac time for a rise or a set — transit is unaffected.

#66 State what range the magnitude strip covers, or widen it. `static/images/magnitude-scale.svg`
runs 1.0 to 4.0, and the table it explains holds the Sun at about -26 and Neptune at +7.8, so
every body on the page but the faintest is off the end of it. Either the strip grows to the range
the rows actually show, or the footnote says it is a sample of the scale and not the whole of it.
