package com.lc.offgrid.common.misc.claude;

import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.RefusalStopDetails;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.Usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * What one call to Claude asked and what came back. A call that runs tools takes several rounds,
 * one Message each, oldest first; the last one is the answer.
 */
public class ClaudeAnswer
{
	private MessageCreateParams	params;
	private List<Message>		messageList;

	public ClaudeAnswer(MessageCreateParams params, List<Message> messageList)
	{
		this.params			= params;
		this.messageList	= messageList;
	}

	public MessageCreateParams getParams()
	{
		return params;
	}
	public List<Message> getMessageList()
	{
		return messageList;
	}
	public Message getLastMessage()
	{
		int	lastIndex	= getMessageList().size() - 1;
		return getMessageList().get(lastIndex);
	}

	// The model the call named, which is the one whose prices this answer is counted against.
	public ClaudeModel getModel()
	{
		String	modelId	= getParams().model().asString();
		return ClaudeModel.of(modelId);
	}

	// The last message of the question, which the caller sends as the visitor's own words. Empty
	// when that message is blocks rather than a plain string.
	public String getQuestionText()
	{
		List<MessageParam>	messageParamList	= getParams().messages();
		int					lastIndex			= messageParamList.size() - 1;
		MessageParam		lastMessageParam	= messageParamList.get(lastIndex);
		String				questionText		= lastMessageParam.content().string().orElse("");
		return questionText;
	}

	public String getId()
	{
		return getLastMessage().id();
	}

	// The model that answered, which is not always the one asked for: a refusal fallback answers
	// on a different model.
	public String getModelName()
	{
		return getLastMessage().model().asString();
	}

	public List<Usage> getUsageList()
	{
		List<Usage>	usageList	= new ArrayList<>();

		for (Message message : getMessageList())
		{
			usageList.add(message.usage());
		}
		return usageList;
	}

	// Every round priced in input tokens, by the ratios of the model that was asked.
	public double getEffectiveToken()
	{
		ClaudeModel	model			= getModel();
		double		effectiveToken	= 0.0;

		for (Usage usage : getUsageList())
		{
			effectiveToken += model.getEffectiveToken(usage);
		}
		return effectiveToken;
	}

	public List<String> getTextList()
	{
		List<String>	answerTextList	= new ArrayList<>();

		for (ContentBlock contentBlock : getLastMessage().content())
		{
			Optional<TextBlock> textBlock	= contentBlock.text();

			textBlock.ifPresent(block -> answerTextList.add(block.text()));
		}
		return answerTextList;
	}

	// Claude may answer in several text blocks. They are consecutive parts of one answer, so all
	// of them are joined rather than the first one kept and the rest silently lost.
	public static final String TEXT_BLOCK_SEPARATOR = "\n\n";

	// Why an answer stopped short, in the visitor's words: the limit is the site owner's.
	public static final String OVER_LIMIT_TEXT = "That question takes more work than this site "
			+ "allows for one answer. Try asking something simpler.";

	/**
	 * What the visitor is shown: the text when Claude finished, and otherwise a sentence saying
	 * why there is no answer, or that the one there is was cut short.
	 */
	public String getUserText()
	{
		String		text		= String.join(TEXT_BLOCK_SEPARATOR, getTextList());
		StopReason	reason		= getStopReason();
		String		userText	= text;

		if (StopReason.REFUSAL.equals(reason))
		{
			String	refusalText	= getRefusalText();
			userText = null == refusalText ? "I can't answer that." : refusalText;
		}
		else if (StopReason.MAX_TOKENS.equals(reason))
		{
			userText = String.format("%s%s%s", text, TEXT_BLOCK_SEPARATOR, OVER_LIMIT_TEXT);
		}
		else if (StopReason.TOOL_USE.equals(reason))
		{
			userText = OVER_LIMIT_TEXT;
		}
		return userText;
	}

	public StopReason getStopReason()
	{
		StopReason reason = getLastMessage().stopReason().orElse(null);
		return reason;
	}
	public boolean isEndTurn()
	{
		return StopReason.END_TURN.equals(getStopReason());
	}

	public RefusalStopDetails getStopDetails()
	{
		RefusalStopDetails stopDetails = getLastMessage().stopDetails().orElse(null);
		return stopDetails;
	}

	// The stop reason as the wire spells it, for a caller that has no business with the SDK's
	// own type. Null when the answer states none.
	public String getStopReasonText()
	{
		StopReason	reason		= getStopReason();
		String		reasonText	= null == reason ? null : reason.asString();

		return reasonText;
	}

	/**
	 * The refusal's category and explanation as one line, or null when the answer carries no
	 * refusal. Either half may be missing on a refusal that does carry details.
	 */
	public String getRefusalText()
	{
		RefusalStopDetails	stopDetails	= getStopDetails();
		String				refusalText	= null;

		if (null != stopDetails)
		{
			String	categoryText	= stopDetails.category()
					.map(RefusalStopDetails.Category::asString).orElse("unstated");
			String	explanation		= stopDetails.explanation().orElse("no explanation");

			refusalText = String.format("%s: %s", categoryText, explanation);
		}
		return refusalText;
	}
}
