# Decisions

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

## 2026-08-24 — Repo

`offgrid`, private, under `PierreCote-57`. Private costs nothing on a personal account;
public would only buy unlimited Actions minutes and Pages, neither of which is in use.

Name chosen over `gettinglost*` because it is short, matches the content, and is a legal
Java package segment with no hyphen to strip.

## 2026-08-24 — Java 21 (Amazon Corretto)

FullHost's published Java stack tops out at 21 LTS (23 non-LTS, no 25), so 21 is the
newest version that is both LTS and runnable there. 25 was the first pick until their
stack list was read.

Non-LTS releases stop getting security patches roughly six months after they ship, which
rules them out for a server.

Their published list looks stale, but Pierre confirmed with FullHost on 2026-08-24 that
Corretto 21 is available. The pin holds.

## 2026-08-24 — Spring Boot 4.1.1

Current release on start.spring.io. Requires Java 17+, so 21 sits inside its range with
room ahead.

## 2026-08-24 — Maven, not Gradle

The project was generated with Gradle first and migrated. FullHost's build node runs
Maven, and building from GitHub was judged worth the switch: a deploy then needs nothing
from Pierre's machine — push from anywhere, including a phone, and the site updates. Same
shape as the GettingLost pipeline.

The cost accepted: the build runs on their JDK and Maven, so a build can fail there and
pass locally. Their build log is where that shows up.

IntelliJ is indifferent between the two.

## 2026-08-24 — Fat jar

`./mvnw package` produces one self-contained jar. Nothing is deployed as a war into a
container.

## 2026-08-24 — Shape of the application

Two controllers: a Client controller serving pages, and an MCP controller. Thymeleaf for
server-rendered pages, Alpine for client-side behavior, plus static content.

Package `com.lc.offgrid`, artifact `offgrid`.

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

## 2026-08-24 — Palette

Forest: bar `#1f5e42`, bar text `#eef3ef`, deep `#173f2d`, soft `#e7efe9`, body text
`#243027`, rules `#d6dfd8`. Declared as custom properties on `:root`, so the palette
changes in one place.

Header and footer wear the colour, the page between them is white.

**Superseded 2026-08-25 on both counts** — the page is warm paper and the green family is no
longer alone. See *Paper, not white* below.

**There is no contrasting accent colour.** Every decorative colour on the site is a shade of
the bar green. An amber accent was tried and rejected — the button jumped off the page.
Buttons are outline style: soft fill, brand border, filling in on hover.

The exceptions, added 2026-08-25, are the message signal colours: warning orange (`--warn`
`#b26a00`, `--warn-deep` `#7a4700`, `--warn-soft` `#fdf0dc`) and error red (`--error`
`#a02a1f`, `--error-deep` `#7a1f16`, `--error-soft` `#fbeae8`). These are not accent
colours — they are signals, and a failure that reads as another shade of green is a failure
nobody sees. Nothing outside the message strip may use them.

GettingLost's brown/Lora/Source Sans look was not carried over; a lighter, happier palette
was wanted instead.

## 2026-08-24 — Brand wordmark

"Offgrid" in the header is Bradley Hand, italic. It reads as handwriting on a field
notebook rather than a formal script, and it stays legible when the header shrinks on a
phone.

Pacifico, Chalkduster and Papyrus were tried and dropped — Papyrus specifically because it
is the one typeface a general audience recognises and mocks by name.

Bradley Hand is an Apple system font, so it cannot be self-hosted the way Alpine is.
Non-Apple visitors fall back to generic `cursive`. Accepted for a single seven-letter word;
it would not be acceptable for anything the site depends on being read.

## 2026-08-25 — px, not rem

All CSS lengths are px. `rem` is out: Pierre reads px and pt, and a stylesheet he cannot
read at a glance is a stylesheet he cannot review. The conversion was exact at the default
16px root, except where a rem value did not land on a whole pixel — those were rounded to
the nearest px (1.9rem → 30px, 0.95rem → 15px, 0.85rem → 14px, and similar sub-pixel
paddings).

What this gives up: text no longer scales when a visitor raises their browser's default
font size. Zoom still works, and that is what people actually use.

## 2026-08-25 — Centred page column, 1024px

`main` carries `max-width: 1024px` and `margin-inline: auto`, so the whole column — heading,
paragraphs, images — centres as one block. The measure previously sat on `main p`, which
capped the text but left it hard against the left edge and out of line with the heading.

1024 is wider than prose alone wants: a full-width paragraph runs past 120 characters a
line, which is where readers start losing their place on the return sweep. It is chosen
anyway because normal pages carry images interspersed with the text, so no paragraph
actually spans the full width. If a page ever does run edge-to-edge prose, that page caps
its own paragraphs.

`box-sizing: border-box` is set on `main` so the max-width is the visible column width
rather than the width before padding.

## 2026-08-25 — Content lives in the repo

JSON and HTML content ship in the repo and reach the server the way the code does: push,
FullHost builds, site updates. No mounted volume, no Mountain Duck licence, and the
question of which container path survives a redeploy stops mattering.

