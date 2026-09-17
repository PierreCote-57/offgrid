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

#42 Decide how `/mcp` is protected before it is deployed. Locally a client reaches it over
loopback and nothing else can; on FullHost it is on the open internet, and a tool answers
anyone who posts to it. The question outlives the implementation — it has to be answered for
whatever serves `/mcp`, not for the server that was removed.

#55 Convert the remaining `rem` lengths in `site.css` to px. `docs/decisions/look.md` rules
that all CSS lengths are px, and the conversion recorded there on 2026-08-25 left values
behind — run the count before deciding how big the pass is.

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
