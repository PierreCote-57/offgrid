package com.lc.offgrid.common.misc.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;
import com.lc.basics.tools.logging.BasicLogger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

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

	public static final String	MODEL			= "claude-opus-5";

	// A ceiling on one answer, not a target: an answer that reaches it is cut off mid-sentence.
	public static final long	MAX_TOKENS		= 16000L;

	public static final String	SYSTEM_PROMPT	= "You answer visitors' questions on the Going "
			+ "offgrid site. Be brief, and say so when you do not know.";

	@Value("${anthropic.claude.key}")
	private String			claudeKey;

	private AnthropicClient	claudeClient;

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
	 * The answer to one question, or null when Claude declined to answer it.
	 */
	public String ask(String questionText)
	{
		MessageCreateParams	params		= MessageCreateParams.builder()
				.model(MODEL)
				.maxTokens(MAX_TOKENS)
				.system(SYSTEM_PROMPT)
				.addUserMessage(questionText)
				.build();

		Message				message		= getClaudeClient().messages().create(params);
		String				answerText	= readAnswerText(message);

		if (null == answerText)
		{
			getLogger().info("ask(%s) was declined, stop reason %s", questionText,
					message.stopReason().orElse(null));
		}
		return answerText;
	}

	/**
	 * The text of the first text block, or null when there is none: a decline arrives as a
	 * successful response carrying a stop reason and no text.
	 */
	private static String readAnswerText(Message message)
	{
		String	answerText	= null;

		for (ContentBlock contentBlock : message.content())
		{
			Optional<TextBlock>	textBlock	= contentBlock.text();

			if (textBlock.isPresent())
			{
				answerText = textBlock.get().text();
				break;
			}
		}
		return answerText;
	}
}
