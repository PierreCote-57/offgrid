#!/usr/bin/env python3
"""Build a half-letter (5.5 x 8.5 in) booklet: cover + one page per source page.

All content (title, intro, group headings, steps) is read live from the page
templates the browser dataset data/shared/browser/van-<kind>.json lists, in its
order -- nothing is duplicated here. Only the <section data-howto-section="howto"> block of each
page is used, its gallery-photo images included. A page with no such section is a title-only
page (producing the block is the page author's job).

Two booklets are defined in BOOKLETS below and selected on the command line:

    python3 docs/skills/booklet/build_booklet_pdf.py checklist
    python3 docs/skills/booklet/build_booklet_pdf.py howto

Add a path to override the default output, {folder.local}/document/<kind>.pdf:

    python3 docs/skills/booklet/build_booklet_pdf.py howto _preview/howto.pdf

Requires: reportlab, beautifulsoup4
    pip install --user reportlab beautifulsoup4
"""

import copy
import json
import os
import re
import sys
from xml.sax.saxutils import escape as xml_escape

from bs4 import BeautifulSoup
from reportlab.lib.units import inch
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, PageBreak, Table, TableStyle, Flowable, Spacer, Image
)
from reportlab.platypus.tableofcontents import TableOfContents
from reportlab.lib.utils import ImageReader

HALF_LETTER = (5.5 * inch, 8.5 * inch)
PAGE_MARGIN = 0.5 * inch
TEXT_WIDTH = HALF_LETTER[0] - 2 * PAGE_MARGIN
PHOTO_MAX_HEIGHT = 4 * inch

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
RESOURCES = os.path.join(REPO_ROOT, "src", "main", "resources")


def _p(*parts):
    return os.path.join(RESOURCES, *parts)


def _yaml_value(file_name, section, key):
    """The value of <section>.<key> in one of the application yaml files: the
    `key:` line among the indented lines under a top-level `section:`."""
    with open(_p(file_name), encoding="utf-8") as f:
        yaml_text = f.read()
    pattern = r'^%s:\s*\n(?:[ \t]+.*\n)*?[ \t]+%s:\s*"?([^"\n]+)"?' % (re.escape(section), re.escape(key))
    match = re.search(pattern, yaml_text, re.MULTILINE)
    if not match:
        sys.exit("_yaml_value(%s, %s, %s): not found" % (file_name, section, key))
    return match.group(1).strip()


FOLDER_LOCAL = _yaml_value("application-dev.yaml", "folder", "local")

# Booklet images (cover art) are the site's own originals, addressed by bare
# filename and located by search -- the year folders don't matter.
PICTURES_BASE = os.path.join(FOLDER_LOCAL, "images", "native")
# In-page photos use the site's medium copies, <img>-medium.<ext>, which keep the PDF small.
MEDIUM_BASE = os.path.join(FOLDER_LOCAL, "images", "medium")


# ---------------------------------------------------------------------------
# Per-booklet config. These are the ONLY literal cover strings in the file, and
# the only things that differ between booklets: where to read pages and what the
# cover says. How a list renders (checkbox / number / bullet) is decided by the
# page markup, not here -- the renderer doesn't know which booklet it's in.
# ---------------------------------------------------------------------------
BOOKLETS = {
    "checklist": {
        "page_list": _p("data", "shared", "browser", "van-checklist.json"),
        "data_dir": _p("data", "hardware", "checklist"),
        "cover_title": "Checklists",
        "cover_subtitle": "Quick checklists, refer to howto for more details",
        "cover_image": "IMG_2773_crop.jpg",
        "default_output": os.path.join(FOLDER_LOCAL, "document", "checklist.pdf"),
    },
    "howto": {
        "page_list": _p("data", "shared", "browser", "van-howto.json"),
        "data_dir": _p("data", "hardware", "howto"),
        "cover_title": "How To",
        "cover_subtitle": "Step-by-step instructions",
        "cover_image": "IMG_2773_crop.jpg",
        "default_output": os.path.join(FOLDER_LOCAL, "document", "howto.pdf"),
    },
}

# The profile-free site name: the booklet is published, so never the local profile's.
COVER_KICKER = _yaml_value("application.yaml", "BaseWebProcessor", "siteName")

