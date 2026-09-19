# Todo

**next id: 70**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#1 FullHost. Four parts, one job:
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
 - Decide how `/mcp` is protected before it is deployed. Locally a client reaches it over
   loopback and nothing else can; on FullHost it is on the open internet, and a tool answers
   anyone who posts to it. The question outlives the implementation — it has to be answered for
   whatever serves `/mcp`, not for the server that was removed.

#6 Answer the MCP tools and resources from the data. Spring AI serves `/mcp` now, and every
answer under `com.lc.offgrid.webapp.mcp` is hard-coded: the image list and the image itself, the
worst road in to a destination, and what the van and the Bronco are due for. The real
answers: the limiting leg that `Access.getRoadLimitingLeg` builds, and the earliest entry
still outstanding in `m-van.json` and `m-bronco.json`.

#67 The `every-journey-has-a-first-step` excerpt ends in a literal `&hellip;`. `th:text` and
`browser.js`'s `escapeHtml` both render those eight characters as themselves, so the browser
cards already show them. Decide whether the entity comes out of the JSON or the excerpt is
rewritten to end on a word.

#68 Draw the three most recent blogs on the home page. `templates/index.html` does not call
`fragments/block/blog-list :: blogList`, which was built to be called from both. The fragment
takes no parameter and reads `blogList` off the model, so the home page's processor method
sets that attribute to the first three of `makeBlogList`.

#69 The date format `'MMMM d, yyyy'` is written in both `fragments/block/blog-list.html` and
`fragments/block/blog-header.html`. Collapse it into a `dateline(date)` fragment they both
call, so a post and its row can never disagree.

#70 `SkyBodyAnalyser.getSunAngle` and `HorizonsPosition.getSunAngle` return degrees; per §9.8 the
name should carry the unit. Rename both to `getSunAngleDeg`, which reaches outside the Sky* files.

#71 `parseTimeZone` in `OffgridUtil` now answers null where it used to answer the default zone.
Its javadoc still says it answers the marker's own, and its log line still says "using '<zone>'".
Rewrite both to state that it answers null and the caller decides. The `Instant` and
`ZonedDateTime` imports in that file are left over from `parseZonedDateTime` and are unused.
