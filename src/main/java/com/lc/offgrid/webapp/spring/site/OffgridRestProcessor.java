package com.lc.offgrid.webapp.spring.site;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.webapp.pojo.chat.ChatAnswer;
import com.lc.offgrid.webapp.pojo.chat.ChatMessage;
import com.lc.offgrid.webapp.pojo.chat.ChatRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
}
