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
`og-constants.js` and `google-map.js` only register on `window.OG`, and `lightbox.js` and
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

`/hardware/checklist/{name}` is one `@GetMapping` for the whole folder, not one per page.
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

The lists carry `og-checklist` and `og-numcheck`. The `gl-` prefix existed to namespace
against the WordPress theme, and offgrid has no theme to collide with, so it bought nothing
here — it was kept only so pages copied from GettingLost pasted in untouched.

**A page copied from GettingLost is renamed on the way in.** Nothing in this repo carries a
`gl-` name, so a pasted page is not finished until every one of them is `og-`.

The class on the list is what selects the variant, so it stays on the element and the
processor reads it: `og-checklist` is a plain checkbox row, `og-numcheck` adds the counter
span. One processor with two looks rather than two processors.

**The processor has to be idempotent, or it recurses until the stack runs out.** Thymeleaf
hands a model processor's own output straight back to it — `ProcessorTemplateHandler` sets
`modelAfterProcessable = true` whenever the processor changed anything — and the rewritten
list still carries `og-checklist`, so it matches again. The engine stops only when a pass
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
the parameterized route too. It sits above `/hardware/checklist/{name}`, which is a segment
deeper and does not collide.

The van page is `van`, not `van-overview`: its data folder came across as `data/hardware/van/`
and the mirror decides the name.

**`photo` is inserted, not replaced.** The page owns the box — the van sets
`width:100%;float:left` on the block element itself — so `th:insert` keeps the authored
element and puts the picture inside it. `th:replace` would drop the geometry the author
wrote. The defaults for a page that sets nothing live in `.og-photo` in the stylesheet, not
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

## 2026-08-26 — Blogs, and the map that does not belong on one

The six blogs are converted. `.og-blog` — the lined paper written in the Texture pass and
until now used by nothing — is what the body sits on, and the blog's `date`, carried in all
six JSON files and rendered by none of them, is a dateline in the brand's hand directly under
the title.

**No `googleMap` on a blog.** Every WordPress blog opened with one, and every GettingLost blog
JSON carried `googleMap.where`; the block is gone from all six templates and the data was not
brought over. A blog that needs to put its subject on a map links to the destination page,
which is the page that owns the map. So `googleMap` stays on `DestinationPage` and is not
promoted to `PageData`.

Links to pages that do not exist yet are written as the real `<a th:href="@{…}">` they will
be — the three destination links, and the "← All blogs" link that used to point at
`gettinglostonvi.wpcomstaging.com`. A link that is removed because its target is missing is a
link nobody restores when the target arrives.

`photo-gallery` now puts `id="og-photo-<gallery>-<itemId>"` on each figure. The ids were in
the JSON all along and the fragment dropped them, which left the picnic blog's link to its
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

`og-constants.js` holds the vocabularies — `TAG_COLORS`, `DESTINATION_TYPES`, `LINK_TYPES`,
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

The `pageLink` block became the real `<a th:href="@{/hardware/checklist/arriving-campsite}">`
it will be, following the rule the blogs pass set.

**Step lists carry `og-numcheck`, enumerations stay plain `<ol>`.** `dump.html` had already made
that choice; awning, climate and water follow it. The lists in battery and power enumerate
things rather than tell you to do them in order, so they are not checkboxes.

## 2026-08-28 — The back link is built on the server

`Referer` is on the HTTP request, so the server knows where the visitor came from and the
link is in the page as it arrives. `paintBackLink`, `browserReferrer`, `galleryName`,
`BROWSER_PATH` and `GALLERY_NAMES` are gone from `offgrid.js`.

The rule it came from: the reason for putting work in the browser has to be something the
server genuinely cannot know. The referrer was never that.

Two model attributes carry it, `backQuery` and `backName` — the query the gallery is
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
`og-map-<mapName>` — and the render function is `window["renderMap" + mapName]`, so two maps
on a page never collide.