# ----- palette (site.css --rust, the ruled checklist's edge) -----
RUST = colors.HexColor("#b5502f")
INK = colors.HexColor("#333333")
MUTED = colors.HexColor("#555555")

# ---------------------------------------------------------------------------
# Box glyphs. The booklet is set in Helvetica, one of the base-14 fonts, whose
# WinAnsi encoding has no ballot box -- reportlab prints the notdef glyph, a
# SOLID black square, wherever a page writes one. A Unicode TTF is registered
# and applied to those characters ALONE, so everything else stays Helvetica.
# ---------------------------------------------------------------------------
BOX_FONT = "Boxes"
BOX_FONT_PATHS = (
    "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
    "/Library/Fonts/Arial Unicode.ttf",
)
BOX_CHARS = "\u2610\u2611\u2612\u25a1\u25a2\u274f\u2750\u2751\u2752"
BOX_RUN_RE = re.compile("[" + BOX_CHARS + "]+")

def _register_box_font():
    """Register the first box-capable font found. False if none is installed."""
    for path in BOX_FONT_PATHS:
        if os.path.exists(path):
            pdfmetrics.registerFont(TTFont(BOX_FONT, path))
            return True
    sys.stderr.write("no box font found, box characters will print solid: %s\n"
                     % ", ".join(BOX_FONT_PATHS))
    return False

BOX_FONT_OK = _register_box_font()


def _boxes(markup):
    """Paragraph markup with every box character switched to the box font."""
    if not BOX_FONT_OK:
        return markup
    switched = BOX_RUN_RE.sub(
        lambda m: '<font name="%s">%s</font>' % (BOX_FONT, m.group(0)), markup)
    return switched


styles = getSampleStyleSheet()

cover_title = ParagraphStyle(
    "CoverTitle", parent=styles["Title"], fontName="Helvetica-Bold",
    fontSize=24, leading=28, textColor=INK, alignment=TA_CENTER)
cover_sub = ParagraphStyle(
    "CoverSub", parent=styles["Normal"], fontSize=12, leading=16,
    textColor=MUTED, alignment=TA_CENTER)
cover_kicker = ParagraphStyle(
    "CoverKicker", parent=styles["Normal"], fontSize=10, leading=14,
    textColor=RUST, alignment=TA_CENTER, fontName="Helvetica-Bold")

page_title = ParagraphStyle(
    "PageTitle", parent=styles["Heading1"], fontName="Helvetica-Bold",
    fontSize=24, leading=28, textColor=INK, alignment=TA_CENTER, spaceAfter=4)
page_subtitle = ParagraphStyle(
    "PageSubtitle", parent=styles["Normal"], fontName="Helvetica-Bold",
    fontSize=13, leading=16, textColor=RUST, alignment=TA_CENTER, spaceAfter=8)
intro = ParagraphStyle(
    "Intro", parent=styles["Normal"], fontSize=9, leading=11.5,
    textColor=MUTED, spaceAfter=5)
group = ParagraphStyle(
    "Group", parent=styles["Heading2"], fontName="Helvetica-Bold",
    fontSize=10, leading=12, textColor=RUST, spaceBefore=5, spaceAfter=2)
item = ParagraphStyle(
    "Item", parent=styles["Normal"], fontSize=9, leading=11.5, textColor=INK)
warn = ParagraphStyle(
    "Warn", parent=styles["Normal"], fontSize=9, leading=11.5,
    textColor=RUST, spaceBefore=3, spaceAfter=3)
cell = ParagraphStyle(
    "Cell", parent=styles["Normal"], fontSize=8, leading=10, textColor=INK)

toc_title = ParagraphStyle(
    "TocTitle", parent=styles["Heading1"], fontName="Helvetica-Bold",
    fontSize=24, leading=28, textColor=INK, alignment=TA_CENTER, spaceAfter=12)
toc_entry = ParagraphStyle(
    "TocEntry", parent=styles["Normal"], fontName="Helvetica",
    fontSize=9, leading=11.5, textColor=INK)


