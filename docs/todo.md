# Todo

Parked work. Side issues found mid-task land here rather than derailing the task.

#1 FullHost account — signed up, but locked out. The password reset sends nothing to the
address that verified the account, and FullHost has no phone support. The way in is a guest
ticket at https://manage.fullhost.com/submitticket.php or the sales form, both of which work
without logging in. Worth asking in the ticket whether the account was ever fully
provisioned — a signup that stalled after email verification would leave no client record to
reset a password against, which looks exactly like this.

#2 Deploy the skeleton to FullHost — validates that their build node builds this repo and
runs the jar, while nothing is invested in it.

#4 Menu links go nowhere — Destinations, and Howto and Checklist under Hardware, are
still `href="#"`. Blog is still `href="#"` and the six posts now link to `/blog`. The van, the Bronco and the three Info items are wired. Van maintenance
and Bronco maintenance point at `/hardware/maintenance/{name}`, which nothing serves yet.

#5 Footer "Last modified" is a placeholder — no source decided for the date.

#6 MCP controller — not started. The Client controller is the only one that exists.

#7 Content from GettingLost — the JSON is in (56 files) and converted. The HTML content is
in for the two checklists, the van, the Bronco, the two maintenance pages, the six posts and
all 24 destinations; the six howto pages and all the images are not.

#8 Reading the JSON — `readFile` reads it per request, and the mapping method names the
class: `PageData` for info and hardware, `MaintenancePage`, `PostPage`. Settled: content
lives in the repo; `resources/data/` mirrors `resources/templates/`, a folder per template
that needs data; seven page classes and eighteen parts under `com.lc.offgrid.pojo`. Still
open: whether the data rides inside the jar or on disk beside it, and whether the
destinations — where one folder holds several kinds — can be served the same way, since a
single `/destinations/{folder}/{name}` cannot name a class the way the hardware routes do.

#9 Getting content onto the server — settled for JSON and HTML: they live in the repo and
arrive by push and rebuild. No mounted volume needed. Settled 2026-08-25 for images too:
they live in the folder `folder.image` names, outside the resource tree, and are served
through `/image/{imageName}`. What is still open is how that folder gets onto the server,
which the local profile does not answer.

#10 **Standing rule — convert every JSON file brought in from GettingLost.** GettingLost
keeps the old spellings and its own consumers still read them; offgrid does not. The
conversion happens on the way in, as part of the copy, never afterwards. Everything already
in `resources/data` is converted.

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

Nothing in `com.lc.offgrid.pojo` maps key names — a file that arrives unconverted binds its
renamed fields to null rather than failing, so the miss is silent.

#16 A URL that does not resolve lands on the `exception` view. It survives and it logs,
which is the requirement; the page itself is not fit to look at yet.

#17 A gallery's heading is authored twice — the page writes
`<h3 class="gl-heading">Listing pictures</h3>` while `photoGalleries.<key>.name` in the JSON
says the same thing and nothing reads it. Decide which is the source. The fragment renders
tiles only, so if the JSON wins, something else has to put the heading on the page.

#20 `OffgridImageManager.getImageMetadata` reads the file on every call — no cache. Fine
while nothing calls it; it is a disk read per request the day something does.

#21 `photo-ref` still renders text. It takes a `gallery/itemId`, not a filename, so unlike
the other two photo blocks it cannot be pointed at `/image/` without a resolution step
against `photoGalleries`.

#22 A thumbnail and its lightbox load the same file. GettingLost split them — a small
Photon URL for the grid, the 1920 cap for the overlay — and offgrid has one URL per image,
so the grid pulls full-size originals. This is where a resize seam goes.

#23 Signage typography is parked, not dropped — condensed uppercase headings, a letterspaced
kicker in amber, a route-shield chip. Revisit when there are buttons or controls to carry it.
The sample is the `signage` block in an earlier revision of `_preview/samples.html`.

#24 `ImageMetadata.cameraDirection` will be null on almost everything that is not a phone.
Standalone cameras do not write `TAG_IMG_DIRECTION`, so any UI built on it needs the absent
case to be the normal one.

#25 The two maintenance work sheets have no file and no route. `workUrl` in
`maintenance/van.json` and `maintenance/bronco.json` name `van-2026-08-17.pdf` and
`bronco-2026-09-19.pdf`; neither is in the repo, and nothing serves documents the way
`/image/{imageName}` serves images. The record links them at `/document/{documentName}`, so
the two fixes are the missing route and the missing files.

#26 Blog or posts — the folder, the template folder and the route all say `posts`, the menu
item says Blog, and the six posts link back to `/blog`. A post is served at `/posts/{name}`.
Decide which word the site uses in a URL before either name is public.

#29 `data/shared/browser/browser.json` has an empty `name`, so the browser page's `<h1>` is
blank and its tab reads `— Going offgrid`. Every other page's name comes from its JSON.

#30 The external downloads have no smart accessors. `FeatureGeometry.getCoordinates()` hands
back a raw `Object`, so nothing answers where a rest stop is without indexing the list itself,
and a rest stop's nearest town is still prose. The parse for it: 204 of the 219 values of
`DISTANCE_FROM_MUNICIPALITY` read as `<distance> KM <direction> OF|FROM <town>`; of the 15 that
do not, 8 name no town at all (`2 KM`, `13.256`, `10`, `AT BC/YUKON BORDER`, `TOP OF KOOTENAY
PASS`, and three ferry terminals).


#31 Two spellings of a page pointer now coexist. `shared/browser/destinations.json` carries
folder paths with no extension (`destinations/rec-sites/echo-lake-dayuse`), while the
individual page JSONs still carry bare filenames: 22 `file` values across the destination
pages, one more at `hardware/bronco/bronco.json:222` (`logging.html`), and 17 internal `url`
values linking a destination to a sibling (`lakes/echo-lake/echo-lake.json:57` ->
`echo-lake-dayuse.html`). The two Canadian Tire `url` values in `bronco.json` also end in
`.html` and are external.

#32 Nothing serves a destination page. The gallery card's href is `row.file` verbatim
(`browser.js:145`), so it is now a relative `destinations/rec-sites/echo-lake-dayuse` against
`/shared/browser`, and `OffgridController` has no mapping under `/destinations`.

#33 Gallery cards never draw badges. `browser.js:237` reads `(row.tags || {}).badges`, but
every row carries `tags.badgeList` after the #10 conversion — `hydratePageList` merges the
page JSON verbatim and renames nothing. The same file already reads `badgeList` at lines 461
and 565, so only the card renderer is on the old spelling.

#34 The `photo` fragment drops a photo's GPS caption. GettingLost's photo block took
`data-lat`/`data-lng` and rendered a DMS caption linking to Google Maps; the fragment takes
the filename only. `destinations/lakes/echo-lake.html` has the one call that used it —
49.984331, -125.413606 on the third photo — and now shows the picture with no caption.

#35 The tags row has no road badge. GettingLost drew badges LEFT and the road badge RIGHT,
derived from `access.legList` — worst leg type wins the word, every leg of that type sums to
the km. `PageData` has no `access` and `OffgridController.destination` binds `PageData`, so
the fragment cannot reach it; `fragments/block/tags.html` leaves the right-hand group empty
and `.gl-tagrow-km` is in `site.css` with nothing writing it. The vocabulary and the
derivation exist in `gl-constants.js` (`ROAD_RANK`, `NON_DRIVE_LEG_TYPES`, `ROAD_COLORS`) and
`browser.js:166` (`deriveRoadBadge`), which is what the gallery cards already use.

