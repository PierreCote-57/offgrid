/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The sky page in the browser. The table, the observer line under it and the chart are all
 * built here from what /rest/sky/data answers.
 *
 * Every page loads this file, so it finds its own blocks at load and asks the server for
 * nothing when there are none. A page may hold more than one chart — sky.html draws two —
 * which is why both are lists.
 */
(function () {
	"use strict";

	var TABLE_SELECTOR = ".og-sky-table";
	var CHART_SELECTOR = ".og-sky-chart";
	var DRAWING_SELECTOR = ".og-sky-drawing";
	var OBSERVER_SELECTOR = ".og-sky-observer";
	var DATEBOX_SELECTOR = ".og-sky-datebox";
	var DATE_BUTTON_SELECTOR = ".og-sky-date";
	var DATE_INPUT_SELECTOR = ".og-sky-date-input";
	var PLACE_SELECTOR = ".og-sky-place";
	var DASH_SELECTOR = ".og-sky-place-dash";
	var DATA_URL = "/rest/sky/data";

	// The ends the date picker may not go past, as a date input names a day.
	var FIRST_DAY_TEXT = "2020-01-01";
	var LAST_DAY_TEXT = "2029-12-31";

	// What a cell shows when the day does not hold the moment it asks for.
	var MISSING_TEXT = "-";

	// The year a period is stated in once it is longer than one.
	var DAYS_IN_YEAR = 365.25;

	// How long the browser is given to answer where the visitor is, and how old an answer it
	// may reuse. The same numbers the menu's sky link waits on.
	var POSITION_TIMEOUT_MS = 5000;
	var POSITION_MAX_AGE_MS = 600000;

	// What splits a heading's group from the heading itself: "Rise|Time" is the Time column of
	// the Rise group. A heading may hold anything but this, and only the first one splits.
	var HEADING_SEPARATOR = "|";

	/*
	 * The table's columns, one entry each: the heading, the width its th states, and the path to
	 * the value.
	 *
	 * A path whose first segment names a member of the answer is read from that member, keyed by
	 * the body — bodyMap.name is that body's entry in bodyMap. A first segment naming nothing up
	 * there is a moment of the body's own day, so RISE.bearing is where it came up. The last
	 * segment picks the format.
	 *
	 * Narrow and wide differ by these two lists and nothing else: a column is added, moved or
	 * dropped here alone.
	 */
	var NARROW_COLUMN_LIST = [
	    // 570 pixels available
		{ heading: "Body",                width: 70, value: "bodyMap.name" },
		{ heading: "Current|Bearing",     width: 70, value: "NOW.bearing" },
		{ heading: "Current|Elevation",   width: 70, value: "NOW.elevation" },
		{ heading: "Rise|Time",           width: 60, value: "RISE.epochSecond" },
//		{ heading: "Rise|Bearing",        width: 60, value: "RISE.bearing" },
		{ heading: "Transit|Time",        width: 60, value: "TRANSIT.epochSecond" },
//		{ heading: "Transit|Elevation",   width: 60, value: "TRANSIT.elevation" },
		{ heading: "Set|Time",            width: 60, value: "SET.epochSecond" },
//		{ heading: "Set|Bearing",         width: 60, value: "SET.bearing" },
		{ heading: "Sunlit",              width: 60, value: "skyBodyDayMap.litFraction" },
//		{ heading: "Period",              width: 50, value: "bodyMap.periodDay" }
	];

	var WIDE_COLUMN_LIST = [
	    // 1024 pixels available
		{ heading: "Body",                width: 70, value: "bodyMap.name" },
		{ heading: "Parent",              width: 55, value: "bodyMap.parent" },
		{ heading: "Current|Bearing",     width: 70, value: "NOW.bearing" },
		{ heading: "Current|Elevation",   width: 70, value: "NOW.elevation" },
		{ heading: "Rise|Time",           width: 60, value: "RISE.epochSecond" },
		{ heading: "Rise|Bearing",        width: 60, value: "RISE.bearing" },
		{ heading: "Transit|Time",        width: 60, value: "TRANSIT.epochSecond" },
		{ heading: "Transit|Elevation",   width: 60, value: "TRANSIT.elevation" },
		{ heading: "Set|Time",            width: 60, value: "SET.epochSecond" },
		{ heading: "Set|Bearing",         width: 60, value: "SET.bearing" },
		{ heading: "Orbit|Period",        width: 60, value: "bodyMap.periodDay" },
		{ heading: "Orbit|Radius",        width: 70, value: "skyBodyDayMap.orbitRadius" },
		{ heading: "Sunlit|Fraction",     width: 60, value: "skyBodyDayMap.litFraction" }
	];

	// How a value is written, by the last segment of the path that found it. A field with no
	// entry here is written as it stands.
	var FORMAT_BY_FIELD = {
		epochSecond: timeText,
		bearing: degreeText,
		elevation: degreeText,
		litFraction: percentText,
		periodDay: periodText,
		orbitRadius: distanceText
	};

	// A kind of body: dotRadius as a percent of the chart radius, scaled for the figure.
	var ROCK = { dotRadius: 3.0, colour: "#c8553d" };
	var GAS =  { dotRadius: 3.5, colour: "#c08a2e" };
	var ICE =  { dotRadius: 3.0, colour: "#2f7e76" };
	var STAR = { dotRadius: 5.0, colour: "#f5c400" };
	var MOON = { dotRadius: 1.5, colour: "#808080" };

	// The rest of the palette: the orbit line and the label text.
	var ORBIT_COLOUR = "#8c877d";
	var LABEL_COLOUR = "#243027";

	// orbitRadius: percent of the outermost orbit, scaled for the figure. Spacing is not to scale.
	var CHART_BODY_MAP = {
		SUN:     { orbitRadius:   0.0, type: STAR },
		MERCURY: { orbitRadius:  15,   type: ROCK },
		VENUS:   { orbitRadius:  25,   type: ROCK },
		EARTH:   { orbitRadius:  45,   type: ROCK },
		MOON:    { orbitRadius:  10,   type: MOON },
		MARS:    { orbitRadius:  60,   type: ROCK },
		JUPITER: { orbitRadius:  70,   type: GAS },
		SATURN:  { orbitRadius:  80,   type: GAS },
		URANUS:  { orbitRadius:  90,   type: ICE },
		NEPTUNE: { orbitRadius: 100.0, type: ICE }
	};

	// What the two lengths above are a percent of, and the space kept clear outside the outermost
	// orbit. The margin holds half a label, so it is a text measurement and does not scale.
	var ORBIT_BASE_PERCENT = 100;
	var DOT_BASE_PERCENT = 100;
	var CHART_MARGIN = 25;

	// The label's font, and how far its baseline sits above the dot it names. Both are text
	// measurements: the font stays 12px whatever the canvas.
	var LABEL_FONT_SIZE = 12;
	var LABEL_FONT_FAMILY = "-apple-system, BlinkMacSystemFont, \"Segoe UI\", sans-serif";
	var LABEL_GAP = 5;

	// The caption's size, and how far its baseline sits above the bottom edge: the tail of a "g"
	// lands on the edge itself.
	var CAPTION_FONT_SIZE = 13;
	var CAPTION_DESCENT = 3;

	// What a reader who cannot see the chart is told it is.
	var CHART_LABEL = "The Sun, the eight planets and the Moon, each on its orbit, seen from above";

	// The namespace an SVG element is created in. createElement() makes an HTML element of the
	// same name, which the browser lays out but never draws.
	var SVG_NAMESPACE = "http://www.w3.org/2000/svg";

	// Where the visitor stands, asked of the browser once and reused for the rest of the page
	// view: every block that asks again is another callback and another wait, and a visitor does
	// not move between two clicks on the same page.
	var positionPromise = null;

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

	// ---- the table ------------------------------------------------------------

	// An epoch second on the clock of the zone the answer states, which is the zone its times
	// are in. The browser's own zone never enters into it.
	function timeText(value, timeZone) {
		var text = new Date(value * 1000).toLocaleTimeString("en-GB", {
			timeZone: timeZone, hour: "2-digit", minute: "2-digit", hour12: false
		});
		return text;
	}

	// Whole degrees: the table is read at a glance, and the ephemeris is sampled every two
	// minutes, so a decimal would state a precision the value does not have.
	function degreeText(value, timeZone) {
		var text = Math.round(value) + "°";
		return text;
	}

	// Days while the number is small, years once it is not: 27.3 d, 88 d, 165 y. One decimal
	// below a hundred and none above, so no cell states a precision the reader has no use for.
	function periodText(value, timeZone) {
		var isYear = value >= DAYS_IN_YEAR;
		var amount = isYear ? value / DAYS_IN_YEAR : value;
		var numberText = (amount < 100) ? amount.toFixed(1).replace(/\.0$/, "") : String(Math.round(amount));
		var text = numberText + (isYear ? " y" : " d");
		return text;
	}

	// Astronomical units, two decimals: the Moon sits at 0.99 of one and Neptune at 30.07.
	function distanceText(value, timeZone) {
		var text = value.toFixed(2) + " AU";
		return text;
	}

	// A fraction of the whole, as a reader says it: 0.8734 is 87%.
	function percentText(value, timeZone) {
		var text = Math.round(value * 100) + "%";
		return text;
	}

	/*
	 * The value one path names for one body.
	 *
	 * A null is a moment that does not happen on the day — the Moon that never rises — and an
	 * undefined is a path that names nothing at all. The two are told apart so a typo says so
	 * while an empty cell stays quiet.
	 */
	function valueAt(path, bodyId, skyData) {
		var partList = path.split(".");
		var head = partList[0];
		var value;

		if (head in skyData) {
			value = skyData[head][bodyId];
		}
		else {
			var bodyDay = skyData.skyBodyDayMap[bodyId];
			value = (bodyDay === undefined) ? undefined : bodyDay.momentMap[head];
		}

		for (var index = 1; index < partList.length; index++) {
			if (value === null || value === undefined) { break; }
			value = value[partList[index]];
		}

		return value;
	}

	// The text of one cell: the value the path names, written the way its last segment says.
	function cellText(path, bodyId, skyData) {
		var value = valueAt(path, bodyId, skyData);
		if (value === undefined) {
			logError("cellText('" + path + "', '" + bodyId + "') names nothing in the answer.");
			return MISSING_TEXT;
		}
		if (value === null) {
			return MISSING_TEXT;
		}

		var partList = path.split(".");
		var field = partList[partList.length - 1];
		var format = FORMAT_BY_FIELD[field];
		var text = format ? format(value, skyData.timeZone) : String(value);
		return text;
	}

	// Which columns this block asks for. The page states it, on the block itself.
	function columnListFor(tableElement) {
		var columnList = (tableElement.dataset.wide === "true") ? WIDE_COLUMN_LIST : NARROW_COLUMN_LIST;
		return columnList;
	}

	// The group a column belongs to, or "" for one that stands on its own.
	function groupOf(column) {
		var index = column.heading.indexOf(HEADING_SEPARATOR);
		var group = (index < 0) ? "" : column.heading.slice(0, index);
		return group;
	}

	// The column's own heading, which is all of it when there is no group.
	function headingOf(column) {
		var index = column.heading.indexOf(HEADING_SEPARATOR);
		var heading = (index < 0) ? column.heading : column.heading.slice(index + 1);
		return heading;
	}

	/*
	 * The widths, one element each. Under table-layout: fixed the columns are taken from the
	 * first row, and that row carries the groups' spans — a col is what still makes a column
	 * exactly the width its entry asks for.
	 */
	function buildColumnGroup(columnList) {
		var columnGroup = document.createElement("colgroup");

		columnList.forEach(function (column) {
			var columnElement = document.createElement("col");
			columnElement.style.width = column.width + "px";
			columnGroup.appendChild(columnElement);
		});

		return columnGroup;
	}

	/*
	 * Two rows: the groups, and the columns under them. Columns naming the same group in a row
	 * share one cell above them, so "Bearing" says which bearing it is; a column with no group
	 * takes one cell down both rows.
	 */
	function buildHead(columnList) {
		var head = document.createElement("thead");
		var groupRow = document.createElement("tr");
		var columnRow = document.createElement("tr");

		for (var index = 0; index < columnList.length; index++) {
			var group = groupOf(columnList[index]);

			if (group === "") {
				var aloneCell = document.createElement("th");
				aloneCell.rowSpan = 2;
				aloneCell.textContent = headingOf(columnList[index]);
				groupRow.appendChild(aloneCell);
				continue;
			}

			var span = 1;
			while (index + span < columnList.length && groupOf(columnList[index + span]) === group) {
				span++;
			}

			var groupCell = document.createElement("th");
			groupCell.colSpan = span;
			groupCell.textContent = group;
			groupRow.appendChild(groupCell);

			for (var inner = 0; inner < span; inner++) {
				var cell = document.createElement("th");
				cell.textContent = headingOf(columnList[index + inner]);
				columnRow.appendChild(cell);
			}

			index += span - 1;
		}

		head.appendChild(groupRow);
		if (columnRow.childElementCount > 0) {
			head.appendChild(columnRow);
		}
		return head;
	}

	// One row per body, in the order the answer lists them, and every body gets one: a body that
	// does not rise today is still a body the reader is looking for.
	function buildBody(columnList, skyData) {
		var body = document.createElement("tbody");

		Object.keys(skyData.skyBodyDayMap).forEach(function (bodyId) {
			var row = buildRow(columnList, bodyId, skyData);
			body.appendChild(row);
		});

		return body;
	}

	function buildRow(columnList, bodyId, skyData) {
		var row = document.createElement("tr");

		columnList.forEach(function (column) {
			var cell = document.createElement("td");
			cell.textContent = cellText(column.value, bodyId, skyData);
			row.appendChild(cell);
		});

		return row;
	}

	// What the table needs, which is what its columns ask for and nothing else.
	function tableWidth(columnList) {
		var width = 0;

		columnList.forEach(function (column) {
			width += column.width;
		});

		return width;
	}

	// Fill one block's table. The fragment leaves it empty and keeps the observer line beside
	// it, so this replaces the table's own children and touches nothing else in the block.
	function renderTable(tableElement, skyData) {
		var table = tableElement.querySelector("table");
		if (table === null) {
			logError("renderTable() found no table element in the block.");
			return;
		}

		var columnList = columnListFor(tableElement);
		table.style.width = tableWidth(columnList) + "px";
		table.replaceChildren(buildColumnGroup(columnList), buildHead(columnList),
				buildBody(columnList, skyData));
	}

	// ---- the observer ---------------------------------------------------------

	function coordinateText(latitude, longitude) {
		var latitudeText = Math.abs(latitude).toFixed(4) + "°" + (latitude >= 0 ? "N" : "S");
		var longitudeText = Math.abs(longitude).toFixed(4) + "°" + (longitude >= 0 ? "E" : "W");
		return latitudeText + ", " + longitudeText;
	}

	// The zone by name and the offset it was on at that moment — an offset belongs to a moment,
	// since a zone changes its own twice a year.
	function zoneText(timeZone, epochSecond) {
		var partList = new Intl.DateTimeFormat("en-GB", {
			timeZone: timeZone, timeZoneName: "longOffset"
		}).formatToParts(new Date(epochSecond * 1000));

		var offsetPart = partList.find(function (part) { return part.type === "timeZoneName"; });
		var offsetText = offsetPart ? offsetPart.value : "";
		return timeZone + " (" + offsetText + ")";
	}

	/*
	 * The line under the table: where the answer was read as, and the clock its times are on.
	 * The name is not in it — the server has coordinates and not a name — so it renders with the
	 * numbers and gains its name a moment later, if Google answers.
	 */
	function renderObserver(tableElement, skyData) {
		var observerElement = tableElement.querySelector(OBSERVER_SELECTOR);
		if (observerElement === null) {
			logError("renderObserver() found no " + OBSERVER_SELECTOR + " in the block.");
			return;
		}

		var placeElement = document.createElement("strong");
		placeElement.className = "og-sky-place";

		var dashElement = document.createElement("span");
		dashElement.className = "og-sky-place-dash";
		dashElement.textContent = " \u2014 ";
		dashElement.hidden = true;

		var coordinateElement = document.createElement("span");
		coordinateElement.textContent = coordinateText(skyData.latitude, skyData.longitude);

		var placeLine = document.createElement("p");
		placeLine.append(placeElement, dashElement, coordinateElement);

		var zoneLine = document.createElement("p");
		zoneLine.textContent = zoneText(skyData.timeZone, skyData.epochSecond);

		observerElement.replaceChildren(placeLine, zoneLine);
	}

	// ---- the place name -------------------------------------------------------

	// The town, then the province's short form: "Campbell River", "BC".
	function placeName(result) {
		var componentList = result.address_components || [];
		var town = null;
		var region = null;
		componentList.forEach(function (component) {
			if (component.types.indexOf("locality") >= 0) { town = component.long_name; }
			if (component.types.indexOf("administrative_area_level_1") >= 0) { region = component.short_name; }
		});
		if (!town) { return null; }
		return region ? town + ", " + region : town;
	}

	/*
	 * Google answers a point with a stack of results, finest first: the street, then the
	 * neighbourhood, the town, the district, the province. Only some carry a locality, so this
	 * takes the first one that does.
	 */
	function firstNamed(resultList) {
		for (var index = 0; index < resultList.length; index++)
		{
			var name = placeName(resultList[index]);
			if (name) {
				log("Result " + index + " of " + resultList.length + " gave \"" + name + "\".");
				return name;
			}
		}
		log("No result carried a locality; the types were "
				+ resultList.map(function (result) { return result.types.join("/"); }).join(", ")
				+ ".");
		return null;
	}

	/*
	 * Ask Google what is at the answer's coordinates and write it into the observer line. No
	 * answer means no name, and the numbers stand on their own.
	 *
	 * It goes through the Maps JavaScript API rather than the geocoding web service: the site's
	 * key is restricted by referrer, and the web service refuses such a key outright.
	 */
	function lookUpPlace(tableElement, skyData) {
		var placeElement = tableElement.querySelector(PLACE_SELECTOR);
		var dashElement = tableElement.querySelector(DASH_SELECTOR);
		if (placeElement === null) {
			logError("lookUpPlace() found no " + PLACE_SELECTOR + " to fill in.");
			return;
		}
		if (!window.OG || !window.OG.loadGoogleMapsApi) {
			logError("OG.loadGoogleMapsApi is not there; the name cannot be looked up.");
			return;
		}

		var latitude = skyData.latitude;
		var longitude = skyData.longitude;
		var startedAt = Date.now();
		log("Loading the Maps JavaScript API to reverse geocode " + latitude + "," + longitude + ".");

		window.OG.loadGoogleMapsApi(function () {
			log("Maps API ready after " + (Date.now() - startedAt) + "ms; asking the geocoder.");

			var geocoder = new google.maps.Geocoder();
			var request = { location: { lat: latitude, lng: longitude } };

			geocoder.geocode(request)
				.then(function (answer) {
					var resultList = answer.results || [];
					log("Geocoder answered " + resultList.length + " result(s) after "
							+ (Date.now() - startedAt) + "ms.");

					var name = firstNamed(resultList);
					if (!name) {
						log("No name applied; the coordinates stand on their own.");
						return;
					}

					placeElement.textContent = name;
					if (dashElement !== null) {
						dashElement.hidden = false;
					}
					log("Wrote \"" + name + "\" into the observer line.");
				})
				.catch(function (failure) {
					logError("The geocoder failed after " + (Date.now() - startedAt) + "ms: "
							+ failure.message);
				});
		});
	}

	// ---- the chart ------------------------------------------------------------

	// One SVG element, with the attributes it is drawn by.
	function makeSvgElement(name, attributeMap) {
		var element = document.createElementNS(SVG_NAMESPACE, name);

		Object.keys(attributeMap).forEach(function (attributeName) {
			element.setAttribute(attributeName, attributeMap[attributeName]);
		});

		return element;
	}

	// The width this block draws at. The page states it, on the block itself.
	function chartWidthOf(chartElement) {
		var width = Number(chartElement.dataset.width);
		return width;
	}

	/*
	 * Where every body lands on a canvas of one width, keyed by body: the dot, the radius of the
	 * circle it travels on, and the size and colour of the dot itself.
	 *
	 * The answer names the bodies in the order the server states them, parents before children,
	 * which is what lets a child's circle be centred on a parent already placed. A body whose
	 * parent is not there is drawn around the middle, where the Sun sits.
	 */
	function placeBodyMap(width, skyData) {
		var widthOrbit = width - 2 * CHART_MARGIN;
		var centre = width / 2;
		var placeMap = {};

		Object.keys(skyData.bodyMap).forEach(function (bodyId) {
			var chartBody = CHART_BODY_MAP[bodyId];
			if (chartBody === undefined) {
				logError("placeBodyMap() has no chart entry for '" + bodyId + "'; it is not drawn.");
				return;
			}

			var parentId = skyData.bodyMap[bodyId].parent;
			var parentPlace = placeMap[parentId];
			var aroundX = (parentPlace === undefined) ? centre : parentPlace.dotX;
			var aroundY = (parentPlace === undefined) ? centre : parentPlace.dotY;

			var orbitRadius = (chartBody.orbitRadius / ORBIT_BASE_PERCENT) * (widthOrbit / 2);
			var dotRadius = (chartBody.type.dotRadius / DOT_BASE_PERCENT) * (widthOrbit / 2);
			var radians = skyData.sunAngleMap[bodyId] * Math.PI / 180;
			var dotX = aroundX + orbitRadius * Math.cos(radians);
			var dotY = aroundY - orbitRadius * Math.sin(radians);

			placeMap[bodyId] = {
				dotX: dotX,
				dotY: dotY,
				orbitRadius: orbitRadius,
				dotRadius: dotRadius,
				colour: chartBody.type.colour
			};
		});

		return placeMap;
	}

	/*
	 * The circle a body travels on, centred on what it goes around. A body with nothing to go
	 * around is the middle of the chart and travels on no circle, so it draws none.
	 */
	function drawOrbitList(svgElement, placeMap, skyData) {
		Object.keys(placeMap).forEach(function (bodyId) {
			var parentId = skyData.bodyMap[bodyId].parent;
			var parentPlace = placeMap[parentId];
			if (parentPlace === undefined) {
				return;
			}

			var place = placeMap[bodyId];
			var orbitElement = makeSvgElement("circle", {
				cx: parentPlace.dotX,
				cy: parentPlace.dotY,
				r: place.orbitRadius,
				fill: "none",
				stroke: ORBIT_COLOUR,
				"stroke-width": 0.5
			});
			svgElement.appendChild(orbitElement);
		});
	}

	// The body itself, at the place it was put.
	function drawDotList(svgElement, placeMap) {
		Object.keys(placeMap).forEach(function (bodyId) {
			var place = placeMap[bodyId];
			var dotElement = makeSvgElement("circle", {
				cx: place.dotX,
				cy: place.dotY,
				r: place.dotRadius,
				fill: place.colour
			});
			svgElement.appendChild(dotElement);
		});
	}

	/*
	 * Every body's name, centred above its dot. Two bodies standing together take two labels that
	 * overlap, which is the price of a label always being in the one place the reader looks.
	 */
	function drawLabelList(svgElement, placeMap, skyData) {
		Object.keys(placeMap).forEach(function (bodyId) {
			var place = placeMap[bodyId];
			var baselineY = place.dotY - (place.dotRadius + LABEL_GAP);

			var labelElement = makeSvgElement("text", {
				x: place.dotX,
				y: baselineY,
				"text-anchor": "middle",
				"font-size": LABEL_FONT_SIZE,
				"font-family": LABEL_FONT_FAMILY,
				fill: LABEL_COLOUR
			});
			labelElement.textContent = skyData.bodyMap[bodyId].name;
			svgElement.appendChild(labelElement);
		});
	}

	/*
	 * The caption, centred on the drawing's bottom edge: it sits in the band the margin keeps
	 * clear outside the outermost orbit, as low in it as a descender allows.
	 */
	function drawCaption(svgElement, width, captionText) {
		var baselineY = width - CAPTION_DESCENT;

		var captionElement = makeSvgElement("text", {
			x: width / 2,
			y: baselineY,
			"text-anchor": "middle",
			"font-size": CAPTION_FONT_SIZE,
			"font-family": LABEL_FONT_FAMILY,
			fill: LABEL_COLOUR
		});
		captionElement.textContent = captionText;
		svgElement.appendChild(captionElement);
	}

	/*
	 * Draw one block's chart. The drawing is square and exactly the width the block states, so
	 * the page places a chart at any size and states nothing else.
	 */
	function renderChart(chartElement, skyData) {
		var drawingElement = chartElement.querySelector(DRAWING_SELECTOR);
		if (drawingElement === null) {
			logError("renderChart() found no " + DRAWING_SELECTOR + " in the block.");
			return;
		}

		var width = chartWidthOf(chartElement);
		if (!width) {
			logError("renderChart() found no width on the block; the page states it as data-width.");
			return;
		}

		var svgElement = makeSvgElement("svg", {
			viewBox: "0 0 " + width + " " + width,
			width: width,
			height: width,
			role: "img",
			"aria-label": CHART_LABEL
		});
		// Inline by default, which leaves the caption a baseline's worth of space below the chart.
		svgElement.style.display = "block";

		var placeMap = placeBodyMap(width, skyData);
		drawOrbitList(svgElement, placeMap, skyData);
		drawDotList(svgElement, placeMap);
		drawLabelList(svgElement, placeMap, skyData);
		drawCaption(svgElement, width, chartElement.dataset.caption);

		drawingElement.replaceChildren(svgElement);
	}

	/*
	 * Where the visitor stands, as a promise that is made once. Refused, timed out, unavailable
	 * or simply not offered by the browser, it answers null, and a call carrying no coordinates
	 * is answered for the server's marker — the same thing the menu's sky link settles for.
	 */
	function positionOf() {
		if (positionPromise) {
			return positionPromise;
		}

		positionPromise = new Promise(function (resolve) {
			if (!navigator.geolocation) {
				log("This browser states no position; the server's marker will answer.");
				resolve(null);
				return;
			}

			var startedAt = Date.now();
			navigator.geolocation.getCurrentPosition(
				function (position) {
					log("Position after " + (Date.now() - startedAt) + "ms.");
					resolve(position);
				},
				function (failure) {
					log("No position after " + (Date.now() - startedAt) + "ms: " + failure.message
							+ "; the server's marker will answer.");
					resolve(null);
				},
				{ timeout: POSITION_TIMEOUT_MS, maximumAge: POSITION_MAX_AGE_MS });
		});

		return positionPromise;
	}

	/*
	 * The call the endpoint reads. A parameter it is not given falls back on the server, which is
	 * what a refused position comes to.
	 */
	function makeDataUrl(dateTime, position) {
		var timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
		var epochSecond = Math.floor(dateTime.getTime() / 1000);

		var url = DATA_URL + "?timezone=" + encodeURIComponent(timeZone) + "&epochSecond=" + epochSecond;
		if (position) {
			url = url + "&lat=" + position.coords.latitude + "&lng=" + position.coords.longitude;
		}

		return url;
	}

	/*
	 * Ask for the sky at one moment and fill the blocks inside one element with the answer. The
	 * scope is what a date control states it drives; given none, the whole page answers, which is
	 * what the first load wants.
	 *
	 * It is on window.OG because the page will ask again — another date, another place — and
	 * the caller then is not this file.
	 */
	function loadSkyData(dateTime, scopeElement) {
		var scope = scopeElement || document;

        log("Getting position");
		positionOf().then(function (position) {
			requestSkyData(dateTime, position, scope);
		});
	}

	// Ask, and fill every block inside the scope with what comes back.
	function requestSkyData(dateTime, position, scope) {
		var tableList = scope.querySelectorAll(TABLE_SELECTOR);
		var chartList = scope.querySelectorAll(CHART_SELECTOR);
		var pickerList = scope.querySelectorAll(DATEBOX_SELECTOR);
		var url = makeDataUrl(dateTime, position);
		var startedAt = Date.now();
		log("Asking " + url + " for the sky.");

		fetch(url)
			.then(function (response) {
				if (!response.ok) {
					throw new Error("failed to load " + url + " (HTTP " + response.status + ")");
				}
			log("Sky data available");
			return response.json();
			})
			.then(function (skyData) {
				log("Answered " + skyData.dateTimeText + " after " + (Date.now() - startedAt) + "ms.");

				tableList.forEach(function (tableElement) {
					renderTable(tableElement, skyData);
					renderObserver(tableElement, skyData);
					lookUpPlace(tableElement, skyData);
				});

				chartList.forEach(function (chartElement) {
					renderChart(chartElement, skyData);
				});

				pickerList.forEach(function (boxElement) {
					renderDatePicker(boxElement, skyData);
				});
			})
			.catch(function (failure) {
				logError("requestSkyData(" + url + ") failed after " + (Date.now() - startedAt)
						+ "ms: " + failure.message);
			});
	}

	/*
	 * The day the input names, as a moment on it. Noon, because a date carries no time of its
	 * own and midnight is one offset away from the day before it.
	 */
	function noonOf(dayText) {
		var partList = dayText.split("-");
		var year = Number(partList[0]);
		var month = Number(partList[1]);
		var day = Number(partList[2]);
		var dateTime = new Date(year, month - 1, day, 12, 0, 0);
		return dateTime;
	}

	/*
	 * The day the answer is for, written the way a date input names a day: yyyy-MM-dd, on the
	 * clock of the zone the answer states. Built from the parts rather than from a locale's own
	 * order, which is not the input's.
	 */
	function dayTextOf(epochSecond, timeZone) {
		var partList = new Intl.DateTimeFormat("en-GB", {
			timeZone: timeZone, year: "numeric", month: "2-digit", day: "2-digit"
		}).formatToParts(new Date(epochSecond * 1000));

		var partMap = {};
		partList.forEach(function (part) {
			partMap[part.type] = part.value;
		});

		var text = partMap.year + "-" + partMap.month + "-" + partMap.day;
		return text;
	}

	// What the button reads: "Mon 2026-09-07", the weekday on the same clock as the day beside it.
	function buttonText(epochSecond, timeZone, dayText) {
		var weekday = new Date(epochSecond * 1000).toLocaleDateString("en-GB", {
			timeZone: timeZone, weekday: "short"
		});
		var text = weekday + " " + dayText;
		return text;
	}

	/*
	 * Fill one date control with the day the answer is for, and the ends the calendar may not go
	 * past. The button carries the words and the input holds the value the browser's calendar
	 * opens on, so both are written. The button is blank until this runs: the page is served with
	 * no date on it.
	 */
	function renderDatePicker(boxElement, skyData) {
		var buttonElement = boxElement.querySelector(DATE_BUTTON_SELECTOR);
		var inputElement = boxElement.querySelector(DATE_INPUT_SELECTOR);
		if (buttonElement === null || inputElement === null) {
			logError("renderDatePicker() found no " + DATE_BUTTON_SELECTOR + " or "
					+ DATE_INPUT_SELECTOR + " in the box.");
			return;
		}

		var dayText = dayTextOf(skyData.epochSecond, skyData.timeZone);
		inputElement.min = FIRST_DAY_TEXT;
		inputElement.max = LAST_DAY_TEXT;
		inputElement.value = dayText;
		buttonElement.textContent = buttonText(skyData.epochSecond, skyData.timeZone, dayText);
	}

	// The element a date control drives: the id its box states, or the whole page when it states
	// none. An id naming nothing is reported, and the page answers instead of nothing happening.
	function scopeOf(box) {
		var scopeId = box.dataset.scope;
		if (!scopeId) {
			return document;
		}

		var scopeElement = document.getElementById(scopeId);
		if (!scopeElement) {
			logError("scopeOf(" + scopeId + ") names no element on this page; the whole page answers.");
			return document;
		}

		return scopeElement;
	}

	/*
	 * The date control's click, which the button carries in its markup. The calendar belongs to
	 * the browser and is anchored to the input beside the button, which is the only reason that
	 * input is on the page at all.
	 *
	 * Focus first, then open. Safari opens the calendar either way but only closes it — on
	 * Escape, on a click outside — when focus is in the input.
	 *
	 * Picking a day fires change, and the blocks the box drives ask for that day where they
	 * stand. The button's text comes back with the answer, like every other part of a block, so
	 * nothing is written here. Assigning onchange rather than adding a listener is what makes a
	 * second click harmless.
	 */
	function openDatePicker(button) {
		var box = button.closest(DATEBOX_SELECTOR);
		var input = box.querySelector(DATE_INPUT_SELECTOR);
		var scope = scopeOf(box);

		input.onchange = function () {
			var dayText = input.value;
			var dateTime = noonOf(dayText);
			loadSkyData(dateTime, scope);
		};

		input.focus();
		if (typeof input.showPicker === "function") {
			input.showPicker();
			return;
		}

		input.click();
	}

	// A page carrying no block of ours asks the server for nothing.
	function onPageLoad() {
		var tableList = document.querySelectorAll(TABLE_SELECTOR);
		var chartList = document.querySelectorAll(CHART_SELECTOR);

		if (tableList.length === 0 && chartList.length === 0) { return; }

		loadSkyData(new Date());
	}

	// Created defensively: this file may execute before OR after the others that add to it.
	window.OG = window.OG || {};
	window.OG.loadSkyData = loadSkyData;
	window.OG.openDatePicker = openDatePicker;

	document.addEventListener("DOMContentLoaded", onPageLoad);
})();