class BookletDoc(SimpleDocTemplate):
    """SimpleDocTemplate that feeds page titles into the Table of Contents."""
    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph) and flowable.style.name == "PageTitle":
            # Sections from one page share a main title, so the ToC uses the
            # combined "title — subtitle" stashed on the paragraph.
            text = getattr(flowable, "_toc_text", None) or flowable.getPlainText()
            self.notify("TOCEntry", (0, text, self.page))


class NumberedCanvas(canvas.Canvas):
    """Stamps a 'Page n of N' footer on every page (needs the final page
    count, so it defers drawing until save())."""
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        total = len(self._saved_page_states)
        for n, state in enumerate(self._saved_page_states, start=1):
            self.__dict__.update(state)
            self._draw_footer(n, total)
            super().showPage()
        super().save()

    def _draw_footer(self, n, total):
        self.setFont("Helvetica", 9)
        self.setFillColor(MUTED)
        self.drawCentredString(HALF_LETTER[0] / 2, 0.3 * inch,
                               "Page %d of %d" % (n, total))


class CheckBox(Flowable):
    """A small empty square, baseline-aligned with the first text line. Given a
    number, the "n." follows the box, as og-numcheck draws it on the web."""
    NUMBER_GAP = 4

    def __init__(self, size=8, number=None):
        super().__init__()
        self.size = size
        self.number = number
        self.width = size
        self.height = size

    def draw(self):
        c = self.canv
        c.setStrokeColor(RUST)
        c.setLineWidth(0.9)
        c.rect(0, 0, self.size, self.size, stroke=1, fill=0)
        if self.number is not None:
            c.setFillColor(INK)
            c.setFont(item.fontName, item.fontSize)
            c.drawString(self.size + self.NUMBER_GAP, 0.5, "%d." % self.number)


def _marker(checklist, numbered, ordered, n):
    """The left-column marker for a list item: a checkbox (og-checklist), a
    checkbox and "n." (og-numcheck), a "n." number (ordered list), or a bullet
    (unordered list)."""
    if numbered:
        return CheckBox(number=n)
    if checklist:
        return CheckBox()
    if ordered:
        return Paragraph("%d." % n, item)
    return Paragraph("•", item)


def render_list(list_el):
    """Render an <ol>/<ul> as a Table of [marker, content] rows. The marker
    follows the markup, exactly like the web: an og-checklist list gets
    checkboxes, an og-numcheck list checkboxes and numbers, otherwise <ol> is
    numbered and <ul> is bulleted. Nested lists recurse, each deciding by its
    own class."""
    ordered = list_el.name == "ol"
    class_list = list_el.get("class") or []
    numbered = "og-numcheck" in class_list
    checklist = numbered or "og-checklist" in class_list
    rows = []
    n = 0
    for li in list_el.find_all("li", recursive=False):
        n += 1
        # Pull nested lists out first so the item's own text reads cleanly,
        # then render them indented beneath it.
        nested = [sub.extract() for sub in li.find_all(["ol", "ul"], recursive=False)]
        content = []
        text = _text(li)
        if text:
            content.append(Paragraph(text, item))
        for sub in nested:
            content.append(render_list(sub))
        rows.append([_marker(checklist, numbered, ordered, n), content or [Paragraph("", item)]])

    marker_top_pad = 2.6 if checklist else 1.7  # nudge box onto line
    marker_width = 0.46 * inch if numbered else 0.26 * inch
    t = Table(rows, colWidths=[marker_width, None])
    t.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("TOPPADDING", (0, 0), (-1, -1), 1.7),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 1.7),
        ("LEFTPADDING", (0, 0), (0, -1), 0),
        ("TOPPADDING", (0, 0), (0, -1), marker_top_pad),
    ]))
    return t


def render_table(table_el):
    """Render an HTML <table> as a gridded reportlab Table."""
    data = []
    for tr in table_el.find_all("tr"):
        cells = tr.find_all(["th", "td"])
        if cells:
            data.append([Paragraph(_text(c), cell) for c in cells])
    if not data:
        return Spacer(0, 0)
    t = Table(data)
    t.setStyle(TableStyle([
        ("GRID", (0, 0), (-1, -1), 0.4, MUTED),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("TOPPADDING", (0, 0), (-1, -1), 2),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2),
        ("LEFTPADDING", (0, 0), (-1, -1), 3),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
    ]))
    return t


