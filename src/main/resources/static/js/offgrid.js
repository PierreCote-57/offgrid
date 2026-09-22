/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The site's shared code. What more than one place needs lives here, so a thing that looks
 * the same on a gallery card and on a page IS the same code, and a question asked of the
 * browser is asked in one voice.
 *
 * Colour is the first of these. A pill carries its word in data-tag or data-road and no
 * colour of its own; paintTags fills it from the palettes in og-constants.js. That is what
 * lets a Thymeleaf fragment, which cannot read those palettes, write the same pill the
 * gallery card writes.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.OG = window.OG || {};

	/*
	 * Fill every pill under root — the whole document when no root is given. Markup built
	 * after load passes the element it just filled.
	 *
	 * The selector is the CLASS. og-tag is what a pill IS; data-tag only says which colour
	 * to give it. Selecting on the attribute meant a pill that arrived without one was
	 * skipped in silence, and looked right only for as long as the page behind it did.
	 *
	 * data-tag is an OPEN vocabulary: a word the palette does not know gets the fallback, so
	 * a new badge is visible the day it is authored. data-road is CLOSED: an unknown value is
	 * a data error, and the badge goes away rather than showing uncoloured.
	 *
	 * A road badge on a page IS a og-tag — it sits in the tag row and wears the pill shape —
	 * so the first pass leaves anything carrying data-road to the second.
	 */
	window.OG.paintTags = function (root) {
		var scope = root || document;

		var pillList = scope.querySelectorAll(".og-tag:not([data-road])");
		for (var i = 0; i < pillList.length; i++) {
			var pill = pillList[i];
			var word = pill.getAttribute("data-tag");
			if (!word) {
				console.error('[offgrid] A og-tag with no data-tag: "' + pill.textContent +
					'" — painted from the fallback.');
			}
			var tagColors = window.OG.TAG_COLORS[word] || window.OG.TAG_FALLBACK;
			pill.style.background = tagColors.bg;
			pill.style.color = tagColors.text;
		}

		var badgeList = scope.querySelectorAll("[data-road]");
		for (var j = 0; j < badgeList.length; j++) {
			var badge = badgeList[j];
			var road = badge.getAttribute("data-road");
			var roadColors = window.OG.ROAD_COLORS[road];
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
	 * The site's ONE link decision, as the opening <a> tag only — the twin of the
	 * fragments/block/link.html fragment, same input and same output. The caller writes
	 * whatever goes between the tags and closes it.
	 *
	 *   external            -> new tab, rel=noopener
	 *   internal /document/ -> new tab, no rel (it is our own file)
	 *   internal page       -> this tab, bare anchor
	 */
	window.OG.linkOpenTag = function (url) {
		var external = url.charAt(0) !== "/";
		var newTab = external || url.indexOf("/document/") === 0;

		var openTag = '<a href="' + escapeAttr(url) + '"';
		if (newTab) { openTag += ' target="_blank"'; }
		if (external) { openTag += ' rel="noopener"'; }
		openTag += ">";
		return openTag;
	};

	// Minimal attribute escaping for a value going into a quoted attribute.
	function escapeAttr(value) {
		var escaped = String(value)
			.replace(/&/g, "&amp;")
			.replace(/"/g, "&quot;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;");
		return escaped;
	}

	// How long the browser is given to answer where the visitor is, and how old an answer it may
	// reuse.
	var POSITION_TIMEOUT_MS = 5000;
	var POSITION_MAX_AGE_MS = 600000;

	// Asked of the browser once and reused for the rest of the page view: every caller that asks
	// again is another callback and another wait, and a visitor does not move between two clicks
	// on the same page.
	var positionPromise = null;

	/*
	 * Where the visitor stands, as a promise that is made once. Refused, timed out, unavailable
	 * or simply not offered by the browser, it answers null, and the caller settles for whatever
	 * it does without one. The browser keeps the visitor's answer against the site, so a grant
	 * made on one page is already given on the next.
	 */
	window.OG.positionOf = function () {
		if (positionPromise) {
			return positionPromise;
		}

		positionPromise = new Promise(function (resolve) {
			if (!navigator.geolocation) {
				console.log("[offgrid] This browser states no position.");
				resolve(null);
				return;
			}

			var startedAt = Date.now();
			navigator.geolocation.getCurrentPosition(
				function (position) {
					console.log("[offgrid] Position after " + (Date.now() - startedAt) + "ms.");
					resolve(position);
				},
				function (failure) {
					console.log("[offgrid] No position after " + (Date.now() - startedAt) + "ms: "
						+ failure.message + ".");
					resolve(null);
				},
				{ timeout: POSITION_TIMEOUT_MS, maximumAge: POSITION_MAX_AGE_MS });
		});

		return positionPromise;
	};

	document.addEventListener("DOMContentLoaded", function () {
		window.OG.paintTags();
	});
})();
