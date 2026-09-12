# Decisions — Look

What was decided and why. Rationale has no other master — the repo shows *what* the code
is, never *why* it is that.

The palette, the paper textures, the wordmark, type and lengths, the lightbox and the
warning sign.

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

**The 1024 is what a page lays out in** — reversed on 2026-09-12. `main` had
`box-sizing: border-box` and 24px of side padding, so the column measured 1024 and a page
laying blocks out in it had 976. A stated width is the width there is to use, or every sum a
page makes is short by an amount it cannot see. The side padding is gone: `main` is 1024 and
gives 1024. A page that wants its content held off the edge states that itself.

## 2026-08-25 — Paper, not white

The site felt bland, and the cause was written at the top of the stylesheet as policy: every
decorative colour a shade of the bar green, with orange and red locked away for messages. On
a site about forests and gravel roads that spent the whole palette on one hue.

Three paper treatments now, one stylesheet, scoped by what the element is:

- **Wavy** — topographic contour lines, pale on the bar and footer, green on the page. Two
  `url()` data-URIs held in custom properties, so the drawing exists once.
- **Grid** — quadrille at 15px, one weight, on `.og-checklist` and `.og-numcheck`. Not
  scientific graph paper: no fine sub-grid, no heavier majors.
- **Lined** — horizontals at 30px on `.og-post`, with the copy sitting on the rulings.

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

The diamond is inline SVG carrying geometry and three class names — `og-field`,
`og-border`, `og-mark` — with every colour in `site.css`. A drawing whose colours are baked into the markup
cannot follow the palette.

The panel reuses `--warn-soft` rather than mixing a fourth cream from the sign orange. Two
new properties were needed: `--sign-orange`, louder than the `--warn` a message bar uses,
and `--sign-ink`, the warm near-black of printed sheeting.

This is where the parked signage typography first lands — the `Warning` kicker is condensed
uppercase, letterspaced, in rust. The rest of it stays parked.

Alternatives drawn and rejected: the sign bare on the paper with no panel, mounted on two posts above the text, a hazard-tape strip with a small chip, and a
worded orange panel with no symbol at all.