**The page states the map's size on its own div, and keeps it with `th:insert`** (2026-08-29).
Google fills the container it is given, so some box has to state a width and a height. The
page writes `<div style="width:47%;height:200px;float:right" th:insert="…map('road')">`, and
`.og-mapbox` is `width/height:100%` so the fragment's div fills that box. `th:replace` would
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
    offgrid.js                  window.OG.linkOpenTag(url) -> string

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
`browser.js` writes its own `<a class="og-gallery-card">`, and a map pin with a url and no
photo navigates on marker click with `window.location.href` in `google-map.js`. A card always
points at an offgrid page, which is the bare-anchor case; the rule would add nothing. If a url
ever needs a new tab in either place, that is a content matter, not a defect in these two.

## 2026-08-31 — A photo carries no coordinates

GettingLost's photo block took `data-lat`/`data-lng` and, when both were present, drew a
DMS caption under the picture linking to `google.com/maps?q=lat,lng`.
`fragments/block/photo` takes the filename and nothing else.

Dropped deliberately: whether a picture says where it was taken is the content author's
choice, and the choice is no. The one page that used it is `destination/lake/echo-lake`.

Recorded because the absence looks like a porting gap when offgrid is read against
GettingLost, and it is not one.

## 2026-08-31 — A blog is composed of blogs

Superseded by 2026-09-17 below. This entry gave the collection and the item two different
words, so the url and the folders disagreed. There is one word now, and it is blog.

## 2026-09-07 — The sky page

**The place name is reverse geocoded in the browser, through the Maps JavaScript API.** The
geocoding web service refuses a referrer-restricted key outright, and the site's key is one.
The server never claims a name it cannot know: the page renders with coordinates, and the name
is written in when Google answers, or not at all.

Departure from the template-layout decision above: `fragments/block/sky-fragment.html` holds
all of the page's fragments, not one. Pierre's call — they are the parts of one page and the
file is named for the page, not for any one fragment. Three of them: `datePicker`, `chart` and
`table`, each the empty element `sky.js` fills.

**Every block carries the element the page lays out.** The page places one with `th:replace`
and states nothing about it — `.og-sky-chart` and `.og-sky-table` are inside their fragments,
not in `sky.html`, so the row is two lines and the page reads as content. `th:replace` discards
the host div, which is what makes that work; it is also why a block needing a per-use size uses
`th:insert` instead, the way the map fragment does. `chart` takes its width and `table` takes
`isWide`; `datePicker` takes the id of the element it drives.

**No calendar is ours.** The `datePicker` fragment is a `<button>` over a transparent 1px
`<input type="date">`, and the click calls `input.showPicker()`. The browser draws its own
calendar, anchored to that input — which is the only reason the input is on the page.

**Superseded 2026-09-12 from `change` onwards** — picking a day submitted a GET form and loaded
the page again, with the coordinates riding as hidden fields. Nothing is submitted now. See *A
date control drives the blocks in one element* below.

Two things about that input are load bearing. It must be rendered — transparent and 1px, never
`display:none` — or there is nothing for the calendar to anchor to. And it must stay focusable and
take pointer events: Safari opens the calendar with `tabindex="-1"` and `aria-hidden` on it, then
will not close it for Escape or a click outside, because focus never entered the input. It was
found by drawing three variants side by side and clicking each one in Safari.

The button wears the surface the browser page's filters wear — white, `--rule` border, 6px radius,
the chevron that means a list comes down — at the 20px the date heading used to be. It is a rule of
its own rather than a fifth selector on theirs, since only the idiom is shared and none of the
measurements are.

**The table owns its width, and it is stated on the headings** (2026-09-07). Each `<th>` carries
its column, the table is `table-layout: fixed` so those widths bind, and `width: max-content`
makes the table their sum — so changing a column changes the table and nothing has to be kept in
step in `site.css`. This follows the house rule on `main table`: the width and the column widths
are inline.

Neither column of the row shrinks, so a screen narrower than chart plus table scrolls sideways.
Pierre's call: a table squeezed below its columns folds every row onto several lines, which costs
more than the scroll does. The Orbit column is sized for `1 year 322 day` — Mars is the only
orbit in the 1-to-10-year band where `TimeUnits.format` uses two units, so nothing else grows
into it.

## 2026-09-07 — Every name is `og-`

