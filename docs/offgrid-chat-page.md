# offgrid — chat page, first pass

Session notes, 2026-09-02. Nothing built yet. Drop into `docs/todo.md` as entry **#49**
(next id in that file is 49) or keep as scratch.

Repo: `PierreCote-57/offgrid`. The GettingLost branch this session was handed
(`claude/afgren-chat-window-k9b1iw`) is the wrong repo for this work — point Claude at
offgrid before anything is written.

## What is being built

A page with a chat window: type, it posts to the server, the server answers, the exchange
renders. **First pass answers by repeating the last message back.** The point of the pass is
the round trip, not the answer.

Long term the answer comes from an LLM with access to the site's own tools.

## Settled

1. **Page under Info** — `/info/chat`. `OffgridController.info()` already maps
   `/info/{name}`, so the page needs no Java at all: a template and its JSON, plus one `<li>`
   in the menu.
2. **`static/js/chat.js`** — `Alpine.data("chat", …)`, listed in `header.html` *after*
   `alpine.min.js`. Matches `offgrid.js` / `browser.js`, and keeps the template to markup
   only, which is what `decisions.md` says a page is.
3. **The echo shows the server's timing** — the bubble repeats the text with `durationText`
   small underneath. `RestBaseAnswer` already carries it, so it costs nothing, and it is the
   visible proof the reply came from the server rather than from the page. Without it a JS
   bug that never calls the server looks exactly like success.
4. **`OffgridRestController`, peer of `OffgridController`** in `spring/web` — the site's REST
   controller, of which chat is the first endpoint. Not a chat-specific class.

## Open

- **The wire contract.** Whole transcript each turn (`{"messages":[{role,text},…]}`, server
  stateless, echo ignores all but the last, pass two hands the same array to the model) or
  just the last message (`{"message":"…"}`, smallest thing that works, but pass two then needs
  either a contract change or server-side session state). Parked deliberately — decide at the
  end, once the rest is settled.
- **What backs `OffgridRestController`.** A new `OffgridRestProcessor` peer, or
  `OffgridProcessor` gains the method. The existing processor already answers non-view things
  (`processImage`, `processBrowserData`), so REST work is not foreign to it.

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
| `java/…/spring/web/ChatRequest.java`, `ChatAnswer.java` | `ChatAnswer extends RestBaseAnswer` |
| the processor | per the open question above |
| `docs/decisions.md` | why the REST controller is a peer, why the echo shows timing |

## Three things that will bite otherwise

**Script order.** `alpine.min.js` and `chat.js` are both `defer`, so they execute in document
order, both before `DOMContentLoaded`. That means `document.addEventListener("alpine:init", …)`
in `chat.js` works — but only while `chat.js` is listed after Alpine in `header.html`.

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
