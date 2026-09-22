package com.lc.offgrid.common.misc.claude;

import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.RefusalStopDetails;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.Usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageParser
{
	private ClaudeModel	model;
	private Message		message;

	public MessageParser(ClaudeModel model, Message message)
	{
		this.model = model;
		this.message = message;
	}

	// The model the call named, which is the one whose prices this answer is counted against.
	public ClaudeModel getModel()
	{
		return model;
	}
	public Message getMessage()
	{
		return message;
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

	public Usage getUsage()
	{
		return getMessage().usage();
	}

	// The whole call priced in input tokens, by the ratios of the model that was asked.
	public double getEffectiveToken()
	{
		return getModel().getEffectiveToken(getUsage());
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