The sky page's CSS came out of `sky.html` and `sky-fragment.html` into `site.css`. It went
there rather than into a `sky.css` because the header loads one stylesheet for every page,
and a second one would need a per-page `<link>` mechanism that does not exist.

That raised the prefix, which the stylesheet had been answering two ways: 67 classes carried
GettingLost's `gl-`, and everything written for offgrid carried none.

**One prefix, `og-`, on every class and every generated id.** `window.GL` became `window.OG`
and `gl-constants.js` became `og-constants.js` — a GettingLost name is a GettingLost name
whether or not it is a class. The sweep reached `site.css`, the four js files, the templates,
and `ChecklistElementProcessor`, which writes three class names in Java.

**State classes stay bare** — `is-active`, `has-submenu`. They say what an element currently
is, not what it is; `og-is-active` reads as a thing.

## 2026-09-11 — The chart's width is one number, and the caption is its own line

**The width is stated once, in the page's `chart(width)` call.** It rides on the block as
`data-width` and the browser reads it, and it is the drawing element's own width and height, so
the space is held from the first paint. `site.css` states no length for it at all — `flex: 0 0
430px` was the same number written twice, and either it was kept in step by hand or the browser
scaled the drawing to whatever the CSS said. A page places the chart at any width and changes
nothing else, which is what the 430 beside the table and the 1024 in the `<details>` already
wanted.

**The drawing is square**, which is what lets one number set both attributes.

**The caption rides on the block as `data-caption`.** It is a literal in the fragment: one
template reads it, and a constant that only one file wants belongs in that file.

## 2026-09-12 — The sky table is drawn in the browser

**Thymeleaf produces the minimum the JS knows how to fill, and no data.** The `table` fragment
is a `div.og-sky-table` holding an empty `<table>` and an empty `div.og-sky-observer`; `sky.js`
builds the rows, the observer line and the place name from what `/rest/sky/observer` answers. The
point is the second call: the page can ask again — another date, another place — and only this
block changes. What it costs is the first paint, since an empty block shows nothing until the
answer arrives and nothing at all without JS.

**The fragment takes `isWide` and writes it as `data-wide`**, because the browser is what reads
it. `sky.js` holds a narrow and a wide column list and picks between them.

**A column is one entry: heading, width, and a path to its value.** The path's first segment
names a member of the answer when the answer has one by that name — `bodyMap.name`,
`skyBodyDayMap.litFraction`, keyed by the body — and names a moment of that body's day when it
does not, so `RISE.bearing` is where it came up. The last segment picks the format, out of a map
of field name to formatter. Rejected: sniffing the format from the value's range, which already
misreads this answer — a bearing runs past 180, and `litFraction` and `distance` both look
exactly like degrees.

**A heading carries its group before a `|`.** `Rise|Time` and `Rise|Bearing` sit under one
`Rise` cell spanning both, and a heading with no separator takes one cell down both rows. It is
one field rather than two because a column's whole identity then moves as one string. `/` was
the first choice and lost to units like `km/h`; `~` stays available to a heading meaning
"about".

**The group row's rule is drawn as an inset pseudo-element, not a border.** Collapsed borders
join into one line across the head, which says nothing about which columns a group covers.

**A column occupies exactly the width its entry states.** `box-sizing: border-box` and no side
padding on the cells, the table's own width set from the sum of the list, and the widths in a
`<colgroup>` — the first row carries the groups' spans, so a width on a `th` would no longer
bind. Anything that quietly adds to a stated width makes the author's arithmetic useless: seven
columns of 50 have to fit wherever two of 175 fit.

## 2026-09-12 — A date control drives the blocks in one element

The page carries one date control per `<details>` rather than one for the page, because a control
sitting inside the block it changes needs no prose explaining what it applies to — which was the
problem to solve: the single picker above the blocks read as decoration. The cost is accepted
deliberately: two blocks may show two different days at once, and there is nothing wrong with
wanting sunrise on two days side by side.

**A control is told the id of the element holding what it drives**, and the page states it:
`datePicker('sky-overview')` beside `<details id="sky-overview">`. Walking up the tree with
`closest()` was the alternative and was rejected — it ties the scope to whatever tag the page
happened to use for disclosure, it cannot drive a group the control does not sit inside, and two
blocks inside one `<details>` would silently share a control. An id that names nothing is reported
by `logError` and the whole page answers, so a renamed wrapper is visible rather than inert.