The cost accepted: a typo fix is a push, a Maven build on their node and a restart —
minutes, and the site blips. Worth it while content changes in batches.

Images are **not** covered by this and are deliberately left undecided; git keeps every
version of every binary forever, which is the one place the repo stops being free.

## 2026-08-25 — `resources/data/` mirrors `resources/templates/`

The data folder is an exact mirror of the templates folder. `templates/index.html` takes
its data from `data/index/`; `templates/fragments/site/footer.html` takes its from
`data/fragments/site/footer/`. Mirror means mirror — a fragment that needs data gets the
matching folder on the same terms a page does.

A template that needs no data has no matching folder. Absence is the normal case, not an
omission.

Either side may run ahead of the other, in both directions: data sitting in `data/` that no
template reads yet, or a template that will get its folder later. Nothing checks the mirror
and nothing fails on a mismatch — an unmatched data folder is simply not read, and a
template with no folder renders without it.

The point is that view name → data path is a string transform. No registry, no map, no
per-page wiring — adding a page is a template plus, if it needs one, its folder.

A folder per template rather than a file per template, so a template that later wants a
second data file beside its first does not force a migration or an exception to the rule.

Consequence for data used by several pages: it belongs to the fragment that renders it,
which is the thing that actually has the mirror entry. There is no shared bucket outside
the mirror.

## 2026-08-25 — Gson, and records for the content POJOs

Gson reads the page JSON. Jackson is already on the classpath through
`spring-boot-starter-webmvc` and stays there for HTTP message conversion; Gson is a second
library, added deliberately, not a replacement. Boot manages its version, so the pom carries
no `<version>`.

Jackson was weighed against it on 2026-08-25 and lost on one concrete behaviour: Gson binds
`"icon": ""` to `MapIcon.NONE` through `@SerializedName("")`, and Jackson cannot — it will
not take `@JsonProperty("")` on a constant, so the empty string arrives as null and `NONE`
is a dead constant. Content that says "no opinion about the marker" stays expressible as a
value rather than an absence.

The argument for Jackson was that it costs no new dependency, against two lines of mapper
configuration. That was the closer call and it was made the other way. Everything else was
a wash: six annotations either way (`@SerializedName` vs `@JsonProperty`), both sets deleted
by the rename pass — the table is in *Standing rule — convert every JSON file brought in
from GettingLost*, below.

`PageDataJacksonReadTest` is kept as the worked comparison — the same files, the same
records, read by Jackson — so the choice can be re-examined without reconstructing it.

Boot 4 ships Jackson 3, whose package is `tools.jackson`, not `com.fasterxml.jackson`. Worth
knowing before reading any Jackson answer written before 2025.

The content POJOs are **classes, not records** — reversed the same day. Records cannot
extend anything, and the page model is a hierarchy. Records were the first pick because Gson
binds to **field** names rather than accessors, so a class in the `m_` house style would look
for a JSON key `"m_name"`; the classes therefore carry plain field names, `lat` and `label`,
with no prefix and no `FieldNamingStrategy`. Fields are private with public getters, which is
what Thymeleaf reads.

Classes also allow computed accessors. Note that records permit methods too, so that was not
the deciding factor; inheritance was.

Gson sets fields directly by reflection and never calls a constructor or a setter. Computed
**getters** work as written. A computed **setter** would silently never run.

One type per file, which Java requires for public types anyway.

Every field is optional. A page omits what it does not need and the field arrives null —
`index.json` carries only `name` and exercises exactly that. Gson ignores unknown properties
with no configuration, which is what makes "either side may run ahead" work.

`wpSettings` has no POJO. It is WordPress publishing state, not page content, and is simply
not read.

### No key mapping in the POJOs

There is no `@SerializedName` on any field. The JSON keys are spelled exactly as the record
components are, and the six that were not — `badges`, `types`, `notes`, `list`, `items`,
`haversine`, plus `location_id` for its snake_case — were renamed in the data on 2026-08-25
rather than mapped in the code.

The bridge was built first and removed the same day. The argument for keeping it was that
GettingLost's live consumers (`gettinglost.jst`, `list_browser.jst`, `check_docs.py`,
`build_booklet_pdf.py`) read the old spellings — but they read GettingLost's own copies, in
a different repo, so renaming on the way into offgrid leaves them untouched. The annotation
was buying nothing and costing a permanent disagreement between what a data file says and
what the Java says.

The cost accepted: while both sites run, an offgrid file no longer diffs cleanly against its
GettingLost original, so the rename table below is what verifies a copy instead.

`MapIcon` still carries `@SerializedName`, on its constants. That maps enum *values*, not
key names, and is unaffected.

### Standing rule — convert every JSON file brought in from GettingLost

GettingLost keeps the old spellings and its own consumers still read them; offgrid does not.
The conversion happens on the way in, as part of the copy, never afterwards.

    badges      -> badgeList          keywords  -> keywordList
    types       -> typeList           legs      -> legList
    notes       -> noteList           amenities -> amenityList
    list        -> itemList           haversine -> haversineList
    items       -> itemList           location_id -> locationId
    displayName -> label

    campground  -> campgroundData
    links       -> campgroundData.referenceList   (moves inside, not just renamed)
    location.zoom -> the googleMap entries that have none of their own
    tags: []    -> the key is deleted
    categories  -> deleted when empty

