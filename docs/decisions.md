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

- `fragments/site/` — chrome every page gets whether or not it asked for it: header, footer.
- `fragments/block/` — content a page deliberately places inside itself.

The day that boundary blurs is the day the split stops helping, so it is written down here
rather than left to be inferred from the folder names.

Fragment files hold only their markup, with no `<html>`/`<body>` wrapper.

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
by the rename pass in todo #10.

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
GettingLost original, so the rename table in todo #10 is what verifies a copy instead.

`MapIcon` still carries `@SerializedName`, on its constants. That maps enum *values*, not
key names, and is unaffected.

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
included. The rename table is in todo #10; the structural moves were `links` into
`campgroundData.referenceList`, `location.zoom` onto the googleMap entries that had none of
their own, `tags: []` deleted from the 8 van files, empty `categories` deleted from the 6
posts, and four posts' `googleMap` becoming `relatedDestinationList`.

Two rounds, deliberately. The first pass renamed and moved but deleted nothing that had
content in it, and reported what it had left behind. Pierre then ruled which of those were
obsolete — the two posts pointing at locationIds with no page, and the four park files still
carrying `access` and `campgroundData` — and those were removed in a second pass.

**Deleting content is his call, not the migration's.** A pass that renames can run
unsupervised; a pass that drops a block someone wrote cannot. `actual` in the two van
maintenance files is still there for that reason (todo #14), as is `wpSettings` in all 52.

One `campground` key survives on purpose: it is the name of a `googleMap` entry in
`morton-lake-park`, a map of the campground inside the park, not the data block.

Every top-level key in the 52 page files now binds to a class except those two.

## 2026-08-25 — A URL is the view name

`/about/useful-links` renders `templates/about/useful-links.html`, whose data folder is
`data/about/useful-links/`. The URL, the view name and the mirror path are the same string,
so adding a page is a template, a `@GetMapping` and a `process*` method — nothing else to
keep in step. Where a parameterized route already covers the shape, it is a template and its
folder, and there is no Java to write at all.

The three Info pages are the first to use it. `/about` is a heading, the header and the
footer, and nothing else; the two Useful pages render their `noteList`.

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

A description is a `List<String>` and each entry is one `<p>` with no margin, so the lines
stack the way they are written. A blank string is therefore a blank line, not a paragraph
break — the data says where the breaks go, the stylesheet does not guess.

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
`/about/useful-anything` reaches `readFile`, throws, and is caught into the `exception`
view. That is the correct outcome, not a hole to plug.
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
