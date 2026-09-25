package com.lc.offgrid.common.misc.claude;

import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
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
	public Message getMessage()
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

	public String getId()
	{
		return getMessage().id();
	}

	// The model that answered, which is not always the one asked for: a refusal fallback answers
	// on a different model.
	public String getModelName()
	{
		return getMessage().model().asString();
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

		for (ContentBlock contentBlock : getMessage().content())
		{
			Optional<TextBlock> textBlock	= contentBlock.text();

			textBlock.ifPresent(block -> answerTextList.add(block.text()));
		}
		return answerTextList;
	}

	public StopReason getStopReason()
	{
		StopReason reason = getMessage().stopReason().orElse(null);
		return reason;
	}
	public boolean isEndTurn()
	{
		return StopReason.END_TURN.equals(getStopReason());
	}

	public RefusalStopDetails getStopDetails()
	{
		RefusalStopDetails stopDetails = getMessage().stopDetails().orElse(null);
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
