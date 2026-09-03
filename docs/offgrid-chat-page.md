# offgrid — chat page, first pass

Session notes, 2026-09-02. The first pass is built; the reasoning is in
`docs/decisions.md` under "The chat page, and the REST controller it arrives on".

Repo: `PierreCote-57/offgrid`. The GettingLost branch this session was handed
(`claude/afgren-chat-window-k9b1iw`) is the wrong repo for this work — point Claude at
offgrid before anything is written.

## What is being built

A page with a chat window: type, it posts to the server, the server answers, the exchange
renders. **First pass answers by repeating the last message back.** The point of the pass is
the round trip, not the answer.

Long term the answer comes from an LLM with access to the site's own tools.

## Settled

1. **Page under Info** — `/info/chat`. `OffgridWebController.info()` already maps
   `/info/{name}`, so the page needs no Java at all: a template and its JSON, plus one `<li>`
   in the menu.
2. **`static/js/chat.js`** — `Alpine.data("chat", …)`, listed in `header.html` *after*
   `alpine.min.js`. Matches `offgrid.js` / `browser.js`, and keeps the template to markup
   only, which is what `decisions.md` says a page is.
3. **The echo shows the server's timing** — the bubble repeats the text with `durationText`
   small underneath. `RestBaseAnswer` already carries it, so it costs nothing, and it is the
   visible proof the reply came from the server rather than from the page. Without it a JS
   bug that never calls the server looks exactly like success.
4. **`OffgridRestController`, peer of `OffgridWebController`** in `spring/web` — the site's REST
   controller, of which chat is the first endpoint. Not a chat-specific class.
5. **`OffgridRestProcessor`, peer of `OffgridWebProcessor`** — backs the REST controller.
6. **The whole transcript on the wire**, `{"messageList":[{"role","text"},…]}`, server
   stateless. `role` is an enum carrying its wire spelling on `@JsonProperty`.

## Files the first pass touches

| Path | What |
| --- | --- |
| `resources/data/info/chat/chat.json` | `{"name": "Chat"}` — same shape as `info/about/about.json`. `readPageJson` needs it or the page 500s |
| `resources/templates/info/chat.html` | header include, `<main>` with the chat block, footer include |
| `resources/templates/fragments/site/menu.html` | one `<li>` in the Info submenu |
| `resources/static/js/chat.js` | the Alpine component — `messages[]`, `draft`, `busy`, `send()` |
| `resources/templates/fragments/site/header.html` | `chat.js` in the script list |
| `resources/static/css/site.css` | `.gl-chat-log`, bubbles, input row |
| `java/…/spring/web/OffgridRestController.java` | `@RestController`, one `@PostMapping`, body delegates through `BaseRestController.processRequest` |
| `java/…/pojo/chat/ChatRequest.java`, `ChatMessage.java`, `ChatAnswer.java` | `ChatAnswer extends RestBaseAnswer` |
| `java/…/spring/web/OffgridRestProcessor.java` | peer of `OffgridWebProcessor`, backs the REST controller |
| `docs/decisions.md` | why the REST controller is a peer, why the echo shows timing |

## Three things that will bite otherwise

**Script order.** `chat.js` has to be listed BEFORE `alpine.min.js` in `header.html`.
Alpine's last line is `queueMicrotask(() => Alpine.start())`, and microtasks drain between
deferred scripts, so `alpine:init` has already fired by the time any script listed after
Alpine executes. Listed after, `Alpine.data("chat", …)` never registers, `x-data="chat"`
resolves to nothing, and the page looks right with a dead Send button.

**Two serializers.** `@RestController` answers through Jackson, which is what
`RestBaseAnswer`'s `@JsonIgnore` already assumes. The processors read files with Gson. Both
stay; they do different jobs.

**The MCP server is not the way in.** `spring-ai-starter-mcp-server-webmvc` makes this app an
MCP *server* — it publishes `next-maintenance` and `worst-road` on `/mcp` for an outside
client like Claude Desktop. A chat page inside the same JVM does not go through that.
`NextMaintenanceTool` is a plain `@Component`; the chat autowires it and calls the method. So
"the chat gets my MCP tools" means the same tools wired in-process — no MCP client, no
loopback HTTP. Only matters at pass two, but it changes what pass one builds toward.

## Not a concern yet

Abuse and cost, because this runs on the local machine only. Both become real the day the
LLM lands and the app is on FullHost — same question `docs/todo.md` #42 already parks for
`/mcp`.
