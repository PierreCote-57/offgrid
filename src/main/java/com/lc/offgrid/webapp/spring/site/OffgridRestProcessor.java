package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.sky.SkyAnalyser;
import com.lc.offgrid.webapp.pojo.chat.ChatAnswer;
import com.lc.offgrid.webapp.pojo.chat.ChatMessage;
import com.lc.offgrid.webapp.pojo.chat.ChatRequest;
import com.lc.offgrid.webapp.pojo.sky.SkyInfoRestAnswer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

	public String getDataRootFolder()
	{
		return data_root_folder;
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
	 * The sky where the observer stands, at the second it asks for. The four parameters arrive
	 * as text so a value that does not parse falls back to the default rather than failing the
	 * request.
	 */
	public SkyInfoRestAnswer processSkyData(String timeZoneText, String latitudeText, String longitudeText,
			String epochSecondText)
	{
		ZonedDateTime	dateTime		= OffgridUtil.parseZonedDateTime(epochSecondText, timeZoneText);
		double			latitude		= OffgridUtil.parseLatitude(latitudeText);
		double			longitude		= OffgridUtil.parseLongitude(longitudeText);

		getLogger().debug("processSkyData(%s, %s, %s)", dateTime, latitude, longitude);

		SkyAnalyser			skyAnalyser	= new SkyAnalyser(getDataRootFolder(), dateTime, latitude, longitude);

		SkyInfoRestAnswer	skyAnswer	= new SkyInfoRestAnswer(dateTime, latitude, longitude);
		skyAnswer.setSunAngleMap(skyAnalyser.getSunAngleMap());
		skyAnswer.setSkyBodyDayMap(skyAnalyser.getSkyBodyDayMap());
		return skyAnswer;
	}
}