Because nothing maps key names, a file that arrives unconverted binds its renamed fields to
null rather than failing, so the miss is silent.

## 2026-08-25 — A location is a place, not a map

```
Point                      Double lat, lng
Place extends Point        String label
                           MapIcon icon
                           String img
                           String url
GoogleMap extends Point    String file, locationId
                           Integer zoom
                           List<Place> pinList
```

`location` is a `Place`. A pin is a `Place`. A `googleMap` is a viewport that points
somewhere and drops pins. No class carries a field it does not use, and there is no `Pin`
type — `List<Place> pinList` already says it.

This departs from GettingLost's schema, which unified `location`, `googleMap` and `pin` into
one shape where a location was *also* a valid map. That model cannot be expressed by
inheritance at all: a location was the union of two siblings, and Java has no multiple
inheritance. Every attempt to arrange it as a hierarchy left one branch short.

Cutting `pinList`, `zoom` and the pointer fields off `location` breaks the union and the
hierarchy falls out in three classes. The check that made it safe: across the 61 GettingLost
files, **no** `location` carries `pinList`, `img`, `url`, `file` or `location_id`. Locations
carry only `lat`, `lng`, `icon`, `zoom` and `displayName`, so the only field actually in use
that moves is `zoom` — 17 of the 26 locations have one.

`lat`, `lng` and `zoom` are boxed. `0.0` is a real coordinate in the Gulf of Guinea, and a
primitive cannot distinguish it from a pin that omits its coordinates to sit at the map's
centre — which the renderer treats as a deliberate instruction.

The pointer fields are `file` and `locationId` as two nullable fields rather than one
`reference`, because that is what the JSON writes, and precedence is `file` → `locationId` →
`lat`/`lng`.

## 2026-08-25 — Two packages: page and part

`com.lc.offgrid.pojo.page` holds the six page classes; `com.lc.offgrid.pojo.part` holds the
seventeen blocks they are built from. A page class is something a URL resolves to. A part is
never a page on its own.

## 2026-08-25 — The page hierarchy

```
PageData            name, featuredImage, excerpt, tags, noteList,
                    photoGalleries, relatedDestinationList
  PostPage          date
  DestinationPage   location, googleMap
    AreaPage        —
      LakePage      fishingReferences
    CampSitePage    access, campgroundData
```

**Superseded 2026-08-31** — `AreaPage` is gone, `access` sits on `DestinationPage`, and
`CampSitePage` is spelled `CampsitePage`. See *A destination has an access, or it does not*
below. Everything else here still holds.

Every page class carries the `Page` suffix. `PageData` does not: it is the base, and it is
also the class a page with nothing special uses.

| real-life page | class | files |
| --- | --- | --- |
| lake | `LakePage` | 10 |
| park | `AreaPage` | 4 |
| rec-site, camping | `CampSitePage` | 5 |
| rec-site, day-use | `CampSitePage`, `campgroundData` null | 3 |
| commercial campground | `CampSitePage` | 2 |
| blog post | `PostPage` | 6 |
| howto, checklist, about | `PageData` | 12 |

The boundaries came out of the data, not from taxonomy. All 10 lake files carry
`fishingReferences` and no `access`; all 8 rec-sites carry `access` alone; all 4 park files
carry `access` + `campground` + `links`, which is to say a park page was a campground page
wearing a park tag. That is the thing being corrected.

**An `AreaPage` has extent, not an address.** A lake and a park are arrived at somewhere along
their edge, so there is no single spot to drive to and no `access` block. A `CampSitePage` is a
spot: you drive to it and stop.

An area can hold several of those spots — a lake with two rec sites, reached by different
roads — and they do not share one access. So the absence of `access` on an `AreaPage` is not a
field left unfilled; there is no single answer for the page to give.

**Superseded 2026-08-31.** The observation about areas holding several spots survives; making
it a class was the part that did not.

**`Site` and `CampSitePage` were collapsed into one class.** A day-use rec site and a camping rec
site are different in real life, but nothing in the data distinguishes them — `tags.types`
says `rec-site` for all 8, and only the map icon (`tent` vs `picnic`) hints at it. A class
tree may not encode a distinction the loader cannot see, so a day-use site is a `CampSitePage`
with `campgroundData` null.

**Posts lose `googleMap`.** A post links to the destination page in its content instead, via
`relatedDestinationList`. A subject with no page of its own gets no link. `PostPage` therefore
adds only `date`.

`relatedDestinationList` is `List<String>` of page filenames; the target page supplies its own
name. No file authors it yet.

`campgroundData` absorbed what was `links`: `{amenityList, operator, siteCount,
referenceList}`. `Reference` is `{label, ReferenceType type, url}` — the operator's own pages,
typed HOMEPAGE, MAP or RESERVATION. It stays a list rather than three flat fields: the 6 page
files each carry exactly one of each, but the registry's 190 entries carry between one and
four, with labels like "Park" and "Trail".

