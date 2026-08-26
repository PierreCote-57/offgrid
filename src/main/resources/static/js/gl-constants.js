/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The site's vocabularies. Every ALL-CAPS value that more than one script may want lives
 * here, on window.GL, so a word means one thing everywhere it is painted or filtered on.
 *
 * Renderer-specific configuration stays with its renderer; only genuinely shared
 * vocabularies belong here.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.GL = window.GL || {};

	// Tag-chip colours, keyed by the badge word.
	window.GL.TAG_COLORS = {
		camping: { bg: "rgba(31, 158, 117, 0.88)", text: "#E1F5EE" },
		fishing: { bg: "rgba(55, 138, 221, 0.88)", text: "#E6F1FB" },
		hiking:  { bg: "rgba(216, 90, 48, 0.88)",  text: "#FAECE7" },
		picnic:  { bg: "rgba(186, 117, 23, 0.88)", text: "#FAEEDA" }
	};
	window.GL.TAG_FALLBACK = { bg: "rgba(95, 94, 90, 0.88)", text: "#F1EFE8" };

	/*
	 * What KIND of place a destination is, carried as tags.types. A CLOSED vocabulary,
	 * unlike tags.keywords, so the browser page can offer these as choices and show a count
	 * of zero against one the data has not used yet.
	 *
	 * Alphabetical: the order carries no meaning, it is only the order the dropdown lists
	 * them in, and alphabetical is the order that does not shift as data lands.
	 *
	 * A type is NEVER mandatory, and that is what keeps `park` meaningful: a city park is a
	 * very different thing from a provincial park, so it carries no type at all rather than
	 * a wrong one.
	 */
	window.GL.DESTINATION_TYPES = ["campground", "lake", "park", "rec-site"];

	/*
	 * What a link IS, carried as `type` on a links[] entry. CLOSED, and the key every
	 * renderer selects by — the label is display text and nothing else. Optional, like
	 * tags.types: a link that is none of these keeps its label and carries no type.
	 */
	window.GL.LINK_TYPES = ["homepage", "map", "reservation"];

	/*
	 * Road-condition badge colours. Severity ramp, easy -> hard: blue -> green -> orange ->
	 * rust -> red. Two values sit OFF that ramp, for opposite reasons: black back_country is
	 * the absence of a ROAD, not the worst one; grey unpaved is the absence of a JUDGEMENT —
	 * a non-paved tail has been measured, but nobody has driven it and said what it does to
	 * the van. Strict vocabulary, so there is intentionally NO fallback — an unknown value
	 * renders nothing.
	 *
	 * The KEY ORDER is the severity order the access filter reads as its threshold ramp.
	 */
	window.GL.ROAD_COLORS = {
		pavement:     { bg: "rgba(45, 110, 200, 0.9)",  text: "#E6F0FB" },
		unpaved:      { bg: "rgba(120, 118, 112, 0.9)", text: "#F1EFE8" },
		dirt:         { bg: "rgba(46, 133, 85, 0.9)",   text: "#E4F5EC" },
		potholes:     { bg: "rgba(196, 100, 28, 0.95)", text: "#FCEDE0" },
		sharp_rock:   { bg: "rgba(190, 76, 34, 0.94)",  text: "#FBE8DF" },
		rugged:       { bg: "rgba(178, 55, 44, 0.94)",  text: "#FBE4E1" },
		back_country: { bg: "rgba(20, 20, 20, 0.94)",   text: "#EDEDED" }
	};

	/*
	 * Severity order of the AUTHORABLE drive leg types, easiest -> hardest, as experienced by
	 * THE van. This is configuration, not truth: the ranking follows from the vehicle, so a
	 * different van can reorder it.
	 *
	 * Two badge values are deliberately absent, because this list doubles as the set of leg
	 * types that may be AUTHORED, and neither of them ever is:
	 *   pavement     — derived from legs: [] ("paved all the way").
	 *   back_country — derived whenever a leg is non-drive (walk/hike/boat). Not "worse road"
	 *                  but "no road", so it is never ranked.
	 * Both live in ROAD_COLORS only, to be painted.
	 *
	 * unpaved ranks mildest ON PURPOSE. It means a non-paved tail was measured but its
	 * character is not yet refined, so any known type must outrank it and take the badge;
	 * its grey, not its position, is what says "unknown".
	 */
	window.GL.ROAD_RANK = ["unpaved", "dirt", "potholes", "sharp_rock", "rugged"];

	/*
	 * Leg types where you are no longer in the van. They carry no severity rank — any one of
	 * them derives the back_country badge, which is the whole point of the distinction.
	 * walk vs hike is about footing, not distance: walk is an improved surface, hike is trail.
	 */
	window.GL.NON_DRIVE_LEG_TYPES = ["walk", "hike", "boat"];
})();
