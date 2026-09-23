/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The browser page: one page, two independent axes — display (table/grid/map) x data —
 * driven entirely by the URL query string.
 *
 *   ?dataset=<id>   one of the ids in datasets.json
 *   ?view=<mode>    table | grid | map
 *   ?keywords= ?types= ?badges= ?access= ?search= ?booklet=
 *
 * THE URL IS THE WHOLE STATE. Every control commits by navigating to a new URL (see
 * navigate()), so the page renders from scratch and Back/Forward work with no machinery.
 *
 * Rendering is FOUR PHASES, in order, read top to bottom in start():
 *   1. PARAMS   — the raw query string becomes what the page acts on
 *   2. LOAD     — datasets.json, then the selected dataset's rows. Sequential because the
 *                 first names the second, and if either fails there is nothing useful to
 *                 do, so there is ONE error path.
 *   3. PROCESS  — all of it, up front: filter the rows, count the vocabularies.
 *   4. DISPLAY  — displayOptionsRow() and displayDataset(), two views on "all that is known".
 *
 * Phases 1-3 build ONE property bag, `known`, a local of start() passed to everything:
 *   phase 1 adds  params
 *   phase 2 adds  datasetList, dataset, rawRows
 *   phase 3 adds  filteredRows, keywords, counts
 *
 * `known` holds KNOWLEDGE, never DOM. The block element is a plain local of start(), which
 * is the only place the page gets touched.
 *
 * Phases 1-3 WRITE the bag; phase 4 only READS it — and the display functions are pure
 * `known -> DOM node`, so they cannot affect each other or the page at all. That
 * independence is the point: hand them the element instead and the options row has to run
 * before the dataset or it lands underneath it, which is the same hidden ordering the
 * phases exist to remove. Every display function takes the same argument and opens by
 * naming what it needs out of it; that is what documents its dependencies.
 *
 * Two switches, deliberately NOT the same vocabulary: the filter switch runs over the URL
 * parameters, the control map is keyed by the dataset's `options` tokens. One option can
 * emit several parameters, or none. A token with no builder, or a parameter with no filter,
 * is a legitimate state (staged rollout, testing, retired-but-kept), never an error.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.OG = window.OG || {};

	// The definition list is a static file; the rows come from the server, which maps the id
	// to its file and hydrates it. The browser never reads a dataset's `file`.
	var DATASETS_URL = "/shared/browser/datasets.json";
	var DATA_URL = "/shared/browser/data/";

	// A booklet is public content the visitor downloads, so it sits beside datasets.json.
	var BOOKLET_URL = "/document/";

	var VALID_VIEWS = ["table", "grid", "map"];

	// Defaults for a bare URL (no params). A present-but-invalid value is NOT defaulted over
	// — it falls through to the graceful "unknown ..." handling.
	var DEFAULT_DATASET = "destination";
	var DEFAULT_VIEW = "grid";

	// Where the map view opens: northern Vancouver Island, wide enough to hold the whole
	// catalog area. FIXED — the map does not reframe itself around whatever is filtered in.
	var MAP_CENTER = { lat: 50.159395, lng: -126.431037 };
	var MAP_ZOOM = 8;
	var MAP_HEIGHT = "600px";

	// The image endpoint. A card's featured image is a bare filename in the data.
	var IMAGE_URL = "/image/";
	var PLACEHOLDER_IMAGE = "under-construction.png";

	// Severity ramp for the access threshold, easiest -> hardest. The access <select> is
	// built from the same array, so the control and the filter cannot drift apart. Read
	// through a function, not a top-level constant, because the constants file may execute
	// after this one.
	function roadOrder() {
		var order = Object.keys(window.OG.ROAD_COLORS);
		return order;
	}

	// ---- fetching -------------------------------------------------------------

	// One fetch path for both files. The URL is in every failure message because "HTTP 404"
	// on its own never says WHICH file was missing — and the two fail for very different
	// reasons (a bad dataset id vs. a missing data file).
	function fetchJson(url) {
		return fetch(url)
			.catch(function () {
				throw new Error("could not reach " + url);
			})
			.then(function (res) {
				if (!res.ok) {
					throw new Error("failed to load " + url + " (HTTP " + res.status + ")");
				}
				return res.json().catch(function () {
					throw new Error("failed to parse " + url + " (not valid JSON)");
				});
			});
	}

	// The datasets.json entry for the requested id, or a throw. Throwing keeps the caller to
	// one statement — the whole not-found case lives here instead of a branch interrupting
	// the flow of the phases, and the single catch is already the place that reports.
	function pickDataset(known) {
		// What I need from what is known
		var datasetList = known.datasetList;
		var id = known.params.get("dataset");

		var entry = datasetList.filter(function (d) { return d.id === id; })[0];
		if (entry) { return entry; }
		throw new Error("Unknown dataset " + id);
	}

	// The ONE place the raw query string becomes what the page acts on. Today it only fills
	// in the defaults; it is also where any parameter validation would go, so nothing
	// downstream has to defend itself.
	//
	// Everything reads the result — filters, controls, and navigate(). One side effect,
	// accepted deliberately: because navigate() patches this object, a default it filled in
	// lands in the next URL, so a shared link says exactly what it will render.
	function processParams(search) {
		var p = new URLSearchParams(search);
		if (!p.get("dataset")) { p.set("dataset", DEFAULT_DATASET); }
		if (!p.get("view")) { p.set("view", DEFAULT_VIEW); }
		return p;
	}

	// ---- shared bits: escaping, tags, the road badge, cards -------------------

	function escapeHtml(str) {
		if (!str) { return ""; }
		var text = String(str)
			.replace(/&/g, "&amp;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&#039;");
		return text;
	}

	// Where a row's own page lives. A row with no `file` has no page — no card, no link on
	// the pin. One helper, so a card, a pin and the table's View link can never disagree.
	function pageHref(row) {
		var file = row.file;
		if (!file) { return null; }
		return file;
	}

	function imageUrl(name) {
		var url = IMAGE_URL + encodeURIComponent(name || PLACEHOLDER_IMAGE) + "?size=small";
		return url;
	}

	/*
	 * The single render-time road-badge derivation, from access.legs. One copy, so the badge
	 * word is computed one way everywhere:
	 *   legList absent     -> null          (not filled in yet: no badge)
	 *   legList []         -> "pavement"    (deliberate: paved all the way)
	 *   any non-drive leg  -> "back_country"
	 *   otherwise          -> the hardest drive leg
	 * An unknown leg type, or an unpaved leg with no km (unpaved asserts a MEASURED tail, so
	 * it claims nothing without one), is a data error: log it and skip the leg.
	 */
	function deriveRoadBadge(access) {
		var legs = access && access.legList;
		if (!Array.isArray(legs)) { return null; }
		if (legs.length === 0) { return "pavement"; }

		var driveList = window.OG.ROAD_RANK;
		var nonDriveList = window.OG.NON_DRIVE_LEG_TYPES;
		var worst = -1;
		for (var i = 0; i < legs.length; i++) {
			var leg = legs[i];
			var type = leg && leg.type;
			var driveRank = driveList.indexOf(type);
			var isNonDrive = nonDriveList.indexOf(type) !== -1;
			if (driveRank === -1 && !isNonDrive) {
				console.error("[browser] deriveRoadBadge: unknown leg type — leg ignored. type='" +
					type + "' leg='" + JSON.stringify(leg) + "'");
				continue;
			}
			if (type === "unpaved" && !(typeof leg.km === "number" && leg.km > 0)) {
				console.error("[browser] deriveRoadBadge: unpaved leg has no km — leg ignored. leg='" +
					JSON.stringify(leg) + "'");
				continue;
			}
			if (isNonDrive) { return "back_country"; }
			if (driveRank > worst) { worst = driveRank; }
		}
		var badge = worst < 0 ? null : driveList[worst];
		return badge;
	}

	// The badge pills in the card image's top-right corner. An empty list renders nothing.
	// The pill carries its word and no colour — OG.paintTags fills that in once the card is
	// in the document, so a card pill and a page pill come from the one palette.
	function renderTags(tagList) {
		if (!tagList || !tagList.length) { return ""; }
		var sorted = tagList.slice().sort();
		var html = '<div class="og-tag-stack">';
		for (var i = 0; i < sorted.length; i++) {
			html += '<span class="og-tag" data-tag="' + escapeHtml(sorted[i]) + '">' +
				escapeHtml(sorted[i]) + "</span>";
		}
		html += "</div>";
		return html;
	}

	// The road badge, lower-left corner. A falsy road (legs not filled in yet) renders
	// nothing, exactly like an empty tag list. The vocabulary is strict, and OG.paintTags is
	// where that is enforced: it drops a badge whose word the palette does not know.
	//
	// The underscore in a value is an id convention, not something to show a reader, so it
	// becomes a space on the way to the badge. data-road keeps the id.
	function renderRoad(road) {
		if (!road) { return ""; }
		var html = '<span class="og-road" data-road="' + escapeHtml(road) + '">' +
			escapeHtml(road.replace(/_/g, " ")) + "</span>";
		return html;
	}

	// One card. Only rows with a page get here — the grid filter is what guarantees it, so
	// the href is never empty.
	function renderCard(row) {
		var title = escapeHtml(row.name || "");
		var image = escapeHtml(imageUrl(row.featuredImage));
		var teaser = escapeHtml(row.excerpt || "");
		var href = escapeHtml(pageHref(row));

		var html =
			'<a class="og-gallery-card" href="' + href + '">' +
			'<div class="og-gallery-card-img-wrap">' +
			'<img class="og-gallery-card-img" src="' + image + '" alt="' + title + '" loading="lazy">' +
			renderTags((row.tags || {}).badgeList) +
			renderRoad(deriveRoadBadge(row.access)) +
			"</div>" +
			'<h3 class="og-gallery-card-title">' + title + "</h3>" +
			'<p class="og-gallery-card-teaser">' + teaser + "</p>" +
			"</a>";
		return html;
	}

	// ---- table columns --------------------------------------------------------

	// `align` is optional and means text-align on the whole column, heading included;
	// buildCell applies it. Left is the default and is not written out.
	var COLUMNS = [
		{ label: "Name", field: "name", width: 170 },
		{ label: "On lost", field: "on_lost", width: 60, align: "center" },
		{ label: "Location", field: "location", width: 155 },
		{ label: "Distance", field: "haversine", width: 85, align: "center" },
		{ label: "Access", field: "roadBadge", width: 75, align: "center" },
		{ label: "Sites", field: "siteCount", width: 55, align: "center" },
		{ label: "Maps", field: "maps", width: 100, align: "center" },
		{ label: "Amenities", field: "amenities", width: 175 },
		{ label: "Reservation", field: "reservation" }
	];

	// ---- table cell helpers ---------------------------------------------------

	// Every link a row carries lives in one flat referenceList, and `type` says what each one
	// IS (OG.LINK_TYPES). The label is display text and nothing else — selecting by type is
	// why the column helpers are one-liners.
	function linksOfType(place, type) {
		var referenceList = (place.campgroundData || {}).referenceList || [];
		var linkList = referenceList.filter(function (l) { return l && l.type === type; });
		return linkList;
	}

	function homepageUrl(place) {
		var homepageList = linksOfType(place, "homepage");
		var url = homepageList.length ? homepageList[0].url : null;
		return url;
	}

	function campbellRiverKm(place) {
		var access = place.access || {};
		var townMap = access.haversineMap || {};
		var km = townMap["Campbell River"];
		return km === undefined ? null : km;
	}

	// Missing values render as an empty cell; arrays join with ", ".
	function cellText(value) {
		if (value === null || value === undefined || value === "") { return ""; }
		if (Array.isArray(value)) { return value.join(", "); }
		return String(value);
	}

	// The one place a cell's presentation is decided, for both <th> and <td>. A column's
	// `align` lands here rather than in the colgroup because a <col> honours only width,
	// border, background and visibility — text-align set there does nothing.
	function buildCell(tag, col) {
		var cell = document.createElement(tag);
		if (col && col.align) { cell.style.textAlign = col.align; }
		return cell;
	}

	// One anchor, whatever the url is. OG.linkOpenTag owns the whole target/rel decision —
	// the same one fragments/block/link.html makes on a server-rendered page — and hands back
	// the opening tag; the text and the title are this file's business.
	function linkTo(url, text, tip) {
		var holder = document.createElement("div");
		holder.innerHTML = window.OG.linkOpenTag(url) + "</a>";
		var a = holder.firstChild;
		a.textContent = text;
		if (tip) { a.title = tip; }
		return a;
	}

	// Renders a list of { label, url } into a cell, one per line, linked when it has a url.
	// `fallback` is the display text for a url with no label.
	function fillLinkList(cell, entryList, tip, fallback) {
		(entryList || []).forEach(function (entry, i) {
			var text = cellText(entry.label) || fallback || "";
			if (i > 0) { cell.appendChild(document.createElement("br")); }
			if (entry.url && text) {
				cell.appendChild(linkTo(entry.url, text, tip));
			} else {
				cell.appendChild(document.createTextNode(text));
			}
		});
	}

	// Fills one column of one place.
	function fillDataCell(cell, place, field) {
		if (field === "name") {
			var home = homepageUrl(place);
			if (home) {
				cell.appendChild(linkTo(home, cellText(place.name), "Open the destination's web site"));
			} else {
				cell.textContent = cellText(place.name);
			}
		} else if (field === "on_lost") {
			var href = pageHref(place);
			if (href) {
				cell.appendChild(linkTo(href, "View", "Open the page on this web site"));
			}
		} else if (field === "location") {
			var loc = place.location || {};
			var label = cellText(loc.label);
			if (loc.lat != null && loc.lng != null && label) {
				cell.appendChild(linkTo(
					"https://www.google.com/maps/search/?api=1&query=" + loc.lat + "," + loc.lng,
					label,
					"Open in Google Maps"
				));
			} else {
				cell.textContent = label;
			}
		} else if (field === "haversine") {
			var km = campbellRiverKm(place);
			if (km !== null && km !== undefined) { cell.textContent = km + " km"; }
		} else if (field === "roadBadge") {
			var badge = deriveRoadBadge(place.access);
			if (badge) { cell.textContent = badge.replace(/_/g, " "); }
		} else if (field === "siteCount") {
			var siteCount = (place.campgroundData || {}).siteCount;
			if (siteCount !== null && siteCount !== undefined) { cell.textContent = String(siteCount); }
		} else if (field === "maps") {
			// Every map, one per line — the label ("Campground", "Park", "Trail") is what
			// tells them apart.
			fillLinkList(cell, linksOfType(place, "map"), "Open the map", "Map");
		} else if (field === "amenities") {
			cell.textContent = cellText((place.campgroundData || {}).amenityList);
		} else if (field === "reservation") {
			fillLinkList(cell, linksOfType(place, "reservation"), "Open the reservation web site", null);
		} else {
			cell.textContent = cellText(place[field]);
		}
	}

	function buildColGroup() {
		var colgroup = document.createElement("colgroup");
		COLUMNS.forEach(function (col) {
			var colEl = document.createElement("col");
			if (col.width) { colEl.style.width = col.width + "px"; }
			colgroup.appendChild(colEl);
		});
		return colgroup;
	}

	function appendNoteMarker(cell, number) {
		var sup = document.createElement("sup");
		sup.textContent = String(number);
		cell.appendChild(sup);
	}

	function buildFootnoteList(textList) {
		var ol = document.createElement("ol");
		ol.className = "og-lb-footnotes";
		textList.forEach(function (text) {
			var li = document.createElement("li");
			li.textContent = text;
			ol.appendChild(li);
		});
		return ol;
	}

	// ---- the filters ----------------------------------------------------------
	//
	// One function per query parameter, all the same shape:
	//     filterX(value, longList) -> shortList
	// Chaining them through filterRows() is what produces AND ACROSS controls; OR WITHIN a
	// control is internal to each filter. Adding a filter later is one function plus one
	// line in the switch.

	// A comma-separated parameter value -> array, empties dropped.
	function splitList(value) {
		var valueList = (value || "").split(",").filter(Boolean);
		return valueList;
	}

	// Only the GRID narrows, to rows that have a page — a card IS a link, so a card without
	// a page is a card that goes nowhere. The table and the map both show everything,
	// including the catalog rows with no page of their own; on the map those get a pin with
	// a hover title and no link. It lives here rather than inside renderGrid so the count
	// cannot disagree with what you see.
	function filterView(value, longList) {
		var shortList = longList;
		if (value === "grid") {
			shortList = shortList.filter(function (row) { return !!pageHref(row); });
		}
		return shortList;
	}

	// Tag membership, OR within the field: keep a row carrying at least one of the wanted
	// values. A row with none of that tag FAILS — badges and keywords are authored claims,
	// and absence is not a match.
	function filterByWord(field, wantedList, longList) {
		if (!wantedList.length) { return longList; }
		var shortList = longList.filter(function (row) {
			var have = (row.tags || {})[field] || [];
			for (var i = 0; i < wantedList.length; i++) {
				if (have.indexOf(wantedList[i]) !== -1) { return true; }
			}
			return false;
		});
		return shortList;
	}

	function filterKeywords(value, longList) {
		var shortList = filterByWord("keywordList", splitList(value), longList);
		return shortList;
	}

	function filterBadges(value, longList) {
		var shortList = filterByWord("badgeList", splitList(value), longList);
		return shortList;
	}

	// What KIND of place it is. Membership like badges, and absence FAILS for the same
	// reason — which is the point of the facet: an untyped row (a city park, say) answers no
	// type filter at all, so "park" returns provincial parks rather than everything with the
	// word in its name.
	function filterTypes(value, longList) {
		var shortList = filterByWord("typeList", splitList(value), longList);
		return shortList;
	}

	// Ordinal threshold, not membership: you pick the worst road you will accept, and a row
	// is kept when its derived badge is no worse. UNKNOWN PASSES — only rows with authored
	// legs derive a badge at all, and an unmeasured road is not evidence of a bad one. An
	// unknown threshold value filters nothing.
	function filterAccess(value, longList) {
		var order = roadOrder();
		var limit = order.indexOf(value);
		if (limit === -1) { return longList; }
		var shortList = longList.filter(function (row) {
			var rank = order.indexOf(deriveRoadBadge(row.access));
			return rank === -1 || rank <= limit;
		});
		return shortList;
	}

	// Free text over the WHOLE row, serialized. Deliberately broad: it sees every field
	// without a list to maintain, at the cost of also seeing keys and URLs (so "map" matches
	// every row carrying a map link).
	function filterSearch(value, longList) {
		var needle = (value || "").toLowerCase();
		if (!needle) { return longList; }
		var shortList = longList.filter(function (row) {
			return JSON.stringify(row).toLowerCase().indexOf(needle) !== -1;
		});
		return shortList;
	}

	// Walk the parameters we are acting on and let each one narrow the list. The loop is
	// driven by the URL, not by the dataset's `options` — a parameter for a control this
	// dataset doesn't show still filters, which is what makes a hand-typed or shared URL
	// work. A key with no case is not an error: it is `dataset`, or a control that doesn't
	// filter, or a filter not built yet.
	function filterRows(known) {
		// What I need from what is known
		var p = known.params;

		var shortList = known.rawRows;
		p.forEach(function (value, key) {
			switch (key) {
				case "view":     shortList = filterView(value, shortList); break;
				case "keywords": shortList = filterKeywords(value, shortList); break;
				case "types":    shortList = filterTypes(value, shortList); break;
				case "badges":   shortList = filterBadges(value, shortList); break;
				case "access":   shortList = filterAccess(value, shortList); break;
				case "search":   shortList = filterSearch(value, shortList); break;
				default: break; // not a filter — see the comment above
			}
		});
		return shortList;
	}

	// ---- PHASE 3: counting ----------------------------------------------------
	//
	// Every filter value shows how many rows it would match, and the numbers are counted
	// HERE, from the rows the server just sent. They are counted over the UNFILTERED rows: a
	// vocabulary derived from the filtered list would delete the choices you need in order
	// to widen the search next.

	// How many rows carry each value of one tag field. The keys are the vocabulary itself
	// for an OPEN field (keywords); for a CLOSED one (types, badges) the vocabulary lives in
	// og-constants.js and a value nobody used simply has no key here, reading as (0).
	function countTags(rows, field) {
		var counts = {};
		rows.forEach(function (row) {
			var valueList = (row.tags || {})[field] || [];
			valueList.forEach(function (value) {
				counts[value] = (counts[value] || 0) + 1;
			});
		});
		return counts;
	}

	// How many rows derive each road badge. Unmeasured rows land under "unknown", which is
	// never a road value, so it cannot collide with one.
	function countAccess(rows) {
		var counts = {};
		rows.forEach(function (row) {
			var badge = deriveRoadBadge(row.access) || "unknown";
			counts[badge] = (counts[badge] || 0) + 1;
		});
		return counts;
	}

	// The whole set, one walk per field. Phase 3 hands this to the controls as known.counts.
	function collectCounts(known) {
		// What I need from what is known
		var rows = known.rawRows;

		var counts = {
			keywords: countTags(rows, "keywordList"),
			types: countTags(rows, "typeList"),
			badges: countTags(rows, "badgeList"),
			access: countAccess(rows)
		};
		return counts;
	}

	// PHASE 3 step — the keyword vocabulary is PROCESSING, not display. keywords is OPEN, so
	// the values the rows actually carry ARE the choice list. Sorted here, since nothing
	// upstream sorts them any more.
	function extractKeywords(known) {
		// What I need from what is known
		var counted = known.counts.keywords;

		var keywordList = Object.keys(counted).sort();
		return keywordList;
	}

	// ---- renderers: pure (rows -> DOM node) -----------------------------------

	// Card grid. Renders exactly what it is handed; the "has a page" rule is filterView's job.
	function renderGrid(rows) {
		var grid = document.createElement("div");
		grid.className = "og-lb-grid";
		var html = "";
		for (var i = 0; i < rows.length; i++) {
			html += renderCard(rows[i]);
		}
		grid.innerHTML = html;
		window.OG.paintTags(grid);
		return grid;
	}

	/*
	 * The rows, as a mapObject for OG.drawMap — the same shape a page's googleMap entry
	 * resolves to, so this map and a destination page's map are one renderer with one pin
	 * vocabulary.
	 *
	 * A row is a pin or it is nothing: no lat/lng means no pin, silently. That filter has to
	 * happen HERE — a coordinate-less pin is placed at the map's centre, which is what an
	 * authored single-pin map wants, so rows that slipped through would stack in the middle.
	 *
	 * The pin's label is the row's NAME on purpose: location.label is the Location column's
	 * text and holds a town ("Black Creek, BC"), which is not what you want off a marker.
	 */
	function mapObjectFor(rows) {
		var pinList = [];
		rows.forEach(function (row) {
			var loc = row.location || {};
			if (typeof loc.lat !== "number" || typeof loc.lng !== "number") { return; }
			var pin = { lat: loc.lat, lng: loc.lng };
			if (loc.icon) { pin.icon = loc.icon; }
			if (row.name) { pin.label = row.name; }
			var href = pageHref(row);
			if (href) { pin.url = href; }
			pinList.push(pin);
		});
		var mapObject = {
			lat: MAP_CENTER.lat,
			lng: MAP_CENTER.lng,
			zoom: MAP_ZOOM,
			pinList: pinList
		};
		return mapObject;
	}

	// Map view: one pin per row, centre and zoom FIXED.
	//
	// Pure like its siblings, and synchronous like them too: it returns the container, then
	// OG.drawMap fills it. The fill is deferred one tick because a map is sized from its
	// element's box — which does not exist until displayDataset's caller has appended it.
	//
	// The height is set here: drawMap never touches its container's box.
	function renderMap(rows) {
		var box = document.createElement("div");
		box.style.height = MAP_HEIGHT;

		setTimeout(function () {
			window.OG.drawMap(box, mapObjectFor(rows));
		}, 0);

		return box;
	}

	// The match count, and the empty state at the same time: 0 rows reads "0 of 38 found".
	// Always both numbers, even when nothing is filtered out — one format at all times, so
	// the readout never changes shape under you as you filter.
	function buildCount(n, total) {
		var el = document.createElement("div");
		el.className = "og-lb-count";
		el.textContent = n + " of " + total + " found";
		return el;
	}

	// One flat table: colgroup + thead + tbody + numbered footnotes. Renders exactly what it
	// is handed. Per-place footnotes become numbered superscripts on the matching cell,
	// dedup'd into the list below the table.
	//
	// The wrapper scrolls sideways rather than letting a wide table push the page — the
	// table is the one view that can want more room than the page column has.
	function renderTable(rows) {
		var footnoteList = [];

		var table = document.createElement("table");
		table.className = "og-lb-table";
		table.appendChild(buildColGroup());

		var thead = document.createElement("thead");
		var headerRow = document.createElement("tr");
		COLUMNS.forEach(function (col) {
			var th = buildCell("th", col);
			th.textContent = col.label;
			if (col.field === "haversine") {
				var caveat = "Straight-line distance from Campbell River, approximate — not road distance.";
				var idx = footnoteList.indexOf(caveat);
				if (idx === -1) { footnoteList.push(caveat); idx = footnoteList.length - 1; }
				appendNoteMarker(th, idx + 1);
			}
			headerRow.appendChild(th);
		});
		thead.appendChild(headerRow);
		table.appendChild(thead);

		// Source order is authoritative (the dataset is pre-sorted); render as-is.
		var tbody = document.createElement("tbody");
		rows.forEach(function (place) {
			var row = document.createElement("tr");
			COLUMNS.forEach(function (col) {
				var cell = buildCell("td", col);
				fillDataCell(cell, place, col.field);
				(place.footnotes || []).forEach(function (fn) {
					if (fn.field !== col.field) { return; }
					var idx = footnoteList.indexOf(fn.text);
					if (idx === -1) { footnoteList.push(fn.text); idx = footnoteList.length - 1; }
					appendNoteMarker(cell, idx + 1);
				});
				row.appendChild(cell);
			});
			tbody.appendChild(row);
		});
		table.appendChild(tbody);

		var wrap = document.createElement("div");
		wrap.className = "og-lb-tablewrap";
		wrap.appendChild(table);

		var out = document.createElement("div");
		out.appendChild(wrap);
		if (footnoteList.length) { out.appendChild(buildFootnoteList(footnoteList)); }
		return out;
	}

	// ---- PHASE 4: display ------------------------------------------------------
	//
	// Two views on what is known. Both are PURE — known -> DOM node. They read the bag,
	// never write it, and never touch the page: start() does the appending, so the order the
	// two land in is visible at the top level instead of hidden inside them.

	// No fetching and no filtering: it is handed the rows and draws them. The final else is
	// not parameter validation — the page has to render SOMETHING, and the table is the
	// honest choice because it is the one view that shows everything.
	function displayDataset(known) {
		// What I need from what is known
		var rows = known.filteredRows;
		var view = known.params.get("view");

		var node;
		if (view === "grid") {
			node = renderGrid(rows);
		} else if (view === "table") {
			node = renderTable(rows);
		} else if (view === "map") {
			node = renderMap(rows);
		} else {
			console.error("[browser] Unknown view " + view + " — showing the table");
			node = renderTable(rows);
		}
		return node;
	}

	// ---- options bar (controls) -----------------------------------------------
	//
	// Every control has the SAME signature — build(known) -> DOM node, or nothing — and
	// opens by naming what it needs out of the bag. They are parallel siblings with
	// genuinely different needs, which is why they are handed everything rather than an
	// argument list that is the union of all of them.

	// The single seam every control commits through: clone what the page is acting on, apply
	// the patch, drop empty values (so a cleared filter leaves no parameter), and move to the
	// new URL. Assigning location.search keeps path and hash and is a real navigation, so
	// Back/Forward restore prior filter states with no extra machinery. Parameters this
	// control doesn't own (notably `dataset`) survive because we clone rather than rebuild.
	// known.params is never mutated: the patch goes onto a copy.
	function navigate(known, patch) {
		var next = new URLSearchParams(known.params);
		for (var key in patch) {
			var value = patch[key];
			if (value === null || value === undefined || value === "") { next.delete(key); }
			else { next.set(key, value); }
		}
		location.search = next.toString();
	}

	// Segmented view toggle; the current view carries .is-active. Commits immediately on
	// click — a single unambiguous choice, nothing to accumulate. VALID_VIEWS is the whole
	// vocabulary and the button label is the value itself (capitalized in CSS), so a new
	// view needs no label of its own.
	function buildViewToggle(known) {
		// What I need from what is known
		var current = known.params.get("view");

		var wrap = document.createElement("div");
		wrap.className = "og-lb-view";
		VALID_VIEWS.forEach(function (v) {
			var b = document.createElement("button");
			b.type = "button";
			b.textContent = v;
			if (v === current) { b.className = "is-active"; }
			b.addEventListener("click", function () { navigate(known, { view: v }); });
			wrap.appendChild(b);
		});
		return wrap;
	}

	/*
	 * One multi-select checkbox dropdown (<details>/<summary> disclosure) over `valueList`,
	 * with the ?<paramName>= values pre-checked. Shared by the keyword, type and badge
	 * filters — they differ only in label, parameter, and vocabulary. snake_case values show
	 * with spaces. An empty vocabulary builds nothing.
	 *
	 * Each value carries how many rows it would match. A value with no key counts 0 — which
	 * happens only in a CLOSED vocabulary, where the zero is worth showing: the dropdown is
	 * advertising room the data has not used yet.
	 *
	 * Commits when the panel CLOSES, not per checkbox: navigating on every tick would reload
	 * the page and shut the panel, so only one value could ever be picked per visit.
	 */
	function buildCheckboxDropdown(label, paramName, valueList, counts, known) {
		// Nothing to pick is not a control. An absent vocabulary and an empty one are the
		// same thing to a reader, so both build nothing.
		if (!valueList || !valueList.length) { return null; }

		// What I need from what is known
		var selected = splitList(known.params.get(paramName));

		var chosen = valueList.filter(function (v) { return selected.indexOf(v) !== -1; });
		var boxList = [];

		var details = document.createElement("details");
		details.className = "og-lb-dropdown";

		var summary = document.createElement("summary");
		summary.textContent = chosen.length ? label + " (" + chosen.length + ")" : label;
		details.appendChild(summary);

		var panel = document.createElement("div");
		panel.className = "og-lb-panel";

		valueList.forEach(function (v) {
			var lab = document.createElement("label");
			var cb = document.createElement("input");
			cb.type = "checkbox";
			cb.value = v;
			if (selected.indexOf(v) !== -1) { cb.checked = true; }
			boxList.push(cb);
			lab.appendChild(cb);
			lab.appendChild(document.createTextNode(v.replace(/_/g, " ") + " (" + (counts[v] || 0) + ")"));
			panel.appendChild(lab);
		});

		// The checked set in valueList order, so it compares cleanly against `chosen` and the
		// emitted parameter is order-stable no matter what order the boxes were hit.
		function checkedValues() {
			var checkedList = valueList.filter(function (v, i) { return boxList[i].checked; });
			return checkedList;
		}

		// `toggle` fires on open too — only the close edge commits.
		details.addEventListener("toggle", function () {
			if (details.open) { return; }
			var now = checkedValues();
			if (now.join(",") === chosen.join(",")) { return; }
			chosen = now;
			summary.textContent = chosen.length ? label + " (" + chosen.length + ")" : label;
			var patch = {};
			patch[paramName] = now.join(",");
			navigate(known, patch);
		});

		details.appendChild(panel);
		return details;
	}

	/*
	 * Access filter — a SINGLE-choice threshold over the severity-ordered road set. You pick
	 * the WORST road you will accept; "any" (the default) applies no filter. A plain
	 * <select>, not checkboxes — the set is fixed and only one value applies.
	 *
	 * The count on an option is CUMULATIVE, because that is what the option means: picking
	 * "dirt" keeps pavement and unpaved rows too, so the number has to be the running total
	 * down the ramp. Unmeasured rows (counted under "unknown") pass every threshold, so they
	 * seed the total rather than sitting in one option.
	 */
	function buildAccessSelect(known) {
		// What I need from what is known
		var current = known.params.get("access") || "";
		var counts = known.counts.access;

		var sel = document.createElement("select");
		sel.className = "og-lb-select";

		var any = document.createElement("option");
		any.value = "";
		any.textContent = "Access: any";
		sel.appendChild(any);

		var running = counts.unknown || 0;
		roadOrder().forEach(function (r) {
			running += counts[r] || 0;
			var opt = document.createElement("option");
			opt.value = r;
			opt.textContent = r.replace(/_/g, " ") + " (" + running + ")";
			if (r === current) { opt.selected = true; }
			sel.appendChild(opt);
		});
		// Commits immediately — one choice, and "any" ("") clears the parameter.
		sel.addEventListener("change", function () { navigate(known, { access: sel.value }); });
		return sel;
	}

	// Free-text search box, prefilled with the current ?search= value. Commits on losing
	// focus and only when the text actually changed, so tabbing through the bar navigates
	// nothing. `committed` is updated BEFORE navigate() because anything that steals focus
	// can re-enter this handler — the guard has to already be closed by then.
	function buildSearchBox(known) {
		// What I need from what is known
		var current = known.params.get("search") || "";

		var input = document.createElement("input");
		input.type = "text";
		input.className = "og-lb-search";
		input.placeholder = "Search";
		input.value = current;

		var committed = input.value;
		input.addEventListener("blur", function () {
			var value = input.value.trim();
			if (value === committed) { return; }
			committed = value;
			navigate(known, { search: value });
		});
		// Enter routes through blur so there is ONE commit path. The input is not in a
		// <form>, so Enter would otherwise do nothing at all.
		input.addEventListener("keydown", function (e) {
			if (e.key === "Enter") { input.blur(); }
		});
		return input;
	}

	// Link to a dataset's companion booklet PDF, e.g. ?booklet=howto -> /document/howto.
	// The parameter names the BOOKLET, not the file; the /document/ path is this builder's
	// business.
	//
	// No parameter, NO BUTTON — returning nothing is a normal outcome, the same way a filter
	// reads its parameter and applies nothing when it is absent. The dataset's `options` says
	// a booklet is POSSIBLE here; the URL says whether this page has one. That keeps every
	// value in one place: the query string.
	function buildBookletLink(known) {
		// What I need from what is known
		var name = known.params.get("booklet");

		if (!name) { return null; }
		// A booklet is one of our own documents, so linkTo gives it the new tab every
		// /document/ link gets, and the list keeps its filters while it is read.
		var a = linkTo(BOOKLET_URL + name, "Open booklet (PDF)");
		a.className = "og-lb-booklet";
		return a;
	}

	// Token -> control builder, all sharing one signature: build(known) -> DOM node, or
	// nothing. datasets.json's `options` array names which controls a dataset shows, and in
	// what order; buildOptionsBar walks it through this map.
	var CONTROL_BUILDERS = {
		view: function (known) { return buildViewToggle(known); },
		types: function (known) {
			return buildCheckboxDropdown("Types", "types", window.OG.DESTINATION_TYPES, known.counts.types, known);
		},
		keywords: function (known) {
			return buildCheckboxDropdown("Keywords", "keywords", known.keywords, known.counts.keywords, known);
		},
		badges: function (known) {
			return buildCheckboxDropdown("Badges", "badges", Object.keys(window.OG.TAG_COLORS), known.counts.badges, known);
		},
		access: function (known) { return buildAccessSelect(known); },
		search: function (known) { return buildSearchBox(known); },
		booklet: function (known) { return buildBookletLink(known); }
	};

	// Assemble the bar from the dataset's `options` recipe (order preserved).
	//
	// A token with no builder builds NOTHING, silently — the mirror of the filter switch's
	// default. It is a legitimate state, not an error: a control can be designed before it is
	// built, and a token need not be about controls at all.
	//
	// A builder may also return NOTHING. `options` says which controls are possible for this
	// dataset; the parameters say what to do about them. Values live in the query string
	// only — a token is a plain name, never "name=value".
	function buildOptionsBar(options, known) {
		var bar = document.createElement("div");
		bar.className = "og-lb-bar";
		(options || []).forEach(function (token) {
			var build = CONTROL_BUILDERS[token];
			if (!build) { return; }
			var node = build(known);
			if (node) { bar.appendChild(node); }
		});
		// The match count rides at the far right (margin-left:auto), appended last so it
		// lands after any booklet control. It is a readout, not a token, so it is not in
		// CONTROL_BUILDERS — it belongs to the bar, on every dataset.
		bar.appendChild(buildCount(known.filteredRows.length, known.rawRows.length));
		return bar;
	}

	// The other half of PHASE 4. The bar IS the options row, so it is returned as it stands —
	// no wrapper div that would exist only for symmetry.
	function displayOptionsRow(known) {
		// What I need from what is known
		var options = known.dataset.options;

		return buildOptionsBar(options, known);
	}

	// ---- the four phases, and the only code in this file that runs ------------
	//
	//   <div data-block-type="browser"></div>
	//
	// The element takes no attributes and the page carries no data: the whole state is the
	// URL. The catch puts the failure on the page as it stands — the same text on screen and
	// in the console. A missing file never loads on a retry, so there is no "temporarily
	// unavailable" to claim, and the file name says instantly whether it was a rename, a bad
	// `file` in datasets.json, or a dataset id that does not exist.
	function start(el) {
		el.innerHTML = "";

		// `known` holds KNOWLEDGE only — no DOM. The appending happens here.
		var known = {};

		// ---- PHASE 1: params ---------------------------------------------------
		known.params = processParams(window.location.search);

		// ---- PHASE 2: load. Sequential — the first file names the second --------
		fetchJson(DATASETS_URL)
			.then(function (datasetList) {
				known.datasetList = datasetList;
				known.dataset = pickDataset(known);

				return fetchJson(DATA_URL + known.dataset.id)
					.then(function (rawRows) {
						known.rawRows = rawRows;

						// ---- PHASE 3: process. All of it, up front ---------------------
						known.filteredRows = filterRows(known);
						known.counts = collectCounts(known);
						known.keywords = extractKeywords(known);

						// ---- PHASE 4: display. READ ONLY from here ---------------------
						el.appendChild(displayOptionsRow(known));
						el.appendChild(displayDataset(known));
					});
			})
			.catch(function (err) {
				console.error("[browser] " + err.message, err);
				el.textContent = err.message;
			});
	}

	document.addEventListener("DOMContentLoaded", function () {
		var el = document.querySelector('[data-block-type="browser"]');
		if (!el) { return; }
		start(el);
	});
})();