`Leg.type` is a `String`, not an enum. Only four values appear — potholes, sharp_rock, unpaved,
dirt — across four files, which is too thin to close the vocabulary. An enum would bind an
unlisted value to null silently.

## 2026-08-25 — The data pass ran

All 56 JSON files in `resources/data` were converted in one pass, the registry's 190 entries
included. The rename table is under *No key mapping in the POJOs*; the structural moves were
`links` into `campgroundData.referenceList`, `location.zoom` onto the googleMap entries that
had none of their own, `tags: []` deleted from the 8 van files, empty `categories` deleted
from the 6 posts, and four posts' `googleMap` becoming `relatedDestinationList`.

Two rounds, deliberately. The first pass renamed and moved but deleted nothing that had
content in it, and reported what it had left behind. Pierre then ruled which of those were
obsolete — the two posts pointing at locationIds with no page, and the four park files still
carrying `access` and `campgroundData` — and those were removed in a second pass.

**Deleting content is his call, not the migration's.** A pass that renames can run
unsupervised; a pass that drops a block someone wrote cannot. A block no class read yet —
the maintenance records, `wpSettings` — was left where it was for that reason.

One `campground` key survives on purpose: it is the name of a `googleMap` entry, a map of the
campground inside a park, not the data block.

## 2026-08-25 — A URL is the view name

`/info/useful-links` renders `templates/info/useful-links.html`, whose data folder is
`data/info/useful-links/`. The URL, the view name and the mirror path are the same string,
so adding a page is a template, a `@GetMapping` and a `process*` method — nothing else to
keep in step. Where a parameterized route already covers the shape, it is a template and its
folder, and there is no Java to write at all.

The three Info pages are the first to use it. `/info/about` is a heading, the header and the
footer, and nothing else; the two Useful pages render their `noteList`.

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

## 2026-08-25 — A user who edits the URL is on their own

"If you try to write foolproof software, nature will invent a better fool." The site is
clean and bug free for the URLs it publishes. A URL nobody linked to is not a case to be
designed for.

What a hand-edited URL owes: **survive, and leave a developer enough to diagnose it.** It
must not corrupt anything and it must not die silently. It owes the person who typed it
nothing beyond that — no friendly wording, no guess at what they meant, no recovery path.

The concrete instance: a parameterized route matches any name in its shape, so
`/info/useful-anything` reaches `readFile`, throws, and is caught into the `exception`
view. That is the correct outcome, not a hole to plug.

`templates/exception.html` was written 2026-08-31 and is the site's own page: a fixed
sentence saying nothing was changed, the menu to leave by, and `errorMessage` in the
`.message-error` strip for whoever is diagnosing it. `BaseWebController` sets `PageName` in
the catch so the heading and the tab have something to read. A failure landing before
`processDefault` runs still leaves the rest of the chrome empty, which is accepted until it
is seen.

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

**An image slot renders its filename.** Until #9 settles there is no URL to serve an image
from, so every photo and every thumbnail shows the filename it wanted. That is the diagnostic
form — it says which file is missing rather than leaving a hole — and it disappears the day
images have a path.

**Superseded 2026-08-25.** Images have a path. All three fragments emit real `<img>` tags —
see *Images live outside the resource tree* below.

## 2026-08-25 — The whole page data goes into the model

A handler puts `pageData` in the model, not a field at a time. `model.addAttribute("noteList",
pageData.getNoteList())` writes *which fields a page has* in a second place, so adding a field
means editing Java to expose something the template already had in hand.

The honest size of the win: three naming sites become two. The template names the field
either way; only the controller's copy disappears.

The argument that did **not** decide it, recorded so it is not made again: a shared fragment
needing one known attribute name. Header and footer data is cross-cutting and arrives on its
own channel whatever a handler puts in the model, so it says nothing about how much of the
page's own data travels.

## 2026-08-25 — The application class sits at the package root

`OffgridApplication` moved from `com.lc.offgrid.spring` to `com.lc.offgrid`. Component
scanning starts at the annotated class's own package, so anything outside `spring/` — the
first case was `misc/imaging` — was never scanned and could not become a bean.

`scanBasePackages` would have fixed the one case. Moving the class fixes every future one,
and the failure it prevents is nasty: an injection error somewhere unrelated to the class
that was actually invisible.

## 2026-08-25 — Images live outside the resource tree, behind `/image/`

Pictures are not in the repo. They sit in a folder on the machine named by `folder.image`,
and reach the browser through `GET /image/{imageName}` — a controller method that returns
`ResponseEntity<Resource>` and never goes through `processRequest`, because it answers with
bytes rather than a view name.

**The URL carries a bare filename, not a path.** That is already what the JSON holds, and it
leaves the folder layout under `folder.image` free to be rearranged without touching a single
data file. The cost accepted: the server resolves name to file, and two files with the same
name in different folders are ambiguous.

The prefix is written once, in the fragments, as `@{/image/{imageName}(imageName=${img})}`
rather than a literal string — `@{}` prepends the servlet context path and URL-encodes the
substitution, which matters the first time a filename has a space in it.

