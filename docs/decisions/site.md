# Decisions — Site

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

Templates, fragments, routes, the link rule, maps and the browser page.

## 2026-08-24 — Template layout

Pages live at `templates/*.html`. Fragments live under `templates/fragments/`, one fragment
per file, so the tree says which is which and no file accumulates unrelated fragments.

Two fragment folders, split on who decides the markup appears:

- `fragments/site/` — chrome every page gets whether or not it asked for it: header, menu,
  footer.
- `fragments/block/` — content a page deliberately places inside itself.

The day that boundary blurs is the day the split stops helping, so it is written down here
rather than left to be inferred from the folder names.

A `site/` fragment is cut on where it lands in the page, not on what kind of markup it
holds, so `header` carries the `<head>` element as well as the header bar — everything above
`<main>`. `menu` is the exception, and it is cut for editing: the nav is the part that
changes, and it should not sit under a script list nobody touches.

A page is therefore its own content and nothing else — doctype, `<html>`, the header
include, its `<main>`, the footer include:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<div th:replace="~{fragments/site/header}"></div>

<main>
	<h1 th:text="${PageName}">About</h1>
</main>

<div th:replace="~{fragments/site/footer}"></div>
</html>
```

`<body>` opens at the end of `header.html` and closes at the end of `footer.html`, so no
page carries either tag and the two files are deliberately unbalanced. Thymeleaf's model is
an event sequence rather than an element tree, so an open tag with no close passes straight
through. The price is that an HTML tool reading any one of these files alone — IntelliJ
included — reports the missing partner.

The host tag on an include is a placeholder and nothing else: `th:replace` discards it and
emits the fragment in its place, so `<div>` renders identically to any other name.

Every script the site has is loaded on every page. Each one is inert where it is not used —
`gl-constants.js` and `google-map.js` only register on `window.GL`, and `lightbox.js` and
`browser.js` return immediately when the element they look for is absent. So there is one
script list, in one file, and a page that adds a block needing a script declares nothing.

One fragment per file means a fragment that takes nothing needs no `:: name` selector — a
fragment expression with no `::` includes the whole template, so `~{fragments/site/header}`
is the whole include. A fragment that takes a parameter has to name itself, because that is
where the arguments go: `~{fragments/block/photo-gallery :: gallery('our-van')}`. Which of
the two applies is decided further down, under the block-fragment rule.

## 2026-08-25 — A URL is the view name

`/info/useful-links` renders `templates/info/useful-links.html`, whose data folder is
`data/info/useful-links/`. The URL, the view name and the mirror path are the same string,
so adding a page is a template, a `@GetMapping` and a `process*` method — nothing else to
keep in step. Where a parameterized route already covers the shape, it is a template and its
folder, and there is no Java to write at all.

The three Info pages are the first to use it. `/info/about` is a heading, the header and the
footer, and nothing else; the two Useful pages render their `noteMap`.

The segment was `about` when this was written and became `info` the same day, folder and
route together — the rule is what matters here, and it did not change.

## 2026-08-25 — A block fragment takes a parameter when the page has to say which

Two shapes, and what decides between them is whether the part is rendered as a whole or its
members are placed individually.

**Rendered as a whole — the fragment reads the model.** A `noteList` is every section, in
order, in one place; nothing renders a single section on its own. The page puts the data on
the model under the part's own name and includes the fragment bare —
`th:replace="~{fragments/block/note-list}"`, no `::` selector, no `th:with`. The attribute
name is the contract: a page that wants the note-list block sets `noteList`. Same contract
`fragments/site/header.html` already uses for `${UserMessageList}`.

**Placed individually — the page names which one, and the fragment takes it.** A `googleMap`
is a list of maps that appear at different points on the page: a campground map at the top
showing the site against the lake, a map of the road in at the bottom. `photoGalleries` is
the same — the van page carries `our-van` partway down and `listing-pictures` near the end.
The name is resolved in the page's HTML, so the page is what says where each one goes.

**The name is always given, even when the collection holds exactly one entry.** There is no
falling back to "the only one" — a page that later grows a second map would silently change
meaning without being edited.

This replaces the rule as first written earlier the same day, that a block fragment never
takes parameters. That was drawn from `noteList`, which was the only block that existed, and
it does not survive the first part whose members are addressed one at a time.

One fragment for the whole `noteList` block, not one per section. It splits the day
something renders a section on its own.

**Superseded 2026-09-01.** That day came: `note-list` takes the block's name, like
`googleMap` and `photoGalleries` — see *A keyed part is a Map* in [data.md](data.md).
Nothing on this page is rendered as a whole from the model any more except
`maintenance-actual`.

A description is a `List<String>` joined into one flowing cell, and it is rendered with
`th:utext`. **Corrected 2026-08-26** — it had been one `<p>` per entry, escaped. Both were
regressions against the GettingLost renderer this replaced, which joins the lines as a soft
word separator and assigns `innerHTML`: a line in the data is where the author wrapped the
JSON, not a break on the page, and the author writes `<b>` or `<a>` in the string and means
it. Escaping the description took that away for nothing.

The one thing that does not survive the move: a block span written into a description — a
photoRef, say — no longer expands. GettingLost's dispatcher made another pass over the
elements a renderer emitted, and Thymeleaf renders once.

## 2026-08-25 — Checklist pages: one route, a processor for the rows

`/hardware/checklists/{name}` is one `@GetMapping` for the whole folder, not one per page.
The name is the view name and the mirror folder both, so a new checklist is a template plus
its data folder and nothing else. Per-page methods were the alternative and buy nothing here
— the pages differ only in their content. The same shape is expected to hold wherever a
folder holds interchangeable pages.

The URL carries no `.html`. Every other route on the site is extensionless and the
suffix would have made checklists the odd ones out; Spring Boot 4 does no suffix matching,
so an extension would have had to be written into the mapping literally.

**The `<li>` stays bare.** A checklist line is authored `<li>Shut off the engine</li>` and
nothing else. `ChecklistElementProcessor`, an `IElementModelProcessor` on `<ol>`/`<ul>`,
wraps each row's content in `<label><input type="checkbox"><span>` as the template renders.

Two alternatives were weighed and dropped:

- **CSS alone cannot do it.** `::before` draws a box, but there is no toggle state to hang
  `:checked` on without a real `<input>` in the DOM.
- **One tag per line** — authoring `<li><input type="checkbox"> text</li>` and striking the
  row with `li:has(input:checked)` — works and needs no Java, but only the box toggles.
  Clicking the words does nothing, which is most of the target on a phone.

Carrying the markup in the authoring was also rejected: it puts the same three tags on every
line of every checklist, where the processor states the rule once.

`gl-checklist` and `gl-numcheck` keep their GettingLost names. The `gl-` prefix exists there
to namespace against the WordPress theme and offgrid has no theme to collide with, so it
buys nothing on its own — but pages copied across paste in untouched, now and later, and a
rename would have to be applied to every one on the way in. Revisit when copying stops.

The class on the list is what selects the variant, so it stays on the element and the
processor reads it: `gl-checklist` is a plain checkbox row, `gl-numcheck` adds the counter
span. One processor with two looks rather than two processors.

**The processor has to be idempotent, or it recurses until the stack runs out.** Thymeleaf
hands a model processor's own output straight back to it — `ProcessorTemplateHandler` sets
`modelAfterProcessable = true` whenever the processor changed anything — and the rewritten
list still carries `gl-checklist`, so it matches again. The engine stops only when a pass
leaves the model untouched (`gatheredModel.sameAs(processedModel)`), so the processor skips
any list whose rows already hold a checkbox. Every checklist is therefore walked twice: once
to wrap, once to find nothing to do.

An authored `<input>` is not enough to skip on. GettingLost writes write-on blanks as bare
`<input size=...>`, so the test reads `type="checkbox"` specifically.

Matching a dialect attribute instead would have avoided the second pass — the engine strips
the matched attribute before handing the model back, so it cannot match twice. It was not
taken because it puts a second marker on every list next to the class that already says the
same thing.

Checked state is not stored anywhere. A reload clears every box, which is what a
one-campsite-at-a-time list wants.

**`photoGallery`, `photoRef` and `backToGallery` render placeholders.** All three are block
fragments that say they are unavailable and read nothing. Images are deferred with the rest
of the image question, and the back link has no list page to return to yet.

## 2026-08-25 — The hardware pages, and the photo blocks

`/hardware/{name}` serves the van and the Bronco. Two singleton pages rather than a folder of
interchangeable ones, but the shape is identical — template plus data folder — so they get
the parameterized route too. It sits above `/hardware/checklists/{name}`, which is a segment
deeper and does not collide.

The van page is `van`, not `van-overview`: its data folder came across as `data/hardware/van/`
and the mirror decides the name.

**`photo` is inserted, not replaced.** The page owns the box — the van sets
`width:100%;float:left` on the block element itself — so `th:insert` keeps the authored
element and puts the picture inside it. `th:replace` would drop the geometry the author
wrote. The defaults for a page that sets nothing live in `.gl-photo` in the stylesheet, not
in code that inspects what the author already set.

**`photoGallery` takes the gallery name and renders a grid of captioned tiles — nothing
else.** Whether a gallery has a heading above it, and whether it sits inside a `<details>`,
is the page's HTML, not the fragment's. A `collapsible` flag was tried first and removed:
structure is the author's, the same rule the rest of the site runs on, and a fragment that
emits its own `<details>` leaves the page unable to see the structure it is producing.

The consequence: the heading is authored in the page rather than taken from the gallery's
`name` in the JSON, so a gallery that has a heading has it written in two places.

**Corrected 2026-09-01.** The gallery's `name` is gone from the JSON entirely — the page was
the only place a heading was ever written, and the field nothing read went with the class.

**An image slot renders its filename.** Until #9 settles there is no URL to serve an image
from, so every photo and every thumbnail shows the filename it wanted. That is the diagnostic
form — it says which file is missing rather than leaving a hole — and it disappears the day
images have a path.

**Superseded 2026-08-25.** Images have a path. All three fragments emit real `<img>` tags —
see *Images live outside the resource tree* in
[local-files.md](local-files.md).

## 2026-08-25 — The whole page data goes into the model

A handler puts `pageData` in the model, not a field at a time. `model.addAttribute("noteMap",
pageData.getNoteMap())` writes *which fields a page has* in a second place, so adding a field
means editing Java to expose something the template already had in hand.

The honest size of the win: three naming sites become two. The template names the field
either way; only the controller's copy disappears.

The argument that did **not** decide it, recorded so it is not made again: a shared fragment
needing one known attribute name. Header and footer data is cross-cutting and arrives on its
own channel whatever a handler puts in the model, so it says nothing about how much of the
page's own data travels.

## 2026-08-26 — A folder per kind of page, and the file does not repeat it

`templates/hardware/howto/awning.html` at `/hardware/howto/awning`, not
`howto/howto-awning.html` at `/hardware/howto/howto-awning`. The folder says what kind of
page it is, so the file saying it again is stutter. Ten files renamed — six howto, two
maintenance, two checklists — and each data folder with the JSON inside it follows the
template name: `data/hardware/howto/awning/awning.json`.

One `@GetMapping` and one processor method per folder, the shape `processChecklist` already
had. **The mapping method is the class choice.** `readFile` has to name the class it
deserializes into, and a maintenance page is not a `PageData`; a method reached only from
`/hardware/maintenance/{name}` knows that statically, with no dispatch table.

**Rejected — one generic `/hardware/{folder}/{name}`.** It cannot name a class. The folder
arrives as a string, so the read call would need a folder-to-class map, which is the same
switch written somewhere less obvious.

**Rejected — flattening the folders away.** `processHardware` reads
`data/hardware/{name}/{name}.json`, so a flat `van-maintenance.html` would have needed no
new code at all. It lands the same problem on `processHardware`, which would then pick the
class by testing the name for a `-maintenance` suffix. Sniffing a filename for a type is the
cost, and the tree stops saying what a page is.

`hardware/van` and `hardware/maintenance/van` are two different pages both named van. That
is the folder doing its job.

## 2026-08-26 — Posts, and the map that does not belong on one

The six posts are converted. `.gl-post` — the lined paper written in the Texture pass and
until now used by nothing — is what the body sits on, and the post's `date`, carried in all
six JSON files and rendered by none of them, is a dateline in the brand's hand directly under
the title.

**No `googleMap` on a post.** Every WordPress post opened with one, and every GettingLost post
JSON carried `googleMap.where`; the block is gone from all six templates and the data was not
brought over. A post that needs to put its subject on a map links to the destination page,
which is the page that owns the map. So `googleMap` stays on `DestinationPage` and is not
promoted to `PageData`.

Links to pages that do not exist yet are written as the real `<a th:href="@{…}">` they will
be — the three destination links, and the "← All posts" link that used to point at
`gettinglostonvi.wpcomstaging.com`. A link that is removed because its target is missing is a
link nobody restores when the target arrives.

`photo-gallery` now puts `id="gl-photo-<gallery>-<itemId>"` on each figure. The ids were in
the JSON all along and the fragment dropped them, which left the picnic post's link to its
own photo pointing at nothing.

## 2026-08-26 — The browser page

The GettingLost list browser is here, renamed `browser` throughout: one page, two independent
axes — display (table/grid/map) x data — and the URL query string is the whole state. Every
control commits by navigating to a new URL, so the page renders from scratch and Back/Forward
work with no history machinery to maintain.

**Four calls, each one file.** The page at `/shared/browser`; `static/js/browser.js`;
`/shared/browser/datasets.json`, a static file the browser reads to resolve `?dataset=` itself;
and `/shared/browser/data/{id}`, which answers that dataset's rows, resolving each `{file}`
pointer against the page JSON it names on the way out. Nothing is woven into the
page's HTML on the way out — no Java class has to mix the content of two files, and each trip
matches the data design. Embedding the definition list into the page was proposed and dropped:
the only thing it bought was a saved read.

**The browser resolves the id, the server resolves the file.** `datasets.json` keeps its `file`
field for `findDataset` to map an id to a filename; the browser stops reading it. An unknown id
is a 404 from the data call and an error line on the page from the definition list.

**A `{id}` mapping would shadow the static file.** `/shared/browser/datasets.json` would reach
the controller as `id = "datasets.json"`, because a controller mapping outranks Spring's static
resource handler — so the dynamic call took a path segment of its own, `data/{id}`, and the
definition list stayed in the browser page's own folder.

**Counts are counted in the browser.** GettingLost's sync step wrote a `counts` block onto each
dataset entry, which a hand-authored `datasets.json` cannot carry without being maintained by
hand. `collectCounts` walks the rows the data call just delivered — the UNFILTERED ones, since a
vocabulary derived from the filtered list deletes the choices you need to widen the search next.

**A card is a link, so the grid is the one view that narrows** — to rows with a `file`.
`isPublished` and `fileToSlug` did not come over: a row carries its own href, and `pageHref` is
the one place a card, a map pin and the table's View link read it, so they cannot disagree.

`gl-constants.js` holds the vocabularies — `TAG_COLORS`, `DESTINATION_TYPES`, `LINK_TYPES`,
`ROAD_COLORS`, `ROAD_RANK`, `NON_DRIVE_LEG_TYPES`, `MAP_CONFIG`, `PIN_ICONS` — because the
browser is the only reader and putting them in Java would mean inventing a way to ship them
out again. The map view builds the real `mapObject` and hands it to `google-map.js`.

**The table does not break out of the page column.** GettingLost forced `width:1180px` with
negative side margins to escape a 900px content cap; `main` here is 1024 wide, which leaves 976
for a table whose fixed columns want 875. The wrapper keeps `overflow-x: auto` so a narrow
viewport scrolls the table instead of the page.

The two booklet PDFs moved to `static/shared/browser/`, next to `datasets.json`: the booklet
button is a download the visitor's browser fetches, so the file has to have a URL.

## 2026-08-26 — The tab says which page, and which machine

Every page's `<title>` was the literal `offgrid`, in all 24 templates, so a window of Safari
tabs read the same word 24 times. The title is now `${PageName} — ${SiteName}`.

**The page name comes FIRST.** A tab keeps the front of a title and truncates the rest, so a
title that opens with the site name identifies nothing. The site name still earns its place at
the end, where bookmarks, history and search results show it.

**`SiteName` is a new thing, not `WelcomeMessage`.** The header's brand text is a greeting and
happens to read like an identity; they are not the same string and were not merged.
`BaseWebProcessor.siteName` is a property like the other three, and `processDefault` puts it on
every model.

**`application-local.yaml` overrides it to `β - Going offgrid`.** The marker goes in front for
the same reason the page name does: at the end it is the first thing a tab drops. The tab now
says which machine you are looking at before you read anything else.

## 2026-08-26 — The howto pages are real pages

The six `hardware/howto` templates were still raw WordPress fragments — no page skeleton, and
every block written as a `data-block-type` marker for a renderer that does not exist here. They
are now full pages like the checklists: header, `<main>`, footer, and the Thymeleaf fragments
`photo-ref`, `photo-gallery`, `photo`, `warning`, `note-list` and `back-to-gallery`.

The `pageLink` block became the real `<a th:href="@{/hardware/checklists/arriving-campsite}">`
it will be, following the rule the posts pass set.

**Step lists carry `gl-numcheck`, enumerations stay plain `<ol>`.** `dump.html` had already made
that choice; awning, climate and water follow it. The lists in battery and power enumerate
things rather than tell you to do them in order, so they are not checkboxes.

## 2026-08-28 — The back link is built on the server

`Referer` is on the HTTP request, so the server knows where the visitor came from and the
link is in the page as it arrives. `paintBackLink`, `browserReferrer`, `galleryName`,
`BROWSER_PATH` and `GALLERY_NAMES` are gone from `offgrid.js`.

The rule it came from: the reason for putting work in the browser has to be something the
server genuinely cannot know. The referrer was never that.

Two model attributes carry it, `backQuery` and `PageTitle` — the query the gallery is
restored with, and what that gallery is called. The path is constant, so the fragment writes
it. The fragment takes no parameter: a page is reached from one gallery, so there is never a
second back link to name.

`GALLERY_NAMES` duplicated the `title` of every entry in `datasets.json`, because a page that
is not the gallery had no reason to fetch that file for three words. On the server the titles
are already being read, so the copy has no purpose left.

## 2026-08-28 — Drawing a map: one drawer, several builders

**`drawMap(box, mapObject)` is the whole interface**, defined in `google-map.js` —
renamed from `map.js` so it pairs with `google-map.html` the way `browser.js` pairs with
`browser.html`.

**No scanner.** Nothing sweeps the document for map boxes. `google-map.html` emits, per
`map()` call, a script whose own `DOMContentLoaded` listener builds that map's `mapObject`
and calls `drawMap` — so a page with two maps has two independent scripts and nothing
coordinating them. Thymeleaf already holds the map entry when it writes the block; a scanner
would only put that in an attribute for something else to read back and re-parse.

**Two concepts that were being confused as one.** Drawing is global and lives once;
*preparing* a `mapObject` is local and lives wherever the data happens to be. The fragment
builds one out of the page JSON at render time; `browser.js` builds one out of hydrated rows
after a fetch, which is why it cannot carry an inline script and calls `drawMap` directly.
Same function, same parameter, two builders with nothing in common.

**No `galleries` parameter.** GettingLost passed `photoGalleries` in because a pin's `img`
could be the reference `"galleryKey/itemId"`, resolved by `resolvePinMedia` *inside* the
drawer. Offgrid's pins carry a bare name (`"img": "IMG_0499"`), and the builder turns it into
a `/image/` URL before the pin ever reaches `drawMap` — the same way `url` arrives as a ready
href rather than a reference. Resolution belongs to whoever builds the mapObject.

**The entry crosses in the script, not on the div** (2026-08-29). `google-map.html` writes
`th:inline="javascript"` and reads the entry as `[[${mapEntry}]]`, so Thymeleaf's own
JavaScript inlining serializes it and no attribute carries JSON. Two consequences of that
serializer, both handled in the builder: it spells a `MapIcon` by its enum constant, so `icon`
is lower-cased on the way into a pin; and it knows nothing of `/image/`, so the entry's
`"IMG_0499"` becomes the URL there too. The script finds its own box by id — the div is
`gl-map-<mapName>` — and the render function is `window["renderMap" + mapName]`, so two maps
on a page never collide.

**The page states the map's size on its own div, and keeps it with `th:insert`** (2026-08-29).
Google fills the container it is given, so some box has to state a width and a height. The
page writes `<div style="width:47%;height:200px;float:right" th:insert="…map('road')">`, and
`.gl-mapbox` is `width/height:100%` so the fragment's div fills that box. `th:replace` would
discard the div and the size with it. Nothing is passed through the fragment call, and float
and margin are stated the same way — GettingLost's `[data-block-type="googleMap"]` house rule
has no equivalent here, because the page that wants a map is the page that says how big.

## 2026-08-30 — One link decision, written twice

Every link on the site follows one rule, keyed off the url alone:

    external            -> new tab, rel="noopener"
    internal /document/ -> new tab
    internal page       -> this tab, a bare <a href>

`noreferrer` was dropped the same day: `noopener` is the half with a security reason, and
hiding the referrer only costs the destination its own analytics for sites we are happy to be
associated with.

The rule is a pure function of the url string, so it is expressed as the OPENING `<a>` TAG
ONLY, and the caller writes the body and closes it. That is what makes one call serve a text
link and an image link with no second signature. Thymeleaf's parser does not balance tags —
`header.html` opens `<body>` and `footer.html` closes it — so a fragment can end mid-element.

    fragments/block/link.html   th:fragment="link(url)"
    offgrid.js                  window.GL.linkOpenTag(url) -> string

Two implementations, because pages are server-rendered and the browser page assembles its
rows in JS from `/shared/browser/data/{id}`. A single implementation would need one side to
stop rendering its own anchors. Rejected on the way: a `link(url, label)` fragment (a text
parameter cannot carry an `<img>`), a `content` fragment parameter (id selectors do not
survive `th:each`), and a click-time handler in `offgrid.js` (Pierre: no onLoad, Thymeleaf
does everything).

For this to hold, a url in the data is a COMPLETE pointer — absolute `http(s)://` or
site-absolute `/…` — never a name a template wraps a route around. `workUrl` carries
`/document/…` for that reason, and the booklets moved out of `static/misc/` into the Documents
folder so they are `/document/` links like any other.

