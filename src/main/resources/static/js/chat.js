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

	document.addEventListener("alpine:init", function () {
		Alpine.data("chat", function () {
			return {
				messageList: [],
				draft: "",
				busy: false,
				error: "",

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
					var body = JSON.stringify({ messageList: this.messageList });

					fetch(CHAT_URL, {
						method: "POST",
						headers: { "Content-Type": "application/json" },
						body: body
					}).then(function (response) {
						if (!response.ok) {
							throw new Error("The server answered " + response.status + ".");
						}
						return response.json();
					}).then(function (answer) {
						component.messageList.push({
							role: "assistant",
							text: answer.text,
							timeText: "",
							durationText: answer.durationText
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