The mechanism is in the code and is not restated here: the fragment comment above `datePicker` in
`sky-fragment.html`, and `openDatePicker` and `scopeOf` in `sky.js`.

**The date control's wiring belongs to `sky.js`.** It lived in `offgrid.js`, which every page
loads, and called `OG.loadSkyData`, which only `sky.js` defines — the site-wide file depending on
a page's file. `noonOf` and `buttonText` went with it, nothing else using them.

**Thymeleaf hooks the click; the JS owns the cascade.** The button carries
`onclick="OG.openDatePicker(this)"` in the fragment, so there is no load-time pass hunting for
controls and the connection is visible in the markup. The scope id rides as `data-scope` on the
box, the idiom already used for `data-width` and `data-wide`: the browser is what reads it.

**The position is asked of the browser once per page view.** `positionOf()` holds the promise, so
three blocks refreshing are three requests for data and one geolocation callback. A visitor does
not move between two clicks on the same page.

## 2026-09-15 — The server sends no sky data

The browser had taken over the table, then the chart, and the server was still computing both
and handing them to the page. `/info/sky` takes no parameters and has no mapping of its own:
`/info/{name}` serves it like every other page under `/info`, and the sky page is now a template
with no Java behind it.

**One source per fact, and the answer is it.** A block's contents come from the sky endpoints
and nothing else. Everything the model used to carry — the table, the chart, the caption, the
date, the ends of the calendar — was a second copy of something the endpoint already answers or
a constant that belongs where it is read.

**The date picker is a block like the other two.** `requestSkyData` collects the boxes in the
scope beside the tables and the charts, and `renderDatePicker` writes the button's words, the
input's value and its `min` and `max`. Thymeleaf renders the button empty and the input bare.
The alternative was filling it from the `dateTime` the request already holds, which fills it
sooner but leaves the calendar's ends to a second mechanism; one place writing the whole control
was worth the wait. `openDatePicker` no longer writes the button on change — the answer does.

**The calendar's ends are `sky.js` constants**, `FIRST_DAY_TEXT` and `LAST_DAY_TEXT`, 2020-01-01
to 2029-12-31. They were `OffgridUtil.SKY_FIRST_DATE` and `SKY_LAST_DATE`, which still clamp
`/rest/sky/observer` at the ephemeris's own 1800-2050. Making the picker's ends follow what is on
disk is `docs/todo.md`.

**A constant that one template reads lives in that template.** The caption was a
`private static final` in `OffgridWebProcessor` put on the model for one fragment, which is a
Java constant taking a round trip to reach the only file that wants it.

## 2026-09-15 — The chart does not wait for the visitor's position

Everything on the page was drawn from one call, and that call waited on `positionOf()`. A first
visit puts a permission prompt in front of it, which a visitor may take seconds to answer or
never answer at all, and the chart and the date control sat blank through all of it — neither
needs to know where anyone is.

**Two calls, fired together.** `requestSkyData(dateTime, scope)` asks `/rest/sky/positions`
straight away and `/rest/sky/observer` inside the `positionOf` callback. The chart and the
picker draw at the speed of a fetch; the table waits, which is the one block that was always
going to.

**Each answer has its own named handler**, `processPosition` and `processObserver`, so the
requesting and the filling read separately. `fetchSkyData(url)` holds what both calls share —
the ok-check, the parse and the timing log — and on failure logs and returns a promise that
never settles, so a handler runs on an answer or not at all.

**The menu's sky link is a plain anchor.** It asked the browser for a position before
navigating, to put the coordinates on the URL; nothing on the page reads a coordinate off a URL
now. The position is asked for once, by `sky.js`, where it is used.

**The blocks are found once, before either fetch, and travel as one `blockMap`** of
`tableList`, `chartList` and `pickerList`. Both handlers take the same `(data, blockMap)` and
pick what they fill. Handing each handler only its own lists was written first and rejected:
it froze at the call site which blocks an answer fills, and that is the handler's business —
the picker needs only `epochSecond` and `timeZone`, which both answers carry, so it could be
filled by either.

