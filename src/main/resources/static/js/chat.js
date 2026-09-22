/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * The chat page. The transcript is the whole state: every turn posts the entire messageList
 * and the server keeps nothing, so a reload starts a new conversation and nothing on the
 * server has to be cleaned up.
 *
 * An answer carries the server's own durationText, drawn under the bubble. That is what
 * separates a real round trip from a page that answered itself.
 *
 * Registered on alpine:init, which header.html has to list BEFORE alpine.min.js. Alpine
 * queues its own start() in a microtask as its script ends, and microtasks drain between
 * deferred scripts — so by the time a script listed after it runs, alpine:init has already
 * fired and a listener added then never hears it.
 */
(function () {
	"use strict";

	var CHAT_URL = "/rest/chat";

	// The wall clock on the visitor's line, local — the twin of the durationText the server
	// puts under its own answer. Lower case, because it sits in the same faded line.
	function nowText() {
		var text = new Date().toLocaleTimeString("en-US", {
			hour: "numeric", minute: "2-digit", hour12: true
		});
		return text.toLowerCase();
	}

	// The zone the browser is set to, which it states without asking the visitor for anything.
	function zoneName() {
		return Intl.DateTimeFormat().resolvedOptions().timeZone;
	}

	// What the bubble says when Claude answered with no text: the reason it stopped is the only
	// thing there is to show, and an empty bubble states nothing at all.
	function bubbleText(answer) {
		if (answer.text) {
			return answer.text;
		}
		var reason = answer.refusalText || answer.stopReason || "no reason stated";
		return "(no text — " + reason + ")";
	}

	// The call's cost in whole effective tokens. An answer that states none shows none, rather
	// than a NaN beside the duration.
	function tokenText(effectiveToken) {
		if ("number" !== typeof effectiveToken) {
			return "";
		}
		return Math.round(effectiveToken).toLocaleString();
	}

	// What the page posts: the transcript, and what the browser can say about where and when the
	// visitor is asking from. A refused position simply leaves the coordinates out.
	function requestBody(messageList, position) {
		var request = { messageList: messageList, timeZone: zoneName() };
		if (position) {
			request.latitudeDeg = position.coords.latitude;
			request.longitudeDeg = position.coords.longitude;
		}
		return JSON.stringify(request);
	}

	document.addEventListener("alpine:init", function () {
		Alpine.data("chat", function () {
			return {
				messageList: [],
				draft: "",
				busy: false,
				error: "",

				// Asked at load rather than at the first message, so the browser's prompt and the
				// wait for an answer are behind the visitor before they have typed anything.
				init: function () {
					window.OG.positionOf();
				},

				// The log shows the newest line, never the oldest: every push is followed by
				// this, after the tick that draws it. Reading scrollHeight before Alpine has
				// added the bubble scrolls to where the log ended one message ago.
				scrollToBottom: function () {
					var component = this;
					this.$nextTick(function () {
						var log = component.$refs.log;
						log.scrollTop = log.scrollHeight;
					});
				},

				// The visitor's line goes into the transcript first, so what is posted is
				// exactly what is on screen.
				send: function () {
					var text = this.draft.trim();
					if (!text || this.busy) {
						return;
					}

					this.messageList.push({ role: "user", text: text, timeText: nowText(), durationText: "" });
					this.draft = "";
					this.error = "";
					this.busy = true;
					this.scrollToBottom();

					var component = this;

					window.OG.positionOf().then(function (position) {
						return fetch(CHAT_URL, {
							method: "POST",
							headers: { "Content-Type": "application/json" },
							body: requestBody(component.messageList, position)
						});
					}).then(function (response) {
						if (!response.ok) {
							throw new Error("The server answered " + response.status + ".");
						}
						return response.json();
					}).then(function (answer) {
						component.messageList.push({
							role: "assistant",
							text: bubbleText(answer),
							timeText: "",
							durationText: answer.durationText,
							effectiveTokenText: tokenText(answer.effectiveToken),
							stopText: "end_turn" === answer.stopReason ? "" : answer.stopReason
						});
					}).catch(function (failure) {
						component.error = "No answer: " + failure.message;
						console.error("[chat] " + failure.message);
					}).finally(function () {
						component.busy = false;
						component.scrollToBottom();
					});
				}
			};
		});
	});
})();
