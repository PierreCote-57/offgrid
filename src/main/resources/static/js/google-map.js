/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The site's one map. Every map on the site is drawn here: a page's googleMap block and the
 * browser page's map view both hand drawMap the same mapObject, so the pins, the icons and
 * the caption are one implementation.
 *
 * Drawing only. PREPARING a mapObject belongs to whoever holds the data — the block fragment
 * out of the page JSON, browser.js out of its hydrated rows — so nothing here reads a page,
 * fetches a file, or resolves a name.
 */
(function () {
	"use strict";

	// The shared registry, created defensively: this file may execute before OR after the
	// other scripts that add to it.
	window.OG = window.OG || {};

	/*
	 * Load the Google Maps API, then call onReady. The script tag is injected once per page
	 * however many maps the page draws: the first call injects it and the ones that follow
	 * queue their callback behind it. A page that already has the API calls back straight
	 * away, synchronously.
	 */
	function loadGoogleMapsApi(onReady) {
		if (window.google && window.google.maps) {
			onReady();
			return;
		}
		if (window.__offgridMapsApiLoading) {
			window.__offgridMapsApiCallbacks.push(onReady);
			return;
		}
		window.__offgridMapsApiLoading = true;
		window.__offgridMapsApiCallbacks = [onReady];
		window.__offgridMapsApiInit = function () {
			window.__offgridMapsApiCallbacks.forEach(function (callback) {
				callback();
			});
		};
		var script = document.createElement("script");
		var mapConfig = window.OG.MAP_CONFIG;
		script.src = "https://maps.googleapis.com/maps/api/js?key=" + mapConfig.mapApiKey +
			"&loading=async&callback=__offgridMapsApiInit";
		script.async = true;
		document.head.appendChild(script);
	}

	// Exposed because the loader is already built to serve several callers on one page, and
	// the geocoder needs the same script the maps do.
	window.OG.loadGoogleMapsApi = loadGoogleMapsApi;

	/*
	 * Build a marker icon from a pin's icon word, via OG.PIN_ICONS. An absent or unknown word
	 * hands back null, and the marker is Google's own pin — a pin nobody gave a figure to is
	 * still a pin.
	 */
	function pinIcon(word) {
		var svg = window.OG.PIN_ICONS && window.OG.PIN_ICONS[word];
		if (!svg) {
			return null;
		}
		var icon = {
			url: "data:image/svg+xml," + encodeURIComponent(svg),
			scaledSize: new google.maps.Size(48, 48),
			anchor: new google.maps.Point(24, 24)
		};
		return icon;
	}

	/*
	 * An <a> around whatever the InfoWindow is showing, so a pin that has BOTH a photo and a
	 * url keeps both: the photo still opens, and it is the link.
	 */
	function pinLink(url, child) {
		var holder = document.createElement("div");
		holder.innerHTML = window.OG.linkOpenTag(url) + "</a>";
		var link = holder.firstChild;
		link.appendChild(child);
		return link;
	}

	/*
	 * Drop one pin onto the map. pin = { lat?, lng?, icon?, label?, img?, url? }.
	 *   lat/lng absent -> the pin sits at the map's centre, which is what a single-pin
	 *                     authored map wants.
	 *   icon           -> the figure from OG.PIN_ICONS, else the default marker.
	 *   img            -> click opens an InfoWindow with the photo, labelled.
	 *   url            -> the pin is a link. No img: the click navigates. With an img: the
	 *                     photo and the header carry the link, so neither field cancels the
	 *                     other out.
	 *   label alone    -> the native hover title.
	 *
	 * img and url are both READY: an image URL and an href, resolved by whoever built the
	 * mapObject. Nothing is looked up here.
	 */
	function renderPin(map, infoWindow, pin, centre) {
		var lat = (typeof pin.lat === "number") ? pin.lat : centre.lat;
		var lng = (typeof pin.lng === "number") ? pin.lng : centre.lng;
		if (typeof lat !== "number" || typeof lng !== "number") {
			return;
		}
		var markerOptions = { position: { lat: lat, lng: lng }, map: map };
		var icon = pinIcon(pin.icon);
		if (icon) {
			markerOptions.icon = icon;
		}
		if (!pin.img && pin.label) {
			markerOptions.title = pin.label;
		}
		var marker = new google.maps.Marker(markerOptions);
		if (pin.img) {
			marker.addListener("click", function () {
				var photo = document.createElement("img");
				photo.src = pin.img;
				photo.alt = pin.label || "";
				photo.style.display = "block";
				photo.style.maxWidth = "320px";
				photo.style.maxHeight = "240px";
				var label = pin.label || "";
				var header = pin.url ? pinLink(pin.url, document.createTextNode(label)) : label;
				var body = pin.url ? pinLink(pin.url, photo) : photo;
				infoWindow.setHeaderContent(header);
				infoWindow.setContent(body);
				infoWindow.open(map, marker);
			});
		} else if (pin.url) {
			marker.addListener("click", function () {
				window.location.href = pin.url;
			});
		}
	}

	/*
	 * Draw a mapObject into a box.
	 *
	 * box       the element to draw into. THE CALLER OWNS ITS BOX — width, height, float and
	 *           margin are never read or written here. A page block is sized by the div the
	 *           page wrote around the fragment; the browser page sizes its own container.
	 * mapObject { lat, lng, zoom, pinList } — the centre, the zoom, and one entry per pin.
	 *
	 * box is the WRAPPER; the map and the caption are both children of it. That containment
	 * is what makes the caption the width of the map instead of the page column, and the flex
	 * column in site.css is what splits the box's height between the two. Both children are
	 * in place BEFORE the API answers, so Google sizes the map once, against a box that
	 * already allows for the caption.
	 */
	window.OG.drawMap = function (box, mapObject) {
		box.textContent = "";
		box.classList.add("og-mapbox");

		var mapEl = document.createElement("div");
		mapEl.className = "og-map";
		box.appendChild(mapEl);

		// The live centre/zoom readout under every map, following pan and zoom through the
		// map's "idle" event, so the current values can be read straight off it.
		var caption = document.createElement("div");
		caption.className = "og-map-caption";
		box.appendChild(caption);

		loadGoogleMapsApi(function () {
			var mapConfig = window.OG.MAP_CONFIG;
			var zoom = (typeof mapObject.zoom === "number") ? mapObject.zoom : mapConfig.mapZoom;
			var map = new google.maps.Map(mapEl, {
				zoom: zoom,
				center: { lat: mapObject.lat, lng: mapObject.lng },
				mapTypeId: mapConfig.mapTypeId,
				styles: mapConfig.mapStyles
			});
			var infoWindow = new google.maps.InfoWindow();
			var pinList = mapObject.pinList || [];
			pinList.forEach(function (pin) {
				renderPin(map, infoWindow, pin, mapObject);
			});
			var updateCaption = function () {
				var centre = map.getCenter();
				caption.textContent = "Center = (" + centre.lat().toFixed(6) + ", " +
					centre.lng().toFixed(6) + ")   ·   Zoom = " + map.getZoom();
			};
			map.addListener("idle", updateCaption);
			updateCaption();
		});
	};
})();
