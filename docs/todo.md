# Todo

**next id: 48**

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

#6 Design the tools the MCP endpoint offers, and answer them from the data. The endpoint,
the protocol and the session handling are in; the only tools behind it are `hello` and
`destination-count`, and the count is a stated 25 rather than a count of anything.

#7 Author `templates/hardware/howto/water.html` — it is still the placeholder text the port
left behind. Everything else that came from GettingLost is in.

#17 The `photo-gallery` port dropped the gallery heading. GettingLost's `photoGallery`
renderer in `gettinglost.jst` treats `photoGalleries.<key>.name` as required, builds the
`gl-heading` from it, and can wrap heading and grid in `<details><summary>` itself. The
offgrid fragment renders tiles only, so `templates/hardware/van.html` and
`templates/hardware/bronco.html` type the same words into their own `<summary>` and nothing
reads the JSON's `name`. The JSON was always the source.

#22 A thumbnail and its lightbox load the same file. GettingLost split them — a small
Photon URL for the grid, the 1920 cap for the overlay — and offgrid has one URL per image,
so the grid pulls full-size originals. This is where a resize seam goes.

#25 The maintenance work sheets have no files. `/document/{documentName}` is served now, off
`LocalFileManager` (`<folder.local>/Documents`). `workUrl` in
`resources/data/hardware/maintenance/van/m-van.json` and `.../bronco/m-bronco.json` names
three PDFs; none of them is in that folder yet.

#42 Decide how `/mcp` is protected before it is deployed. Locally the client reaches it over
loopback and nothing else can; on FullHost it is on the open internet, unauthenticated, and
every tool answers anyone who posts to it.

#43 Give `Leg` a `LegType` enum beside its String. The String stays as the JSON writes it, so
an unlisted surface is never lost; the enum is a non-JSON member with a getter, null until the
first call, and `UNKNOWN` for a word the vocabulary does not carry. Members are `ROAD_RANK`'s
five, `NON_DRIVE_LEG_TYPES`' three, `PAVEMENT` and `BACK_COUNTRY` — the last two because
`Access.getRoadLimitingLeg` builds legs of those types itself and no data file names them.

#45 Finish the request id in `McpMessage`. It is an `Object`, and MCP's own Gson reads an
integral one as a Long, so a client's `7` is answered as `7`. Two ends are not covered:
`McpAnswer.id` is null on a parse error and Gson drops a null field unless the builder says
`serializeNulls()`, which would then write `"error": null` on every successful answer; and
nothing rejects an id that is neither a string nor a number, which the specification forbids.

#46 Answer 400 to an `MCP-Protocol-Version` this server does not speak. The 2025-06-18
transport has the client send that header on every request after initialize, and has the
server refuse a version it does not support. `McpProcessor.processMessage` passes it into
`McpSession.markUsed` and never looks at it, so a client on a revision we do not answer is
served as though it were on ours.

#47 Nothing ever drops an MCP session. `McpProcessor.getSessionMap()` grows one entry per
initialize and loses one only when the client sends DELETE, which a client that crashes or
walks out of signal never does. `McpSession` already records `getTimeUsedMS`, so what is
missing is who sweeps, how often, and how long a session with no stream on it is kept.
