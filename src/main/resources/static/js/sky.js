/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The sky page in the browser. The chart and the table still arrive drawn from the server;
 * this is where drawing them here, from what /rest/sky/data answers, starts.
 *
 * Every page loads this file, so it finds its own blocks at load and asks the server for
 * nothing when there are none. A page may hold more than one chart — sky.html draws two —
 * which is why both are lists.
 */
(function () {
	"use strict";

	var TABLE_SELECTOR = ".og-sky-table";
	var CHART_SELECTOR = ".og-sky-chart";
	var DATA_URL = "/rest/sky/data";

	// The page's own blocks, found once at load and read by every function after: nothing here
	// takes them as a parameter, and nothing outside this file sees them.
	var tableList = null;
	var chartList = null;

	// One place builds the stamp, so no call site carries a time of its own.
	function stamp() {
		var text = new Date().toLocaleTimeString("en-GB", {
			hour: "2-digit", minute: "2-digit", second: "2-digit",
			fractionalSecondDigits: 3, hour12: false
		});
		return text + " [sky] ";
	}
	function log(message) {
		console.log(stamp() + message);
	}
	function logError(message) {
		console.error(stamp() + message);
	}

	/*
	 * Ask the server for the sky, and draw it when the answer comes. The call carries no
	 * parameters yet, so what comes back is the default observer at the current moment.
	 *
	 * It is on window.OG because the page will ask again — another date, another place — and
	 * the caller then is not this file.
	 */
	function loadSkyData() {
		var startedAt = Date.now();
		log("Asking " + DATA_URL + " for the sky.");

		fetch(DATA_URL)
			.then(function (response) {
				if (!response.ok) {
					throw new Error("failed to load " + DATA_URL + " (HTTP " + response.status + ")");
				}
				return response.json();
			})
			.then(function (skyData) {
				log("Answered " + skyData.dateTimeText + " after " + (Date.now() - startedAt) + "ms.");

				tableList.forEach(function (tableElement) {
//					alert("Table " + tableElement.className);
				});

				chartList.forEach(function (chartElement) {
//					alert("Chart " + chartElement.className);
				});
			})
			.catch(function (failure) {
				logError("loadSkyData() failed after " + (Date.now() - startedAt) + "ms: "
						+ failure.message);
			});
	}

	// The blocks are found here and nowhere else. A page carrying neither asks for nothing.
	function onPageLoad() {
		tableList = document.querySelectorAll(TABLE_SELECTOR);
		chartList = document.querySelectorAll(CHART_SELECTOR);

		if (tableList.length === 0 && chartList.length === 0) { return; }

		loadSkyData();
	}

	// Created defensively: this file may execute before OR after the others that add to it.
	window.OG = window.OG || {};
	window.OG.loadSkyData = loadSkyData;

	document.addEventListener("DOMContentLoaded", onPageLoad);
})();