# ---------------------------------------------------------------------------
# Parsing: pull the howto section out of a page.
# ---------------------------------------------------------------------------

# Two tags carry meaning but no text, so collapsing a node to its text would
# lose them. Each is swapped for a private-use marker BEFORE the text is taken,
# and the marker becomes Paragraph markup AFTER escaping -- escaping the real
# markup would print it literally.
BREAK_MARK = "\ue000"
BLANK_MARK_OPEN, BLANK_MARK_CLOSE = "\ue001", "\ue002"
BLANK_DEFAULT_CHARS = 6


def _blank(chars):
    """A write-on blank: two underscores per character of the field's size."""
    rule = "_" * (2 * max(1, chars))
    return rule


def _markable(node):
    """A copy of the node with <br> and <input> swapped for their markers."""
    copied = copy.copy(node)
    for br in copied.find_all("br"):
        br.replace_with(BREAK_MARK)
    for field in copied.find_all("input"):
        size = field.get("size") or field.get("width") or BLANK_DEFAULT_CHARS
        try:
            chars = int(str(size).strip())
        except ValueError:
            chars = BLANK_DEFAULT_CHARS
        field.replace_with(BLANK_MARK_OPEN + str(chars) + BLANK_MARK_CLOSE)
    return copied


def _text(node):
    """Collapsed, XML-escaped text of a node (safe for reportlab Paragraph)."""
    marked = _markable(node)
    collapsed = re.sub(r"\s+", " ", marked.get_text(" ", strip=True))
    # get_text puts a space between every piece, so a comma after a replaced
    # photo-ref would read "van , the".
    collapsed = re.sub(r" ([,.:;!?)])", r"\1", collapsed)
    escaped = _boxes(xml_escape(collapsed))
    escaped = re.sub(r"\s*" + BREAK_MARK + r"\s*", "<br/>", escaped)
    escaped = re.sub(BLANK_MARK_OPEN + r"(\d+)" + BLANK_MARK_CLOSE,
                     lambda m: _blank(int(m.group(1))), escaped)
    return escaped


def _load_data(html_path, data_dir):
    """The page's sibling JSON (<data_dir>/<slug>/<slug>.json), or {}."""
    slug = os.path.splitext(os.path.basename(html_path))[0]
    json_path = os.path.join(data_dir, slug, slug + ".json")
    try:
        with open(json_path, encoding="utf-8") as f:
            return json.load(f)
    except (OSError, ValueError):
        return {}


def _photoref_label(photo_id, galleries):
    """The caption text for a photo-ref id ("<gallery>/<itemId>")."""
    if not photo_id or "/" not in photo_id:
        return None
    gkey, iid = photo_id.split("/", 1)
    photo = (galleries.get(gkey) or {}).get(iid) or {}
    return photo.get("label")


# A fragment call as the template writes it: ~{fragments/block/<file> :: <name>('<argument>')}.
# Thymeleaf escapes a quote inside the literal as \'.
FRAGMENT_RE = re.compile(r"fragments/block/([\w-]+)\s*::\s*\w+\(\s*'((?:[^'\\]|\\.)*)'")


def _photo_img(photo_id, galleries):
    """The image name for a gallery-photo id ("<gallery>/<itemId>"), or None."""
    if not photo_id or "/" not in photo_id:
        return None
    gkey, iid = photo_id.split("/", 1)
    photo = (galleries.get(gkey) or {}).get(iid) or {}
    return photo.get("img")


# The width the page gives a photo's box, as the percentage in its style attribute.
STYLE_WIDTH_RE = re.compile(r"(?:^|;)\s*width\s*:\s*(\d+(?:\.\d+)?)%")
# The side a photo's box floats to, from its style attribute.
STYLE_FLOAT_RE = re.compile(r"(?:^|;)\s*float\s*:\s*(left|right)")