**Two anchors are built without calling it, and that is fine.** The gallery card in
`browser.js` writes its own `<a class="gl-gallery-card">`, and a map pin with a url and no
photo navigates on marker click with `window.location.href` in `google-map.js`. A card always
points at an offgrid page, which is the bare-anchor case; the rule would add nothing. If a url
ever needs a new tab in either place, that is a content matter, not a defect in these two.

## 2026-08-31 — A photo carries no coordinates

GettingLost's photo block took `data-lat`/`data-lng` and, when both were present, drew a
DMS caption under the picture linking to `google.com/maps?q=lat,lng`.
`fragments/block/photo` takes the filename and nothing else.

Dropped deliberately: whether a picture says where it was taken is the content author's
choice, and the choice is no. The one page that used it is `destinations/lakes/echo-lake`.

Recorded because the absence looks like a porting gap when offgrid is read against
GettingLost, and it is not one.

## 2026-08-31 — A blog is composed of posts

Both words are right, and they name different things. The blog is the collection: one page,
at `/blog`, and the menu item that points at it. A post is one item in it, served at
`/posts/{name}`, authored under `templates/posts/` with its data under `data/posts/`.

So there was never a word to choose between. The folder, the template folder and the route say
`posts` because that is what they hold; the menu says Blog because that is what it opens.

