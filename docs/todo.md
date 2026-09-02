# Todo

**next id: 49**

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
   nothing is invested in it.
 - Get the image folder onto the server. The folder `folder.image` names lives outside the
   resource tree and has no delivery path; JSON and HTML arrive by push and rebuild.

#4 Build the blog listing and wire the menu. `fragments/site/menu.html` still has
`href="#"` for Blog, while the six posts already link back to `/blog` and a post is served at
`/posts/{name}`. Everything else in the menu is wired and served.

#6 Answer the MCP tools and resources from the data. Spring AI serves `/mcp` now, and every
answer under `com.lc.offgrid.mcp` is hard-coded: the image list and the image itself, the
worst road in to a destination, and what the van and the Bronco are due for. Each one names
in its javadoc where the real answer comes from.

#7 Author `templates/hardware/howto/water.html` — it is still the placeholder text the port
left behind. Everything else that came from GettingLost is in.

#22 A thumbnail and its lightbox load the same file. GettingLost split them — a small
Photon URL for the grid, the 1920 cap for the overlay — and offgrid has one URL per image,
so the grid pulls full-size originals. This is where a resize seam goes.

#25 The maintenance work sheets have no files. `/document/{documentName}` is served now, off
`LocalFileManager` (`<folder.local>/Documents`). `workUrl` in
`resources/data/hardware/maintenance/van/m-van.json` and `.../bronco/m-bronco.json` names
three PDFs; none of them is in that folder yet.

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
