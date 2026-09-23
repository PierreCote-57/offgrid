# Todo

**next id: 73**

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

#72 Finish the Claude message tools, then the input side of `chat()`. `MessageParser` wraps one
`Message` and hands out what is worth reading from it; the class itself says which reads are in.
What is left, in order:
 - A private method in `ClaudeManager` that helps build a `MessageCreateParams`.
 - Make `chat()` smarter with history and context. What of the transcript, the time zone and
   the coordinates goes in front of Claude is the author's decision, not the endpoint's.
 - An `.md` file sent to Claude with a chat request.
 - `getCost(Usage)` on `ClaudeModel`, the effective tokens priced in dollars.
 - Rename the `folder.local` folder `documents` to `document` (`LocalFileManager("/documents")`), locally and on FullHost.