def _clean_inline(section, soup, galleries):
    """In place: resolve the Thymeleaf fragment calls a section can carry. A
    photo-ref becomes its caption text inline, a warning becomes the warning
    block render_element draws, a gallery-photo becomes the photo block it
    draws at the width its box's style gives, and every other fragment is
    dropped -- the rest is page chrome or JSON the booklet doesn't carry."""
    for el in section.find_all(lambda tag: tag.has_attr("th:replace") or tag.has_attr("th:insert")):
        call = el.get("th:replace") or el.get("th:insert")
        match = FRAGMENT_RE.search(call)
        fragment = match.group(1) if match else None
        argument = match.group(2).replace("\\'", "'") if match else ""
        if fragment == "photo-ref":
            el.replace_with(_photoref_label(argument, galleries) or argument)
        elif fragment == "warning":
            warning = soup.new_tag("div", attrs={"data-block-type": "warning", "data-text": argument})
            el.replace_with(warning)
        elif fragment == "gallery-photo":
            style = el.get("style") or ""
            width_match = STYLE_WIDTH_RE.search(style)
            width_percent = width_match.group(1) if width_match else "100"
            photo = soup.new_tag("div", attrs={"data-block-type": "photo",
                                               "data-img": _photo_img(argument, galleries) or argument,
                                               "data-width": width_percent})
            float_match = STYLE_FLOAT_RE.search(style)
            if float_match:
                photo["data-float"] = float_match.group(1)
            el.replace_with(photo)
        else:
            el.decompose()


def get_photo_image(img, width_percent):
    """A photo at <width_percent> of the text width from the medium copy of
    <img>; a 'Missing picture' line when there is none. A tall photo is scaled
    down to PHOTO_MAX_HEIGHT."""
    path = _find_image_stem(img + "-medium", MEDIUM_BASE)
    if not path:
        return Paragraph("Missing picture: " + xml_escape(img), warn)
    image_width, image_height = ImageReader(path).getSize()
    width = TEXT_WIDTH * width_percent / 100.0
    height = width * image_height / float(image_width)
    if height > PHOTO_MAX_HEIGHT:
        width = width * PHOTO_MAX_HEIGHT / height
        height = PHOTO_MAX_HEIGHT
    return Image(path, width=width, height=height)


def render_photo(img, width_percent):
    """A photo, left-aligned, on a line of its own."""
    photo = get_photo_image(img, width_percent)
    photo.hAlign = "LEFT"
    return [photo, Spacer(0, 6)]


def render_photo_row(photo_list):
    """Floated photo blocks on one line: the left-floated ones from the left
    edge, the right-floated ones against the right edge, each in a column its
    width, the gap between them an empty column."""
    left_list = [el for el in photo_list if el.get("data-float") == "left"]
    right_list = [el for el in photo_list if el.get("data-float") == "right"]
    cell_list = []
    width_list = []
    for el in left_list + [None] + right_list:
        if el is None:
            cell_list.append("")
            width_list.append(0)
            continue
        width_percent = float(el.get("data-width"))
        cell_list.append(get_photo_image(el.get("data-img"), width_percent))
        width_list.append(TEXT_WIDTH * width_percent / 100.0)
    gap_index = len(left_list)
    width_list[gap_index] = max(0, TEXT_WIDTH - sum(width_list))
    row = Table([cell_list], colWidths=width_list)
    row.hAlign = "LEFT"
    row.setStyle(TableStyle([
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("ALIGN", (gap_index + 1, 0), (-1, -1), "RIGHT"),
        ("LEFTPADDING", (0, 0), (-1, -1), 0),
        ("RIGHTPADDING", (0, 0), (-1, -1), 0),
        ("TOPPADDING", (0, 0), (-1, -1), 0),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 0),
    ]))
    return [row, Spacer(0, 6)]


def _is_floated_photo(el):
    return el.name == "div" and el.get("data-block-type") == "photo" and el.has_attr("data-float")


def render_elements(element_list):
    """Render sibling elements in order. A run of adjacent floated photos
    becomes one row; any other element, a clear div included, ends the run."""
    out = []
    photo_list = []
    for el in element_list:
        if _is_floated_photo(el):
            photo_list.append(el)
            continue
        if photo_list:
            out.extend(render_photo_row(photo_list))
            photo_list = []
        out.extend(render_element(el))
    if photo_list:
        out.extend(render_photo_row(photo_list))
    return out


