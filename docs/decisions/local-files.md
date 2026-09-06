# Decisions — Local files

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

Everything under `folder.local` — the `images/` and `documents/` roots that live outside
the jar, and how they are delivered.

## 2026-08-25 — Images live outside the resource tree, behind `/image/`

Pictures are not in the repo. They sit under `folder.local`, the app's own root on the
machine, in `images/` — `documents/` and `logs/` are its siblings, one file manager to
each. They reach the browser through `GET /image/{imageName}` — a controller method that
returns `ResponseEntity<Resource>` and never goes through `processRequest`, because it
answers with bytes rather than a view name.

**The URL carries a bare filename, not a path.** That is already what the JSON holds, and it
leaves the folder layout under `images/` free to be rearranged without touching a single
data file. The cost accepted: the server resolves name to file, and two files with the same
name in different folders are ambiguous.

The prefix is written once, in the fragments, as `@{/image/{imageName}(imageName=${img})}`
rather than a literal string — `@{}` prepends the servlet context path and URL-encodes the
substitution, which matters the first time a filename has a space in it.

`LocalFileManager.ImageFileManager` is the singleton that knows where a name lands;
`ImageMetadataExtractor` reads EXIF through metadata-extractor 2.19.0 and flattens it into
`ImageMetadata`. Drew Noakes over Apache Commons Imaging for the read path: Commons sat in
`1.0-alpha` for about a decade and its makernote coverage is narrower. Commons is the
better bet the day metadata has to be written back.

## 2026-09-04 — An image is asked for by size, not by pixels

**The size rides as `?size=` on the existing `/image/{imageName}` route**, in three named
steps — Small, Medium, Large — rather than as a pixel width. A caller states the ceiling it
is drawing into and nothing computes a number: no device-pixel-ratio doubling in a template,
no rounding rule shared between a page and the server. Rejected: a width in file pixels with
the caller doubling for the screen and the server rounding up, which put arithmetic in four
places that would have to agree.

**The rule per caller is the shape of the thing, not the page it is on.** Anything in a grid
asks Small — a gallery thumbnail, a browser card, a map info window. A photo standing in a
page asks Medium. The lightbox asks Medium too, with a "Full size" link under the image, so
the overlay loads what a screen shows and the original stays one click away.

**A size the enum does not know is `Native`, not a refusal.** The controller takes the
parameter as a String and calls `ImageSize.of`, which ignores case and answers `Native` for a
name it does not carry, and for no name at all — so "no size stated" and "a size nobody
recognises" both mean the file itself, and the processor never holds a null size. `Native`
carries a box past any real photograph and overrides `resize` to answer the source file, since
a copy of it would only be a re-encoding. Spring's own String-to-enum converter was rejected:
it is case-sensitive and turns an unknown value into a 400, and the lenient rule would have
lived in a `WebMvcConfigurer` on the far side of the app instead of on the enum a reader is
already looking at.

**The anchor a lightbox opens from stays bare.** `entryOf` in lightbox.js is the one place the
overlay's URLs are built — the clicked image and every arrow step — so the size the overlay
wants belongs there and not in the two fragments that write the link. The href being a plain
image URL is what still works with JavaScript off, and with no size on it that click gets the
native file.
