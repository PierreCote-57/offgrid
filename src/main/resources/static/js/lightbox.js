/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The pop-up image viewer. Any <a data-lightbox> opens it instead of navigating; the links
 * inside one gallery grid page through each other, and a link on its own opens alone. The
 * href stays a working link, so with JavaScript off a click still shows the image.
 */
(function () {
	"use strict";

	var overlay = null;
	var imgEl, captionEl, hiresLink, counterEl, prevBtn, nextBtn, closeBtn;
	var entryList = [];
	var index = 0;
	var lastFocus = null;

	function build() {
		overlay = document.createElement("div");
		overlay.className = "gl-lightbox";
		overlay.setAttribute("role", "dialog");
		overlay.setAttribute("aria-modal", "true");
		overlay.setAttribute("aria-hidden", "true");

		closeBtn = button("gl-lightbox-close", "Close", "&times;");
		prevBtn = button("gl-lightbox-nav gl-lightbox-prev", "Previous", "&#8249;");
		nextBtn = button("gl-lightbox-nav gl-lightbox-next", "Next", "&#8250;");

		var figure = document.createElement("figure");
		figure.className = "gl-lightbox-figure";

		imgEl = document.createElement("img");
		imgEl.className = "gl-lightbox-img";
		imgEl.alt = "";

		captionEl = document.createElement("figcaption");
		captionEl.className = "gl-lightbox-caption";

		hiresLink = document.createElement("a");
		hiresLink.className = "gl-lightbox-hires";
		hiresLink.target = "_blank";
		hiresLink.rel = "noopener";
		hiresLink.textContent = "Full size";

		counterEl = document.createElement("div");
		counterEl.className = "gl-lightbox-counter";

		figure.appendChild(imgEl);
		figure.appendChild(captionEl);
		figure.appendChild(hiresLink);

		overlay.appendChild(closeBtn);
		overlay.appendChild(prevBtn);
		overlay.appendChild(nextBtn);
		overlay.appendChild(figure);
		overlay.appendChild(counterEl);
		document.body.appendChild(overlay);

		closeBtn.addEventListener("click", close);
		prevBtn.addEventListener("click", function () { step(-1); });
		nextBtn.addEventListener("click", function () { step(1); });
		overlay.addEventListener("click", function (event) {
			if (event.target === overlay) { close(); }
		});
		document.addEventListener("keydown", onKey);
	}

	function button(className, label, glyph) {
		var element = document.createElement("button");
		element.type = "button";
		element.className = className;
		element.setAttribute("aria-label", label);
		element.innerHTML = glyph;
		return element;
	}

	function isOpen() {
		var open = overlay && overlay.getAttribute("aria-hidden") === "false";
		return open;
	}

	function onKey(event) {
		if (!isOpen()) { return; }
		if (event.key === "Escape") { close(); }
		else if (event.key === "ArrowLeft") { step(-1); }
		else if (event.key === "ArrowRight") { step(1); }
	}

	function render() {
		var entry = entryList[index];
		imgEl.src = entry.src;
		imgEl.alt = entry.caption;
		hiresLink.href = entry.hires;
		captionEl.textContent = entry.caption;
		captionEl.style.display = entry.caption ? "block" : "none";

		var multiple = entryList.length > 1;
		prevBtn.style.display = multiple ? "block" : "none";
		nextBtn.style.display = multiple ? "block" : "none";
		counterEl.style.display = multiple ? "block" : "none";
		counterEl.textContent = (index + 1) + " / " + entryList.length;
	}

	function step(delta) {
		if (entryList.length < 2) { return; }
		index = (index + delta + entryList.length) % entryList.length;
		render();
	}

	function open(list, start) {
		if (!list.length) { return; }
		if (!overlay) { build(); }
		entryList = list;
		index = start;
		lastFocus = document.activeElement;
		render();
		overlay.setAttribute("aria-hidden", "false");
		document.body.style.overflow = "hidden";
		closeBtn.focus();
	}

	function close() {
		if (!overlay) { return; }
		overlay.setAttribute("aria-hidden", "true");
		imgEl.src = "";
		document.body.style.overflow = "";
		if (lastFocus) { lastFocus.focus(); }
	}

	/**
	 * The links a click pages through: the whole gallery when the link sits in one, otherwise
	 * just the link itself.
	 */
	function linkListOf(link) {
		var grid = link.closest(".gl-gallery-grid");
		var linkList = grid ? Array.prototype.slice.call(grid.querySelectorAll("a[data-lightbox]")) : [link];
		return linkList;
	}

	/**
	 * The two URLs one link answers. The overlay shows the medium, which is sized for a screen
	 * rather than for a print, and the full-size link under it points at the native file — the
	 * link's own href, which states no size.
	 */
	function entryOf(link) {
		var entry = {
			src: link.href + "?size=medium",
			hires: link.href,
			caption: link.getAttribute("data-caption") || ""
		};
		return entry;
	}

	document.addEventListener("click", function (event) {
		if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) { return; }

		var link = event.target.closest("a[data-lightbox]");
		if (!link) { return; }

		var linkList = linkListOf(link);
		event.preventDefault();
		open(linkList.map(entryOf), linkList.indexOf(link));
	});
})();