`OffgridImageManager` is the singleton that knows where a name lands; `ImageMetadataExtractor`
reads EXIF through metadata-extractor 2.19.0 and flattens it into `ImageMetadata`. Drew
Noakes over Apache Commons Imaging for the read path: Commons sat in `1.0-alpha` for about a
decade and its makernote coverage is narrower. Commons is the better bet the day metadata has
to be written back.

## 2026-08-25 — Paper, not white

The site felt bland, and the cause was written at the top of the stylesheet as policy: every
decorative colour a shade of the bar green, with orange and red locked away for messages. On
a site about forests and gravel roads that spent the whole palette on one hue.

Three paper treatments now, one stylesheet, scoped by what the element is:

- **Wavy** — topographic contour lines, pale on the bar and footer, green on the page. Two
  `url()` data-URIs held in custom properties, so the drawing exists once.
- **Grid** — quadrille at 15px, one weight, on `.gl-checklist` and `.gl-numcheck`. Not
  scientific graph paper: no fine sub-grid, no heavier majors.
- **Lined** — horizontals at 30px on `.gl-post`, with the copy sitting on the rulings.

All three stand on warm paper `#faf7f0`, which replaced white everywhere. Ruled blocks carry
a rust edge down the left so they read as a page out of a notebook rather than as a panel.

The rulings are deliberately near the edge of visible. At full strength they compete with the
text, which is what the first pass got wrong.

**Rejected:** park-patch badges, and a full-bleed photographic hero. **Parked:** signage
typography — condensed uppercase headings with a route-shield chip — until there are buttons
or controls for it to apply to.

## 2026-08-25 — The lightbox came back from GettingLost

Clicking a photo opens a shared in-page overlay: backdrop, ✕ and Esc close it, ←/→ and the
buttons page through the gallery with an "n / total" counter. Ported from the `lightbox` IIFE
in `gettinglost.jst` rather than rewritten against Alpine — it is vanilla, so it needs
nothing, and the sizing traps in it were already paid for.

`min-height: 0` on both the overlay figure and the image is load bearing. Without it the
`max-height` never binds and a portrait shot grows past the viewport and clips at the top.

Three simplifications on the way over: one delegated click listener on the document instead
of per-image handlers; the gallery read from the DOM, so there is no parallel JS array to keep
in step with the markup; and the WordPress admin-bar z-index of 100000 dropped to 1000.

Anchors keep a real `href`, so a click still shows the image with JavaScript off and
cmd-click still opens a tab.

## 2026-08-26 — A warning is a road sign

`fragments/block/warning.html`, called as
`~{fragments/block/warning :: warning('…')}`. The page supplies the words, so it takes a
parameter, which is the rule already set for a block placed individually.

An orange construction diamond sits left of the text on a pale panel with the same orange
down its left edge — the notebook's rust-edge idiom, in the warning colour. Orange is the
temporary-condition family: something is happening right now and you have to watch it.
Yellow, the permanent-hazard family, is unused so far.

The diamond is inline SVG carrying geometry and three class names — `field`, `border`,
`mark` — with every colour in `site.css`. A drawing whose colours are baked into the markup
cannot follow the palette.

The panel reuses `--warn-soft` rather than mixing a fourth cream from the sign orange. Two
new properties were needed: `--sign-orange`, louder than the `--warn` a message bar uses,
and `--sign-ink`, the warm near-black of printed sheeting.

This is where the parked signage typography first lands — the `Warning` kicker is condensed
uppercase, letterspaced, in rust. The rest of it stays parked.

Alternatives drawn and rejected in `_preview/signs.html`: the sign bare on the paper with no
panel, mounted on two posts above the text, a hazard-tape strip with a small chip, and a
worded orange panel with no symbol at all.

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

## 2026-08-26 — MaintenancePage, and the record the JS used to draw

`maintenance.jst` fetched the page JSON in the browser and built the table from a `COLUMNS`
list. That work is now split the way the rest of the site is: `MaintenancePage` carries
`List<MaintenanceEntry>`, `fragments/block/maintenance-actual.html` draws it, and
`site.css` holds every rule the JS used to set inline.

The block reads the model and takes no parameter — a page that wants the record sets
`actualList`, the same contract `noteList` already has.

**The entry is flat.** `shop{name,url}` and `work{name,url}` became `shopName`/`shopUrl` and
`workName`/`workUrl`, `nextDue{km,date}` became `nextDueKm`/`nextDueDate`, and `odometer_km`
and `cost_cad` were the last snake_case keys in `resources/data`. A flat entry makes a table
row a straight read with nothing to walk into, and `Reference` was the wrong home for the
pairs — it is `{label, type, url}`, not `{name, url}`.

`actual` became `actualList` because it is a list, which is the naming rule everywhere else.

Every number field is boxed — `Integer`, `Double` — so a value that is absent is null and its
cell renders empty. An `int` would have printed a zero odometer as a fact.

