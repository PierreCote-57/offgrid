---
name: booklet
description: Build the checklist or howto booklet PDF — cover, table of contents, one page per page's howto section — into {folder.local}/document/, where /document/<name> serves it. Use when Pierre asks to build, rebuild or refresh a booklet, e.g. "rebuild the checklist booklet", "make the howto PDF".
---

# Booklet PDF for one kind

The input is a kind, `checklist` or `howto`. The output is `{folder.local}/document/<kind>.pdf`,
the file the browser's booklet link opens.

## Run

```bash
python3 docs/skills/booklet/build_booklet_pdf.py <checklist|howto> [output.pdf]
```

Run it from the repo root, one kind per run. `folder.local` is read from
`application-dev.yaml`. The script needs `reportlab` and `beautifulsoup4`.

## What goes in

The pages the browser dataset `data/shared/browser/van-<kind>.json` lists, in its order, whatever
their `pageState`. Only the
`<section data-howto-section="howto">` is read. How a list renders follows its class, as on the
web: `og-checklist` gets a box, `og-numcheck` a box and a number, a plain `<ol>` a number, a
`<ul>` a bullet. Of the fragment calls inside a section, `photo-ref` becomes its caption,
`warning` becomes a callout and `gallery-photo` becomes its photo at the width its box's style
gives, adjacent floated photos sharing one row; the rest are dropped.

The builder is [build_booklet_pdf.py](booklet/build_booklet_pdf.py); what it does beyond this is
in its code.

## After

Tell Pierre the file was written and its page count. The FullHost copy of the PDF is his to
upload.
