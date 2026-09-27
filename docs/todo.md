# Todo

**next id: 75**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#74 Decide how `/mcp` is protected on FullHost. Locally a client reaches it over loopback and
nothing else can; on FullHost it is on the open internet, and a tool answers anyone who posts to
it. The question outlives the implementation — it has to be answered for whatever serves `/mcp`,
not for the server that was removed.

#73 Once the site is known to work well on FullHost, set
`spring.jackson.serialization.indent-output` to `false` in `application-host.yaml`. It is `true`
for now so a response is readable while the deploy is being debugged.