The work sheet links at `/document/{documentName}`, which nothing serves yet. `workUrl` names
a PDF the WordPress renderer resolved against `/wp-content/uploads/`, and neither file is in
the repo. Rendering the name as plain text instead was considered and dropped: a missing file
is a content bug, and a template that stops linking because the content is missing is a
template that still does not link once the content arrives.

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

## 2026-08-27 — The external downloads, and what `external` is allowed to know

Four reference downloads live in `resources/external/download/`: `bc_reststop.json` and
`bc_offramp.json` from the province's DataBC WFS, `bc_exits.json` and `bc_exits_amenities.json`
from OpenStreetMap through Overpass. They are read-only reference data, not content, and no
page consumes them yet.

**`com.lc.offgrid.pojo.external` is the file and nothing else.** A class there maps a JSON
shape strictly — every key has a home — and interprets nothing. The packages under it are named
for the supplier, not the subject: `external/bc/reststop`, `external/bc/offramp`,
`external/overpass/exits`, `external/overpass/amenities`. A re-download changes the mirror and
cannot reach a page.

**A properties block is a Map, not a set of fields.** `shared/FeatureProperties extends
HashMap<String, Object>`, and each file's block extends it — `RestStopProperties`,
`OfframpProperties`, `ExitTags`, `AmenityTags` — adding a private key constant and a typed
getter per known column. DataBC adds and drops columns and OSM mappers invent tags; a strict
POJO drops what it was not told about, silently. This keeps everything and still reads well:
`bc_exits_amenities.json` carries 296 distinct tag keys, of which 33 are worth a method.

The base has to *be* a Map rather than hold one — Gson routes anything assignable to `Map`
through its map adapter, while a class holding a `Map` field makes Gson hunt for a JSON key by
that field's name.

**Every number in those maps comes back a `Double`.** For an `Object`-typed value Gson has no
other choice, so `NUMBER_OF_TOILETS` arrives as `1.0`. `FeatureProperties.getInteger` converts
rather than casts; nothing is lost, since every id in these files is well under 2^53.

**`geometry.coordinates` is declared `Object`.** A Point is `[lng, lat]` and a LineString is a
list of those, and Gson binds `coordinates` to whatever the field declares — a `Double[]` and a
`Double[][]` cannot be the same class. `Object` takes both: Gson builds an `ArrayList` of two
`Double` for a Point, an `ArrayList` of those for a LineString. All 1336 offramp features are
LineString; all 219 rest stops are Point.

**What is common across suppliers lives in `external/shared`.** `FeatureGeometry` (with
`GeometryType` as a public enum inside it — all seven GeoJSON shapes, since Gson returns null
for an enum value it cannot match), `FeatureProperties`, `FeatureCollectionCrs`,
`OverpassOsm3s`, `OverpassCenter`, `OverpassElementType`. Nothing in a supplier package imports
from another supplier package.

**Overpass has no geometry block.** A node carries `lat`/`lon` itself; a way or a relation
carries neither and has a `center` instead, because the query ended `out center`. 906 ways and
22 relations of the 3225 amenity elements have no `lat` — reading only `lat`/`lon` drops every
building, which is most of the shops, hotels and supermarkets. OSM ids need `Long`: the largest
in `bc_exits.json` is 13,772,932,805.

**`ExternalUpdater` holds where each file came from and where it is kept** — a path and a
source URL per download. `ExternalManager` is a `@Component implements InitializingBean` that
reads all four once in `afterPropertiesSet` and hands out the cached objects. Nothing in
`src/main` uses it yet; `ExternalTests` drives it.

## 2026-08-27 — The Gson moved down to `BaseFileHandler`

`BaseWebProcessor` owned the only `Gson` and did its own parse inside `readFile`. The instance
is now `BaseFileHandler.GSON`, where `BasicFileWriter` reaches it too, and the parse is
`BasicFileReader.readJsonFile` — four overloads by source (URL, File, filename, InputStream),
the same shape `readPropertiesFile` already had. `readPropertiesFile` is the precedent: reading
a format into an object is what that class does, and the method names itself after the format.

`BaseWebProcessor.readFile` keeps the classloader lookup and delegates, so its eleven callers
in `OffgridProcessor` did not move, and `getGson()` delegates as well. The builder settings
(`setPrettyPrinting`, `disableHtmlEscaping`) came along, so the one `toJson` in `OffgridProcessor`
writes exactly what it wrote before.

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

## 2026-08-29 — `googleMap.file` is gone from the data

A map that pointed at a page's file was GettingLost's way of saying "centre on that place",
and 19 of the 20 entries pointed at their own page. The 20th, on
`rathtrevor-beach-park-campground`, pointed at `rathtrevor-beach-park.html`, which is not a
page in offgrid — so it centred on nothing that exists here.

Every `file` is removed, and the 9 entries that had nothing left but the pointer take
`"zoom": 13`. The 12 that stated their own zoom keep it. A map now carries a zoom and its
pins, and the coordinates come from the page's own `location` when the entry has none — which
is the same answer the self-pointer was giving, without the indirection.

`GoogleMap.file` and `GoogleMap.locationId` stay on the class: the POJO is the shape of the
file, not a list of the keys today's data happens to write.

