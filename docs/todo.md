# Todo

**next id: 42**

Parked work. Side issues found mid-task land here rather than derailing the task.

Ids are permanent and never reused. The list is never renumbered — a deleted entry leaves a
gap, and that is correct, because an id has to still resolve when it is cited later. Take the
next id from the header above and increment it. This numbering is independent of the one in
`~/Claude/todo.md`.

#1 FullHost. Three parts, one job:
 - The account is signed up but locked out. The password reset sends nothing to the address
   that verified it and there is no phone support; the way in is a guest ticket at
   https://manage.fullhost.com/submitticket.php or the sales form, both of which work without
   logging in. Worth asking whether the account was ever fully provisioned — a signup that
   stalled after email verification leaves no client record to reset against.
 - Deploy the skeleton, to prove their build node builds this repo and runs the jar while
   nothing is invested in it.
 - Get the image folder onto the server. The folder `folder.image` names lives outside the
   resource tree and has no delivery path; JSON and HTML arrive by push and rebuild.

#4 Build the blog listing and wire the menu. `fragments/site/menu.html` still has
`href="#"` for Blog, while the six posts already link back to `/blog` and a post is served at
`/posts/{name}`. Everything else in the menu is wired and served.

#6 MCP controller — not started. `OffgridController` is the only web controller.

#7 Author `templates/hardware/howto/water.html` — it is still the placeholder text the port
left behind. Everything else that came from GettingLost is in.

#17 The `photo-gallery` port dropped the gallery heading. GettingLost's `photoGallery`
renderer in `gettinglost.jst` treats `photoGalleries.<key>.name` as required, builds the
`gl-heading` from it, and can wrap heading and grid in `<details><summary>` itself. The
offgrid fragment renders tiles only, so `templates/hardware/van.html` and
`templates/hardware/bronco.html` type the same words into their own `<summary>` and nothing
reads the JSON's `name`. The JSON was always the source.

#22 A thumbnail and its lightbox load the same file. GettingLost split them — a small
Photon URL for the grid, the 1920 cap for the overlay — and offgrid has one URL per image,
so the grid pulls full-size originals. This is where a resize seam goes.

#25 The maintenance work sheets have no files. `/document/{documentName}` is served now, off
`LocalFileManager` (`<folder.local>/Documents`). `workUrl` in
`resources/data/hardware/maintenance/van/m-van.json` and `.../bronco/m-bronco.json` names
three PDFs; none of them is in that folder yet.

