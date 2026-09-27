# Todo

**next id: 74**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#1 FullHost. Three parts, one job:
 - Deploy the skeleton, to prove their build node builds this repo and runs the jar while
   nothing is invested in it. Check what `getClass().getResource("/")` answers inside the
   packaged jar — `ResourceFileManager` builds its root from it and `pom.xml` names no
   packaging, so the build is a Boot fat jar while the IDE runs off a directory.
 - Get the images and the documents onto the server. `folder.local` names the app's own root
   on the machine, holding `images/`, `document/` and `logs/`; the first two live outside
   the resource tree and have no delivery path, while JSON and HTML arrive by push and
   rebuild.
 - Decide how `/mcp` is protected before it is deployed. Locally a client reaches it over
   loopback and nothing else can; on FullHost it is on the open internet, and a tool answers
   anyone who posts to it. The question outlives the implementation — it has to be answered for
   whatever serves `/mcp`, not for the server that was removed.

#73 Once the site is known to work well on FullHost, set
`spring.jackson.serialization.indent-output` to `false` in `application-host.yaml`. It is `true`
for now so a response is readable while the deploy is being debugged.