## 2026-08-30 — Morton Lake and Sproat Lake are campgrounds

Both pages carry the `campground` block, so both moved from `destinations/parks/` to
`destinations/campgrounds/` — templates and data folders — and their two pointers in
`shared/browser/destinations.json` moved with them. This is the same ruling Elk Falls and
Rathtrevor took on 2026-08-28: a campground inside a park is a campground page, and the park
it sits in is a separate subject.

`campgrounds` binds the campsite class, which is where `campgroundData` lives, so the block
resolves whether or not a page carries the data.

`parks` had its own class at the time. It binds `DestinationPage` since 2026-08-31; a park
with no campground in it lands there.

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

## 2026-08-31 — A destination has an access, or it does not

```
PageData            name, featuredImage, excerpt, tags, noteList,
                    photoGalleries, relatedDestinationList
  MaintenancePage   actualList
  PostPage          date
  DestinationPage   location, googleMap, access
    LakePage        fishingReferences
    CampsitePage    campgroundData
```

`AreaPage` is gone and `access` moved up to `DestinationPage`. A class was the wrong place to
say that a lake or a park has no single spot to drive to: that is a fact about one place, and
the page leaves the field null. `parks` binds `DestinationPage`, a lake adds fishing, a
campsite adds its campground data, and nothing else separates them.

The registry always read this way — a row in `shared/browser/destinations.json` is a plain
map, and a row without `access` is simply a row without it. The Java tree was the only place
the distinction was structural.

`CampSitePage` became `CampsitePage`: campsite is one word.

**The road badge is derived in Java.** `Access.getRoadLimitingLeg()` answers with one `Leg`:
back country as soon as a leg leaves the van, otherwise the hardest drive surface in
`ROAD_RANK` order, and pavement when `legList` is empty. Its km is every leg of that same kind
added together — potholes 3, dirt 5, potholes 2, dirt 1 gives potholes 5 — and null for
pavement. The leg is built, not picked out of the list, because pavement and back country are
not surfaces any leg names.

It has to be Java: `ROAD_RANK` and `NON_DRIVE_LEG_TYPES` live in `gl-constants.js` and
Thymeleaf cannot read them. So the vocabulary and the derivation now exist twice, in `Access`
for server-rendered pages and in `browser.js` for the gallery cards the browser assembles —
the same split the link rule carries, for the same reason.

`fragments/block/tags.html` reads that one leg, writes `data-road` with no colour, and
`GL.paintTags` finishes it from the one palette. The tag row now draws when there are badges
**or** a road, with an empty left group when there are no badges, which is what the
GettingLost renderer did.

**`Leg.km` is a `Double`.** With a primitive, a leg whose JSON states no km was
indistinguishable from a leg measured at zero, and the JS derivation already treats the first
as a data error — unpaved asserts a measured tail. The Java side can now see the difference.

## 2026-08-31 — The JSON ships inside the jar, beside its HTML

The page data is a resource like the template it belongs to. `resources/data/` mirrors
`resources/templates/`, both are packaged into the fat jar, and content reaches the server the
way the code does. Nothing sits on disk beside the jar and nothing is mounted.

This closes the question the *Content lives in the repo* entry left open. A data file is not a
separate kind of thing to be deployed on its own terms — it belongs where its HTML belongs.

## 2026-08-31 — A blog is composed of posts

Both words are right, and they name different things. The blog is the collection: one page,
at `/blog`, and the menu item that points at it. A post is one item in it, served at
`/posts/{name}`, authored under `templates/posts/` with its data under `data/posts/`.

So there was never a word to choose between. The folder, the template folder and the route say
`posts` because that is what they hold; the menu says Blog because that is what it opens.

## 2026-08-31 — The MCP endpoint is hand-rolled

Three ways in, and they are layers rather than alternatives: write the protocol, take the MCP
Java SDK, or take the Spring AI starter, which wraps the SDK and fills its tool registry from
annotations.

Hand-rolled wins because the surface is small — one URL answering POST, GET and DELETE, and a
switch over `initialize`, `notifications/initialized`, `ping`, `tools/list` and `tools/call` —
and because it keeps the shape the site already has: a controller that routes, a processor that
speaks the protocol, and a third class that knows the content. Nothing about the protocol is
hidden from Pierre, and the cost accepted is that a revision of the specification is ours to
follow.

`McpOffgrid` is that third class, and it holds what the server *is* — the name, the version and
the instructions `initialize` publishes. What the server can be *asked* is one class per tool.

**Sessions are kept, and the id is the processor's to mint.** Stateless would do for read-only
answers, but the customer is on the road and what he told the server a moment ago — where he is,
above all — is worth remembering for the length of a conversation. `McpOffgrid.initialize`
answers what the server *is*; the id, the session map and the 404 on an id nobody knows belong
to the protocol, so they stay in `McpProcessor`.

The session is the client application's, not the chat window's: Claude Desktop initializes its
servers when the app starts and every conversation in it rides that one session. Nothing in a
tool call carries a conversation id, so per-conversation memory would have to be a parameter the
model fills in.

