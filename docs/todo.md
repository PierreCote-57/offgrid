# Todo

**next id: 84**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#83 Update the jar name in `README.md` (lines 19 and 23): the pom's `finalName` now names it
`offgrid-<version>-<yyyyMMdd-HHmmss>.jar`, and the version there was already stale (`0.0.1`).

#82 Use the `TypeToken` overloads of `BasicFileReader` in offgrid. `OffgridWebProcessor` reads
raw `List.class` in `processBrowserData` and raw `Map.class` in `hydratePageList`, each under
`@SuppressWarnings("unchecked")`; read them with a `TypeToken` and drop the suppressions.

#76 Add a `prod` deployment beside `test` (#75). Its own environment and its own Spring profile
`prod`, served on the real domain name (still to be chosen). Move the Maven build node into a
small environment of its own so either app environment can be stopped without blocking builds; one
Maven project per target — `main` → test, a `prod` branch (or release tag) → prod. `upload.sh
prod` needs a `fullhost-offgrid-prod` entry in `~/.ssh/config`.
`application-prod.yaml` sets `spring.jackson.serialization.indent-output` to `false`; test keeps
`true`.
Decide how `/mcp` is protected, on test and prod both. Locally a client reaches it over loopback
and nothing else can; on FullHost it is on the open internet, and a tool answers anyone who posts
to it. The question outlives the implementation — it has to be answered for whatever serves
`/mcp`, not for the server that was removed.
Waits on production pages: the home page throws until at least one destination is marked
`production`.