Rejected: passing `scope` and letting each handler query. It costs a second pass over the DOM
and puts the selectors in two places.

## 2026-09-17 — Everything says blog

`/blog` lists them and `/blog/{name}` is one of them, out of `templates/blog/` and
`data/blog/`. Pierre's call, and it overrides the entry above: one word for the whole feature,
in the url, the folders, the Java, the CSS and the link text. A second word for the item was
the split the entry above reasoned its way into, and it is gone.

Because url and folder are the same string, the path `OffgridWebController.blog` builds IS the
url — that path is the view name, so one string answers for both.

`templates/blog.html` sits beside `templates/blog/`, and `data/blog.json` beside `data/blog/`:
the list page and the items it lists, named the same because they are the same feature.

The sweep: `BlogPage`, `.og-blog`, `blogList` and `blog` in the controller, `templates/blog/`,
`data/blog/`, the "← All blogs" link in all six, and the older entries in this file,
`look.md` and `data.md` that named the old ones.

## 2026-09-18 — The blog list is a fragment, and a post's head is the same row

**Both files are gone 2026-09-19** — the two fragments and the dateline they share are one
file now, see the entry below. What each one draws did not change.

`fragments/block/blog-list.html :: blogList` draws the list and takes no parameter. Every
other block fragment takes one because it selects which slice of `pageData` to draw —
`notes('Shopping list')`, `gallery('our-van')`. There is only ever one blog list on a page,
so the model attribute name is enough to find it, and **how many rows it draws is the
processor's decision, not the fragment's**: `/blog` sets all of them under `blogList`, the
home page sets the first three under the same name. A count parameter on the fragment would
have put that policy in the template and still made the home page read every blog json to
draw three.

**The model carries `List<Map.Entry<String, BlogPage>>`.** The row needs the url segment,
which is the json's file name, and `BlogPage` has no field for it — `name` is the title.
Putting it on the POJO would have added a field no json carries. `Map.entry` is the JDK's
own pair, so nothing was written to hold two values; a `Pair` from the classpath would have
been someone else's transitive internal, since the pom declares neither commons-lang3 nor
Spring Data.

`fragments/block/blog-header.html :: blogHeader` is the head of one post, and it is the list's
row reused: same `.og-blog-row`, `.og-blog-thumb` and `.og-blog-detail`, the page's own `<h1>`
where the list has its link, and no excerpt — an excerpt stands in for a page the reader has
not opened, and a post is that page. Each of the six posts calls it with one identical line,
which is what retired `.og-dateline` and the six copies of the date format that went with it.

## 2026-09-19 — The blog's blocks share a file, and the dateline is one of them

`fragments/block/blog-fragment.html` holds `blogList`, `blogHeader` and `dateline(date)`.
Pierre's call, and the second departure from the one-fragment-per-file rule above, on the same
grounds as `sky-fragment.html`: a post's head is the list's row with the page's own `<h1>` in
place of the link, so the file is named for the feature and not for any one fragment.

**`dateline(date)` carries the span, the `og-blog-date` class and the format.** The format was
written in both of the old files, so the same post's date could print one way in its header and
another in its row. What the format is and why is *A date reads the same everywhere* in
[look.md](look.md); this entry only says that one fragment now owns it. It takes the date as a
parameter because its two callers hold it in two places — the list under the entry it is drawing,
a post on `pageData` — which is the only thing that ever differed between the two lines.

**A fragment calling another in its own file names the file**, `~{fragments/block/blog-fragment
:: dateline(…)}`. An explicit path resolves the same wherever the calling fragment has been
inserted into a page.

## 2026-09-26 — A link handed outside the page is a whole URL

**`server.publicProtocol`, `server.publicHostName` and `server.publicPort` state where a visitor
reaches the site**, beside `server.port` in the profile yaml. An MCP answer, such as the link
`ChecklistResource` returns, is read outside any page, so a path alone is not a link. The public
port is its own value because it is the visitor's, and equals `server.port` only when nothing
sits in front of the app. The request's own host was rejected: an MCP call may arrive with no
request on the thread, and behind a proxy it names the proxy's side.
