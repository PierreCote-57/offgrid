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

## 2026-09-05 — The size carries its own box, and the compressor is a workbench

**`ImageSize` holds the pixels.** Each constant is built with the width and height of the box
an image fits inside — contain, never crop, and never enlarged past what the file carries, so
only one of the two numbers binds and which one depends on the shot. Nothing outside the enum
computes a number, and the numbers themselves are in the source rather than repeated here.

The boxes came from measuring the CSS box each caller draws into and doubling it for a retina
screen. Several callers share a name — a gallery thumbnail, a browser card and a map info
window are all Small — so a name's box is the largest of the places that use it. `Z`, a single
largest-dimension number as `sips -Z` takes, was rejected: it overshoots a portrait in a
landscape box by half again, for pixels no browser draws.

What this buys is bytes on the wire and nothing else. The browser already fits the native file
into the same box, so no page looks different; the file just stops being megabytes. Bytes track
stored pixels with no knee anywhere in the range, so every doubling of a box costs four times
the transfer.

**`ImageCompressor` is a workbench, newed per use, not injected.** It holds the file it opened
and the pixels it is working on; `open` and `modify` are `void` steps on that state, and only
`toResource` and `write` produce something new. An earlier draft had the steps both mutate the
state and return the image, which left a caller unable to tell whether the value it held was
its own or the compressor's — and `modify` handed back the very object `open` had produced
whenever the image already fitted. Stateful means it cannot be a shared singleton the way
`ImageFileManager` is, which is the trade the shape accepts.

**A derived file is named by folder and by suffix, and `makeFileTo` is static.** Deciding where
a copy lands is separate from making one, so it answers a `File` without opening anything.
`ImageSize.resize` passes the lower-cased size name as both, putting `van/IMG_1234.jpg` at
`small/IMG_1234-small.jpg`: the folder alone would collide in `AbstractFileManager`'s name map,
which keys on the base name across the whole tree and would let a 640px copy answer to the
native's name. Naming neither throws rather than writing over the original.

**Known gaps in what is built.** `open` uses `ImageIO.read`, which ignores EXIF orientation, so
a phone portrait comes back sideways. `toResource` always writes JPEG while the suffix form
keeps the source's extension, so a PNG source would produce JPEG bytes under a `.png` name.


## 2026-09-16 — A drawing the site ships is a resource; a photograph is not

`static/images/` is new, and `magnitude-scale.svg` is the first thing in it. It is served by
Spring's own static handler at `/images/{name}` and has nothing to do with `/image/{name}`,
which stays what the 2026-08-25 entry made it: the reader of `folder.local/images`.

**The line is where the file comes from.** A photograph arrives after the build, carries EXIF,
is asked for by size and opens in a lightbox — it is content, and content lives outside the jar.
A drawing is authored with the page that places it, changes when that page changes, and has no
size variants to make. Shipping it in the resource tree means the page and its drawing are one
commit and one deployment.

Rejected: putting the drawing under `folder.local/images` so a single route answers for every
image. One route is tidy, but it would make a file that belongs to a page deploy by hand,
separately from the page, with no version tying the two together.
