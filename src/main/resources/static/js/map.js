/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The site's one map. Every map on the site is drawn here: a page's googleMap block and the
 * browser page's map view both hand drawMap the same mapObject, so the pins, the icons and
 * the caption are one implementation.
 *
 * STUB — drawMap writes "Map goes here" into its container. The real Google Maps rendering
 * is its own piece of work.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.GL = window.GL || {};

	/*
	 * Draw a map into a container.
	 *
	 * box       the element to draw into; drawMap never touches its box, so the caller owns
	 *           the width and the height.
	 * mapObject { lat, lng, zoom, pinList } — the centre, the zoom, and one entry per pin.
	 */
	window.GL.drawMap = function (box, mapObject) {
		box.textContent = "Map goes here";
	};
})();
