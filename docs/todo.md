# Todo

**next id: 85**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#76 Add a `prod` deployment beside `test` (#75). Its own environment and its own Spring profile
`prod`, served on the real domain name (still to be chosen). Prod deploys a released jar
(`og-<version>`), test any pre-release — both by `DeployArchive` from the GitHub release.
`upload.sh prod` needs a `fullhost-offgrid-prod` entry in `~/.ssh/config`.
`application-prod.yaml` sets `spring.jackson.serialization.indent-output` to `false`; test keeps
`true`.
Decide how `/mcp` is protected, on test and prod both. Locally a client reaches it over loopback
and nothing else can; on FullHost it is on the open internet, and a tool answers anyone who posts
to it. The question outlives the implementation — it has to be answered for whatever serves
`/mcp`, not for the server that was removed.
Waits on production pages: the home page throws until at least one destination is marked
`production`.

