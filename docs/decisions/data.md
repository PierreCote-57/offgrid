# Decisions — Data

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

Everything in `resources/data`: the mirror rule, the POJOs that read it, the vocabularies
and the external downloads.

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

    badges      -> badgeList          keywords    -> keywordList
    types       -> typeList           legs        -> legList
    notes       -> noteMap            amenities   -> amenityList
    displayName -> label              location_id -> locationId

    haversine   -> haversineMap, keyed by town
    list, items -> the value inside a map rather than a list of its own: a note block's
                   entries sit under its heading, a gallery's under each item's id

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

`com.lc.offgrid.pojo.page` holds the page classes; `com.lc.offgrid.pojo.part` holds the blocks
they are built from. A page class is something a URL resolves to. A part is never a page on
its own.

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

## 2026-08-26 — MaintenancePage, and the record the JS used to draw

`maintenance.jst` fetched the page JSON in the browser and built the table from a `COLUMNS`
list. That work is now split the way the rest of the site is: `MaintenancePage` carries
`List<MaintenanceEntry>`, `fragments/block/maintenance-actual.html` draws it, and
`site.css` holds every rule the JS used to set inline.

The block reads the model and takes no parameter — a page that wants the record sets
`actualList`.

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
in `OffgridWebProcessor` did not move, and `getGson()` delegates as well. The builder settings
(`setPrettyPrinting`, `disableHtmlEscaping`) came along, so the one `toJson` in `OffgridWebProcessor`
writes exactly what it wrote before.

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

## 2026-08-31 — A destination has an access, or it does not

```
PageData            name, featuredImage, excerpt, tags, noteMap,
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

## 2026-09-01 — A keyed part is a Map, and the key is the name

Four parts were a list of objects whose first field was really a key: `haversineList`
(`{town, km}`), a gallery's `itemList` (`{id, img, label}`), `lakeChartList` (`{name, url}`)
and `noteList` (`{sectionName, itemList}`). Each is now a map — `haversineMap`,
`photoGalleries`' inner map, `lakeChartMap`, `noteMap` — and the field that repeated the key
is gone with it.

So are the classes. `TownDistance`, `LakeChart`, `Gallery` and `NoteSection` held nothing but
the pair or the collection once the key moved out, and a wrapper around one collection is not
a class. `GalleryItem` survives on `img` and `label`.

**The field is declared `TreeMap`, not `Map`.** Gson builds the type the field names: a `Map`
field gets a `LinkedTreeMap`, which is whatever order the author last typed. A small map that
sorts is predictable, and nothing here reads these in authored order — a gallery and a lake
chart now render in key order, accepted deliberately.

**A note block's key is its heading**, so the block whose key is empty renders no `<h3>` —
that is the block sitting inside its own `<details>`, whose `<summary>` is already the
heading. A map allows one such block per page, which is all any page has needed. If that ever
bites, it gets revisited then.

`note-list` therefore takes the block's name, and a page carrying several makes several calls
whose order is the order they appear in. Every existing page was converted call for call, in
the order its file listed the blocks; after that, order is the author's. `hardware/howto/water`
called the block with no notes behind it, and the call was dropped rather than given a name
nothing answers to.

The browser dataset carries the same shapes as the page files — inline or through a `file`
pointer, both are read by the same template — so it was migrated with them, and `browser.js`
reads `haversineMap` by town instead of scanning for it.

## 2026-09-01 — One vocabulary per thing, and the vocabulary is an enum

Four closed vocabularies were Strings, and each is now an enum carrying `@SerializedName`, so
the JSON keeps the word it always wrote: `RoadType` on `Leg.type`, `DestinationType` on
`Tags.typeList`, `Badge` on `Tags.badgeList`. `keywordList` stays a `List<String>` — it is the
open one, and that is the whole distinction between it and the other two.

`RoadType` carries ten constants: the five drive surfaces a leg may be authored as, the three
ways of leaving the van, and `PAVEMENT` and `BACK_COUNTRY`, which `Access.getRoadLimitingLeg`
derives and no data file ever writes.

**The severity ranking is not the enum's order.** It follows the van, not the road, so
`ROAD_RANK` stays a `List<RoadType>` in `Access` where another vehicle reorders it in one
place. Reading it off `ordinal()` would have buried a vehicle's configuration in a type.

**The URL segment went singular**, so the type word and the route are one vocabulary rather
than two: `/destinations/lake/echo-lake`, and `lakes`, `rec-sites` and `campgrounds` were
renamed to their singular under both `data/destinations/` and `templates/destinations/`, with
every link and every `file` pointer rewritten. Nothing outside the repo pointed at the old
ones. `park` still has no folder — it is a type with no page yet, not a missing one.

A template renders the word, never the constant: both palettes in `gl-constants.js` are keyed
by what the JSON writes, so `tags.html` lower-cases the constant once into `badgeWord` and
`roadWord`. `SHARP_ROCK` reaching `data-road` would have painted nothing.

**Dates are dates.** `MaintenanceEntry.date` and `nextDueDate` are `LocalDate`; `PostPage.date`
is a `LocalDateTime`, because a post happened at a time of day and a shop visit did not. The
data already carried both shapes correctly and needed no migration. Gson has no opinion about
`java.time`, so `LocalDateAdapter` and `LocalDateTimeAdapter` sit beside the shared Gson in
`BaseFileHandler` and read and write the ISO text. A post's dateline now prints `T21:00`
rather than `T21:00:00`, which is what `LocalDateTime` prints.

## 2026-09-01 — A vocabulary with one owner lives inside it

A part used by exactly one other part is a nested type of it, not a file of its own:
`Access.RoadType` and `Access.Leg`, `Place.MapIcon`, `Tags.Badge` and `Tags.DestinationType`,
`CampgroundData.Reference` and `CampgroundData.ReferenceType`, `Dataset.DatasetOption`. The
shape was already there in `FeatureGeometry.GeometryType`. `part` went from eighteen files to
eleven, and nothing outside Java names any of them — no `T(...)` in a template, no qualified
name in the data — so the move is invisible to everything but an import.

`Reference` and `ReferenceType` are both first-level members of `CampgroundData` rather than
the type nesting inside `Reference`, which would have read three deep for no gain.

**The boundary is the `part` package.** `GalleryItem`, `NoteItem`, `Tags`, `CampgroundData`,
`FishingReferences` and `MaintenanceEntry` each have one owner too, but that owner is a page
class. Following the rule there would move every block into `page` and empty `part`, so it
stops at the package line: a part nests inside a part, never inside a page.
