# Todo

**next id: 41**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#1 FullHost account — signed up, but locked out. The password reset sends nothing to the
address that verified the account, and FullHost has no phone support. The way in is a guest
ticket at https://manage.fullhost.com/submitticket.php or the sales form, both of which work
without logging in. Worth asking in the ticket whether the account was ever fully
provisioned — a signup that stalled after email verification would leave no client record to
reset a password against, which looks exactly like this.

#2 Deploy the skeleton to FullHost — validates that their build node builds this repo and
runs the jar, while nothing is invested in it.

#4 Blog is the last dead menu link — `href="#"` in `fragments/site/menu.html`, while the six
posts link back to `/blog` and a post is served at `/posts/{name}`. Same decision as #26.
Everything else in the menu is wired and served.

#6 MCP controller — not started. `OffgridController` is the only web controller.

#7 Content from GettingLost — the JSON, the HTML and the images are all in. What is left is
authoring, not porting: `hardware/howto/water.html` is still the placeholder text. The images
are in the folder `folder.image` names, which is a local path only — see #9.

#8 Reading the JSON — `readFile` reads it per request, and the mapping method names the
class: `PageData` for info and hardware, `MaintenancePage`, `PostPage`. Settled: content
lives in the repo; `resources/data/` mirrors `resources/templates/`, a folder per template
that needs data; a class per page kind and the parts under `com.lc.offgrid.pojo`. Still
open: whether the data rides inside the jar or on disk beside it, and whether the
destinations — where one folder holds several kinds — can be served the same way, since a
single `/destinations/{folder}/{name}` cannot name a class the way the hardware routes do.

#9 Getting content onto the server — settled for JSON and HTML: they live in the repo and
arrive by push and rebuild. No mounted volume needed. Settled 2026-08-25 for images too:
they live in the folder `folder.image` names, outside the resource tree, and are served
through `/image/{imageName}`. What is still open is how that folder gets onto the server,
which the local profile does not answer.

#16 `BaseWebController` returns the view name `exception` when a handler throws, and no
`exception.html` exists — so the failure renders as the container's own error page rather than
the site's. A URL that matches no mapping never reaches that catch at all.

#17 A gallery's heading is authored twice — `templates/hardware/van.html` and
`templates/hardware/bronco.html` each write `<summary><h2 class="gl-heading">…</h2></summary>`
above the gallery call, while `photoGalleries.<key>.name` in the JSON says the same thing and
nothing reads it. Decide which is the source. The fragment renders tiles only, so if the JSON
wins, something else has to put the heading on the page.

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

#25 The maintenance work sheets have no files. `/document/{documentName}` is served now, off
`LocalFileManager` (`<folder.local>/Documents`). `workUrl` in
`resources/data/hardware/maintenance/van/m-van.json` and `.../bronco/m-bronco.json` names
three PDFs; none of them is in that folder yet.

#26 Blog or posts — the folder, the template folder and the route all say `posts`, the menu
item says Blog, and the six posts link back to `/blog`. A post is served at `/posts/{name}`.
Decide which word the site uses in a URL before either name is public.

#30 The external downloads have no smart accessors. `FeatureGeometry.getCoordinates()` hands
back a raw `Object`, so nothing answers where a rest stop is without indexing the list itself,
and a rest stop's nearest town is still prose. The parse for it: 204 of the 219 values of
`DISTANCE_FROM_MUNICIPALITY` read as `<distance> KM <direction> OF|FROM <town>`; of the 15 that
do not, 8 name no town at all (`2 KM`, `13.256`, `10`, `AT BC/YUKON BORDER`, `TOP OF KOOTENAY
PASS`, and three ferry terminals).
