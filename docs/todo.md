# Todo

**next id: 82**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#81 Move the read method from `BaseFileHandler` to `BasicFileReader`.

#80 Add a `TypeToken` overload to `BaseFileHandler.readFile` — `<T> T readFile(String path,
TypeToken<T> typeToken)`, with matching `BasicFileReader.readJsonFile` overloads — so a caller
reads `List<PageData>` without a cast or `@SuppressWarnings`. Gson 2.13.2 has
`fromJson(Reader, TypeToken<T>)`.

#77 Give Claude access to FullHost. Two settings in the Claude cloud environment (title-bar
environment menu → Edit): allow `app.ca-west.oncoregrid.ca` under Network access, and add a
FullHost API access token (created in the FullHost dashboard) as a secret. Claude then drives the
FullHost API — stop/start, nodes, variables, builds — asking before each action.

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

