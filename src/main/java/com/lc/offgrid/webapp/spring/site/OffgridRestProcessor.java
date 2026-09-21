package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.claude.ClaudeManager;
import com.lc.offgrid.common.misc.sky.SkyBodyAnalyser;
import com.lc.offgrid.webapp.pojo.chat.ChatAnswer;
import com.lc.offgrid.webapp.pojo.chat.ChatMessage;
import com.lc.offgrid.webapp.pojo.chat.ChatRequest;
import com.lc.offgrid.webapp.pojo.sky.SkyObserverRestAnswer;
import com.lc.offgrid.webapp.pojo.sky.SkyPositionsRestAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * What the site's REST endpoints answer with. Peer of OffgridWebProcessor, which serves the
 * pages: this one never touches a Model and never names a view.
 */
@Component
@Scope("prototype")
public class OffgridRestProcessor
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(OffgridRestProcessor.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@Value("${folder.local}")
	// Initializer for tests. As WEB/bean, it gets from config
	private String data_root_folder = "/Users/pierrecote/Working/offgrid";

	// Initializer for tests. As WEB/bean, it gets from config
	@Value("${anthropic.claude.key}")
	private String claudeKey = "Not this";

	@Autowired
	private ClaudeManager claudeManager;

	public String getDataRootFolder()
	{
		return data_root_folder;
	}

	public ClaudeManager getClaudeManager()
	{
		return claudeManager;
	}

	/**
	 * The chat's reply. This pass repeats the last line of the transcript back, so what is
	 * being proven is the round trip rather than the answer.
	 */
	public ChatAnswer processChat(ChatRequest chatRequest)
	{
		List<ChatMessage>	messageList		= chatRequest.getMessageList();
		int					lastIndex		= messageList.size() - 1;
		ChatMessage			lastMessage		= messageList.get(lastIndex);
		String				text			= lastMessage.getText();

		getLogger().debug("Chat: %d message(s), echoing \"%s\"", messageList.size(), text);

		ChatAnswer			chatAnswer		= new ChatAnswer(text);
		return chatAnswer;
	}

	/**
	 * Where the bodies stand at the second it asks for, seen from above. It takes no observer and
	 * no zone, so the caller asks for it without waiting for the browser to say where it is. The
	 * parameter arrives as text so a value that does not parse falls back to the default rather
	 * than failing the request.
	 */
	public SkyPositionsRestAnswer processSkyPositions(String epochSecondText)
	{
		Instant			instant			= Instant.ofEpochSecond(OffgridUtil.parseEpochSecond(epochSecondText));

		getLogger().debug("processSkyPositions(%s)", instant);

		SkyBodyAnalyser skyBodyAnalyser = new SkyBodyAnalyser(instant);

		SkyPositionsRestAnswer	skyAnswer	= new SkyPositionsRestAnswer();
		skyAnswer.setAngleDegMap(skyBodyAnalyser.getAngleDegMap());
		return skyAnswer;
	}

	/**
	 * The sky where the observer stands, at the second it asks for. The four parameters arrive
	 * as text so a value that does not parse falls back to the default rather than failing the
	 * request.
	 */
	public SkyObserverRestAnswer processSkyObserver(String timeZoneText, String latitudeText, String longitudeText,
			String epochSecondText)
	{
		Instant			instant			= Instant.ofEpochSecond(OffgridUtil.parseEpochSecond(epochSecondText));
		ZoneId			zoneId			= OffgridUtil.parseTimeZone(timeZoneText);

		double			latitude		= OffgridUtil.parseLatitude(latitudeText, zoneId);
		double			longitude		= OffgridUtil.parseLongitude(longitudeText, zoneId);

		zoneId = null == zoneId ? ZoneId.of(OffgridUtil.getDefaultTimeZone()) : zoneId;
		ZonedDateTime	dateTime		= ZonedDateTime.ofInstant(instant, zoneId);

		getLogger().debug("processSkyObserver(%s, %s, %s)", dateTime, latitude, longitude);

		SkyBodyAnalyser skyBodyAnalyser = new SkyBodyAnalyser(instant, latitude, longitude);

		SkyObserverRestAnswer	skyAnswer	= new SkyObserverRestAnswer(dateTime, latitude, longitude);
		skyAnswer.setSkyBodyDayMap(skyBodyAnalyser.getSkyBodyDayMap(zoneId));
		return skyAnswer;
	}
}