def render_element(el):
    """Render one block-level element from inside the section into a list of
    flowables, recursing into <details> (expanded) and nested content."""
    name = el.name
    if name in ("h2", "h3", "h4", "h5"):
        return [Paragraph(_text(el), group)]
    if name == "p":
        return [Paragraph(_text(el), intro)]
    if name in ("ol", "ul"):
        return [render_list(el)]
    if name == "table":
        return [render_table(el)]
    if name == "details":
        out = []
        summary = el.find("summary")
        if summary:
            out.append(Paragraph(_text(summary), group))
        child_list = [child for child in el.find_all(True, recursive=False) if child.name != "summary"]
        out.extend(render_elements(child_list))
        return out
    if name == "div" and el.get("data-block-type") == "warning":
        txt = el.get("data-text", "")
        return [Paragraph("⚠ " + _boxes(xml_escape(txt)), warn)] if txt else []
    if name == "div" and el.get("data-block-type") == "photo":
        return render_photo(el.get("data-img"), float(el.get("data-width")))
    if name == "div" and el.find(True, recursive=False):
        # A grouping div: its children are the content.
        return render_elements(el.find_all(True, recursive=False))
    # Anything else: render whatever text it carries, else nothing.
    txt = _text(el)
    return [Paragraph(txt, intro)] if txt else []


def build_page(path, data_dir):
    """Return a list of (title, subtitle, [flowables]) — one booklet page per
    <section data-howto-section="howto"> on the page. The title is the page's
    `name` (JSON), shared by every section; the subtitle is that section's
    own heading (via aria-labelledby). A page with no section still yields one
    title-only entry (producing the block is the author's job)."""
    data = _load_data(path, data_dir)
    title = xml_escape(data.get("name") or os.path.splitext(os.path.basename(path))[0])
    galleries = data.get("photoGalleries", {})

    with open(path, encoding="utf-8") as f:
        soup = BeautifulSoup(f.read(), "html.parser")
    sections = soup.find_all("section", attrs={"data-howto-section": "howto"})

    if not sections:
        return [(title, "", [])]

    entries = []
    for section in sections:
        _clean_inline(section, soup, galleries)
        heading_id = section.get("aria-labelledby")
        heading_el = section.find(id=heading_id) if heading_id else None
        subtitle = _text(heading_el) if heading_el else ""

        # The section's own heading becomes the subtitle, not body content.
        child_list = [child for child in section.find_all(True, recursive=False)
                      if not (heading_id and child.get("id") == heading_id)]
        flowables = render_elements(child_list)
        entries.append((title, subtitle, flowables))

    return entries


def _find_image(name):
    """First file named <name> (case-insensitive) anywhere under PICTURES_BASE,
    or None. The folder layout is the author's business; we just locate it."""
    if not name:
        return None
    low = name.lower()
    for root, _dirs, files in os.walk(PICTURES_BASE):
        for f in files:
            if f.lower() == low:
                return os.path.join(root, f)
    return None


def _find_image_stem(stem, base):
    """First file whose name without extension is <stem> (case-insensitive)
    anywhere under <base>, or None."""
    low = stem.lower()
    for root, _dirs, files in os.walk(base):
        for f in files:
            if os.path.splitext(f)[0].lower() == low:
                return os.path.join(root, f)
    return None


def _cover_image(name, width):
    """(path, height) for the cover image scaled to <width>. If the file isn't
    found, (None, <placeholder height>)."""
    path = _find_image(name)
    if path:
        iw, ih = ImageReader(path).getSize()
        return path, width * ih / float(iw)
    return None, 2.5 * inch


def _draw_cover_image(c, path, img_h, y, width):
    """Draw the cover image (or a 'Missing picture' placeholder) <width> wide,
    with its bottom at y, horizontally centered."""
    x = (HALF_LETTER[0] - width) / 2.0
    if path:
        c.drawImage(path, x, y, width=width, height=img_h,
                    preserveAspectRatio=True, mask="auto")
    else:
        c.setStrokeColor(MUTED)
        c.setLineWidth(0.8)
        c.rect(x, y, width, img_h, stroke=1, fill=0)
        c.setFillColor(MUTED)
        c.setFont("Helvetica-Oblique", 11)
        c.drawCentredString(HALF_LETTER[0] / 2.0, y + img_h / 2.0 - 4, "Missing picture")


