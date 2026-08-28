/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The site's shared rendering. What more than one place draws lives here, so a thing that
 * looks the same on a gallery card and on a page IS the same code.
 *
 * Colour is the first of these. A pill carries its word in data-tag or data-road and no
 * colour of its own; paintTags fills it from the palettes in gl-constants.js. That is what
 * lets a Thymeleaf fragment, which cannot read those palettes, write the same pill the
 * gallery card writes.
 *
 * The back link is here for the same reason: it is built from the referrer, which the
 * server never sees while rendering, so the fragment writes the fallback and paintBackLink
 * writes the link.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.GL = window.GL || {};

	// The gallery page every back link returns to. It is browser.js that owns the page.
	var BROWSER_PATH = "/shared/browser";

	/*
	 * What a gallery is called in a sentence, keyed by its dataset id. These DUPLICATE the
	 * `title` of each entry in /shared/browser/datasets.json, deliberately: a page that is
	 * not the gallery has no reason to load that file, and fetching it for three words would
	 * buy async machinery for nothing. Add a dataset there, add its name here — by hand.
	 */
	var GALLERY_NAMES = {
		"destinations": "Destinations",
		"van-howto": "How to",
		"van-checklist": "Checklists"
	};

	// What the link says when the dataset id is one this file has never heard of.
	var UNNAMED_GALLERY = "the gallery";

	/*
	 * Fill every pill under root — the whole document when no root is given. Markup built
	 * after load passes the element it just filled.
	 *
	 * data-tag is an OPEN vocabulary: a word the palette does not know gets the fallback, so
	 * a new badge is visible the day it is authored. data-road is CLOSED: an unknown value is
	 * a data error, and the badge goes away rather than showing uncoloured.
	 */
	window.GL.paintTags = function (root) {
		var scope = root || document;

		var pillList = scope.querySelectorAll("[data-tag]");
		for (var i = 0; i < pillList.length; i++) {
			var pill = pillList[i];
			var word = pill.getAttribute("data-tag");
			var tagColors = window.GL.TAG_COLORS[word] || window.GL.TAG_FALLBACK;
			pill.style.background = tagColors.bg;
			pill.style.color = tagColors.text;
		}

		var badgeList = scope.querySelectorAll("[data-road]");
		for (var j = 0; j < badgeList.length; j++) {
			var badge = badgeList[j];
			var road = badge.getAttribute("data-road");
			var roadColors = window.GL.ROAD_COLORS[road];
			if (!roadColors) {
				console.error('[offgrid] Unknown road value "' + road + '" — no road badge rendered.');
				badge.parentNode.removeChild(badge);
				continue;
			}
			badge.style.background = roadColors.bg;
			badge.style.color = roadColors.text;
		}
	};

	/*
	 * The referrer, but only when it really is this site's browser page — same origin AND
	 * same path. Anything else (off site, another page of this site, an unparseable or
	 * absent referrer) answers null, which is what sends the caller to the page's fallback.
	 * Answers the URL object, so the caller can read both the href and the dataset off it.
	 */
	function browserReferrer() {
		if (!document.referrer) { return null; }

		var url;
		try {
			url = new URL(document.referrer);
		} catch (err) {
			return null;
		}

		if (url.origin !== window.location.origin) { return null; }
		// A trailing slash is optional on the way in.
		var path = url.pathname.replace(/\/+$/, "");
		if (path !== BROWSER_PATH) { return null; }
		return url;
	}

	/*
	 * The name the link says: the gallery it returns to, never the view. Which face of the
	 * gallery the visitor left — grid, table, map — is carried in the URL and restored on
	 * arrival, so saying it in the label only names machinery.
	 *
	 * Looked up, never taken as text: the id reaches here from the referrer, so an id this
	 * file does not know is named generically rather than rendered.
	 */
	function galleryName(url) {
		var id = url.searchParams.get("dataset");
		var name = GALLERY_NAMES[id] || UNNAMED_GALLERY;
		return name;
	}

	/*
	 * Fill every back link under root — the whole document when no root is given.
	 *
	 * Arrived from the browser page: that URL IS the link, verbatim. It carries the visitor's
	 * whole context back — dataset, view and every filter — with nothing to parse or rebuild.
	 * Arrived any other way (bookmark, search engine, a link from another page): there is no
	 * context to return to, so the page's own data-fallback names the gallery it belongs to.
	 */
	window.GL.paintBackLink = function (root) {
		var scope = root || document;

		var from = browserReferrer();
		var blockList = scope.querySelectorAll("[data-fallback]");
		for (var i = 0; i < blockList.length; i++) {
			var block = blockList[i];

			var url = from;
			if (!url) {
				var fallbackQuery = block.getAttribute("data-fallback");
				var fallbackHref = BROWSER_PATH + "?" + fallbackQuery;
				url = new URL(fallbackHref, window.location.origin);
			}
			var name = galleryName(url);

			var link = document.createElement("a");
			link.href = url.href;
			link.textContent = "\u2190 Back to " + name;
			block.innerHTML = "";
			block.appendChild(link);
		}
	};

	document.addEventListener("DOMContentLoaded", function () {
		window.GL.paintTags();
		window.GL.paintBackLink();
	});
})();
