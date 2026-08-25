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

#4 Menu links go nowhere — Destinations, Blog and the four Hardware items are still
`href="#"`. The three Info items (About, Useful links, Useful contacts) are wired.

#5 Footer "Last modified" is a placeholder — no source decided for the date.

#6 MCP controller — not started. The Client controller is the only one that exists.

#7 Content from GettingLost — the JSON is in (56 files) and converted. The HTML content and
the images are not.

#8 Reading the JSON — the classes exist, nothing reads them yet. Settled: content lives in
the repo; `resources/data/` mirrors `resources/templates/`, a folder per template that needs
data; six page classes and seventeen parts under `com.lc.offgrid.pojo`. Still open: whether
the data rides inside the jar or on disk beside it, whether it is loaded at startup or per
request, and what picks the page subclass from `tags.typeList` plus the folder.

#9 Getting content onto the server — settled for JSON and HTML: they live in the repo and
arrive by push and rebuild. No mounted volume needed. Still open for images only, and
deliberately deferred.

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

#14 `van/maintenance/*` (2 files) carry an `actual` block — dated service records with
odometer, shop, cost and next-due. No class reads it. Left in place; it is real content, not
scaffolding.