## 2026-09-01 — A tool is a class, and the framework is what it never sees

Writing a new tool is writing one class: extend `AbstractMcpTool`, name yourself and your
arguments in the constructor, name the class the arguments arrive as, and answer with the text
the model reads. It is a `@Component`, so Spring hands every one of them to `McpProcessor`, which
keys them by the name they publish — nothing is registered and no list is edited.

A tool declares an argument by calling `addEnumArgument` or `addTextArgument`, and the base class
builds the `McpTool` and its JSON Schema. The rejected alternative was a tool returning its own
entry as a JSON string: that moves protocol knowledge *into* the tool — the author now writes
`type`, `properties` and `enum` by hand with no compiler — and puts JSON back in the middle of the
code. The cost of the way taken is that an argument is described twice, once in the
`addArgument` call and once as a field of the tool's arguments class.

`ping` and `notifications/initialized` are the protocol asking whether the server is alive, so
`McpProcessor` answers them itself. They were in `McpOffgrid` only because that class had become
everything that was not an envelope.

**`pojo/mcp` is split three ways, so it is clear which classes a new tool touches.** `wire/` is
every shape that goes on the wire, `server/` is what only this server uses — `McpErrorCode` beside
`wire/McpError` is the pair that names the difference — and `tool/` is what a tool author works
in. The folder is `wire` rather than `external` because `external` already means a supplier's
data in this repo.

## 2026-09-01 — The customer's state is a property bag

A tool that answers by where the customer is needs the state the session holds, and `McpOffgrid`
was forbidden to see a session — so the state had nowhere to be read. What a tool is handed is
`McpCustomer`: the state alone, never the session, so the id and the stream stay with the
protocol.

It is a bag of keys rather than a class of typed fields because the tools are not all going to be
in this jar. Two jars cannot each add a field to one class, and neither can subclass the other's.
Field *order* is not the reason — Java resolves a field by name, not by offset — the reason is
that there is one class and two owners.

Typed accessors are what keep the casts out of the tools. A jar's accessors go in a class of its
own that holds the customer and reads its own keys back typed; a subclass of `McpCustomer` would
not do, since the processor mints the instance and two jars would fight over which subclass it is.
A key is named for the tool that writes it, so two jars choosing the same word do not silently
share one slot.

## 2026-08-31 — MCP works in Java objects, Gson only at the edge

Neither `McpProcessor` nor `McpOffgrid` handles a `JsonObject`. They read and build Java
objects; Gson turns a message into one on the way in and back into JSON on the way out, and
that is the only place it appears.

The classes are in `pojo/mcp/`, one per shape the specification names, and the wire key wins
where it disagrees with a Java name — `@SerializedName("enum")` over `enumList`, `"tools"`
over `toolList`. A tool's own arguments are not the protocol's to describe, so they stay a
`Map<String, Object>` on `McpParams` and are read by name.

**MCP builds its own Gson.** JSON-RPC requires the answer's `id` to be identical to the
request's, and the client chooses whether that id is a string or a number. The shared
`BaseFileHandler` Gson parses every number as a Double, so a client's `7` would be echoed as
`7.0` and it would never match the call it made. Built with
`ToNumberPolicy.LONG_OR_DOUBLE`, an integral id stays integral. Its own instance also keeps
`setPrettyPrinting` off the wire.

## 2026-08-31 — One error, one place: McpErrorCode decides the whole refusal

Every way the server says no goes through `McpErrorCode`. The constant carries three things
that used to be decided apart: the JSON-RPC number, the HTTP status it answers under, and how
the response is built. A rejection at a call site is one line, and no call site picks a status
any more.

That last part is why the enum exists. `McpProcessor` had a helper per status and an int per
call, and two answers ended up 200 that the transport requires to be 400. The split the enum
now holds: input the server cannot accept at all — not JSON, not JSON-RPC 2.0, no method, no
session header — is 400, while a request that reached its method and failed is a 200 carrying
the error in its body, because the transport did carry it. A session id the server does not
hold is 404, and that status is load-bearing: the specification has the client start a new
session on 404 and nothing else.

**A refusal with no body fits any handler.** The GET that opens the stream and the DELETE that
ends the session carry no JSON-RPC message, so there is nothing for an error body to answer.
Those constants — `SESSION_NOT_FOUND_STREAM`, `SESSION_NOT_FOUND_END`, `STREAM_TAKEN` —
override `makeResponse` and answer the status alone. Spring declares `build()` as
`<T> ResponseEntity<T>`, so a body-less response is honestly generic and a handler returning
`ResponseEntity<SseEmitter>` or `ResponseEntity<Void>` keeps its own signature with no cast.
The base `makeResponse`, which does carry a String body, is the only one that casts.

Codes outside JSON-RPC's five are marked for what they are: what the MCP SDK recommends inside
the server range, and what this server named itself. The two are separate sections in the file,
so nobody has to guess which numbers are ours to change.

**Log levels follow the server, not the client.** A refused session is the server working
correctly and saying no, so it is INFO. WARN and ERROR are for the server's own trouble.