## 2026-09-07 — The sky page

Two server calls, and they answer different things. `/info/sky` renders the page and knows
about the observer; `/sky/chart.svg` answers one image and does not, because heliocentric
positions do not depend on where the reader stands. Neither hands the other's data to its
template.

**The chart is an image at its own URL, not inline SVG.** That is what lets a reader open it on
its own, which inline markup cannot offer. The cost is real and was accepted: an SVG loaded
through `<img>` is its own document, so the page's CSS cannot reach inside it and its palette
lives in the fragment.

**It carries no `<title>` or `<desc>`.** Through `<img>` a browser never exposes them, and
opened on its own an image is named by its URL — which is what every JPEG on this site already
does. The `alt` on the page does the accessibility work.

**The width belongs to the template.** The chart fragment takes it as a parameter and calls
`SkyDataChart.setCanvas(width)`, which answers a map of positions keyed by body name. Nothing
in `SkyBody` is a length: the two radii are fractions, and the same body drawn at two widths
has two positions. That is why the page can show a 430 chart beside the table and a 1024 one in
a `<details>` without a second set of anything.

Thymeleaf renders a template on one thread in document order, so two calls at two widths are
safe: the first is fully written before the second replaces the map.

**`SkyData` holds only what both renderings share** — the date and the body list.
`SkyDataChart` adds the canvas and the caption, `SkyDataTable` the observer. The split exists
because one object serving both meant the table's template saw the drawing and the SVG's saw
the observer.

**Label placement rejects a candidate that shares a column with a placed label and sits within
a line height of it.** A box test alone passes two labels that never touch and still read as one
stacked block; that rule is what replaces the skill's "look at the result and override where it
reads badly", which a server cannot do. What it cannot fix is two planets in conjunction at a
small canvas — see `docs/todo.md` #53.

**The place name is reverse geocoded in the browser, through the Maps JavaScript API.** The
geocoding web service refuses a referrer-restricted key outright, and the site's key is one.
The server never claims a name it cannot know: the page renders with coordinates, and the name
is written in when Google answers, or not at all.

Departure from the template-layout decision above: `fragments/block/sky-fragment.html` holds
two fragments, `table` and `chart`, not one. Pierre's call — they are two halves of one page
and the file is named for the page, not for either fragment.
