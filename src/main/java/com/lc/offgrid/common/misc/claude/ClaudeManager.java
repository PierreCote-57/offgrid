package com.lc.offgrid.common.misc.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.services.blocking.MessageService;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.pojo.claude.ChatMessage;
import com.lc.offgrid.common.pojo.claude.ChatRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Questions put to Claude over the Anthropic API, and the answers that come back. One call is
 * one question: nothing is kept between them, so everything the answer depends on travels on
 * the call that asks.
 */
@Component
public class ClaudeManager implements InitializingBean
{
	private static final BasicLogger LOGGER		= BasicLogger.getLogger(ClaudeManager.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	// A ceiling on one answer, not a target: an answer that reaches it is cut off mid-sentence.
	public static final long	MAX_TOKENS		= 16000L;

	public static final String	SYSTEM_PROMPT	= "You answer visitors' questions on the Going "
			+ "offgrid site. Be brief, and say so when you do not know.";

	// Which model every call goes to. One place decides it, so no caller has to name one.
	private ClaudeModel		model		= ClaudeModel.OPUS;

	@Value("${anthropic.claude.key}")
	private String			claudeKey;

	private AnthropicClient	claudeClient;

	public ClaudeModel getModel()
	{
		return model;
	}
	public void setModel(ClaudeModel model)
	{
		this.model = model;
	}

	public String getClaudeKey()
	{
		return claudeKey;
	}

	public AnthropicClient getClaudeClient()
	{
		return claudeClient;
	}

	/**
	 * Builds the client once, at startup, so a request never pays for it and a missing key is a
	 * startup failure rather than a failed answer.
	 */
	@Override
	public void afterPropertiesSet() throws Exception
	{
		claudeClient = AnthropicOkHttpClient.builder()
				.apiKey(getClaudeKey())
				.build();
	}

	/**
	 * The answer to one chat request: the last line of the transcript is the question, and the
	 * parser carries what came back — a stop reason and no text when Claude declined.
	 */
	public MessageParser chat(ChatRequest chatRequest)
	{
		List<ChatMessage>	messageList		= chatRequest.getMessageList();
		int					lastIndex		= messageList.size() - 1;
		ChatMessage			lastMessage		= messageList.get(lastIndex);
		String				questionText	= lastMessage.getText();

		MessageCreateParams.Builder builder = MessageCreateParams.builder();
		builder.model(getModel().getModelId());
		builder.maxTokens(MAX_TOKENS);
		builder.system(SYSTEM_PROMPT);
		builder.addUserMessage(questionText);
		MessageCreateParams params = builder.build();

		MessageService messageService = getClaudeClient().messages();
		Message message = messageService.create(params);
		MessageParser parser = new MessageParser(getModel(), message);

		getLogger().info("chat(%s) answered %s on %s", questionText, parser.getTextList(),
				getModel());

		if (!parser.isEndTurn())
		{
			getLogger().info("chat(%s) was declined on %s, stop reason %s", questionText,
					getModel(), parser.getStopReason());
		}
		return parser;
	}
}