def make_cover_drawer(cfg):
    """Return an onFirstPage callback that paints the cover: image centered on
    the page, and each title centered in its own white band (main above the
    image, subtitle below)."""
    def draw_cover(c, doc):
        w, h = HALF_LETTER
        avail = w - doc.leftMargin - doc.rightMargin

        kicker = Paragraph(COVER_KICKER, cover_kicker)
        title = Paragraph(cfg["cover_title"], cover_title)
        subtitle = Paragraph(cfg["cover_subtitle"], cover_sub)

        _, title_h = title.wrap(avail, h)
        _, kicker_h = kicker.wrap(avail, h)
        _, sub_h = subtitle.wrap(avail, h)

        # Image: 4" wide, vertically centered on the page.
        width = 4 * inch
        path, img_h = _cover_image(cfg.get("cover_image"), width)
        img_bottom = h / 2.0 - img_h / 2.0
        img_top = h / 2.0 + img_h / 2.0
        _draw_cover_image(c, path, img_h, img_bottom, width)

        # Main block (kicker above title) centered in the band above the image.
        gap = 8
        block_h = title_h + gap + kicker_h
        block_bottom = (img_top + (h - doc.topMargin)) / 2.0 - block_h / 2.0
        title.drawOn(c, doc.leftMargin, block_bottom)
        kicker.drawOn(c, doc.leftMargin, block_bottom + title_h + gap)

        # Subtitle centered in the band below the image.
        subtitle.drawOn(c, doc.leftMargin,
                        (doc.bottomMargin + img_bottom) / 2.0 - sub_h / 2.0)

    return draw_cover


def instruction_page(story, title, subtitle, flowables):
    tp = Paragraph(title, page_title)
    tp._toc_text = title + (" — " + subtitle if subtitle else "")
    story.append(tp)
    if subtitle:
        story.append(Paragraph(subtitle, page_subtitle))
    story.extend(flowables)


def build(cfg, path):
    # The browser dataset decides which pages go in and in what order. A row with no
    # file pointer is an external link, not a page.
    with open(cfg["page_list"], encoding="utf-8") as f:
        row_list = json.load(f)
    pages = []
    for row in row_list:
        page_file = row.get("file")
        if not page_file:
            continue
        template_path = _p("templates", page_file.lstrip("/") + ".html")
        if not os.path.isfile(template_path):
            sys.exit("No template for %s in %s" % (page_file, cfg["page_list"]))
        pages.extend(build_page(template_path, cfg["data_dir"]))
    if not pages:
        sys.exit("No pages found in " + cfg["page_list"])

    doc = BookletDoc(
        path, pagesize=HALF_LETTER,
        leftMargin=PAGE_MARGIN, rightMargin=PAGE_MARGIN,
        topMargin=PAGE_MARGIN, bottomMargin=PAGE_MARGIN,
        title=cfg["cover_title"], author=COVER_KICKER)

    story = []

    # Page 1 is the cover, painted by the cover drawer; leave its frame empty.
    story.append(PageBreak())

    # Table of contents
    story.append(Paragraph("Table of Contents", toc_title))
    toc = TableOfContents()
    toc.levelStyles = [toc_entry]
    story.append(toc)

    # One booklet page per section (alpha by title, doc order within a title)
    for title, subtitle, flowables in pages:
        story.append(PageBreak())
        instruction_page(story, title, subtitle, flowables)

    # multiBuild: two passes so the ToC page numbers resolve; NumberedCanvas
    # stamps the "Page n of N" footers on the final pass.
    doc.multiBuild(story, onFirstPage=make_cover_drawer(cfg),
                   canvasmaker=NumberedCanvas)
    print("wrote", path, "(%d pages)" % len(pages))


if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in BOOKLETS:
        sys.exit("usage: build_booklet_pdf.py <%s> [output.pdf]"
                 % "|".join(BOOKLETS))
    cfg = BOOKLETS[sys.argv[1]]
    out = sys.argv[2] if len(sys.argv) > 2 else cfg["default_output"]
    build(cfg, out)
