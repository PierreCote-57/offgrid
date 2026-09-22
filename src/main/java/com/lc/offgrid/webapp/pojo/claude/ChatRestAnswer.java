package com.lc.offgrid.webapp.pojo.claude;

import com.lc.offgrid.webapp.spring.tools.RestBaseAnswer;

/**
 * The chat's reply: what was answered, why it stopped where it did, and what the call cost.
 * The timing comes from RestBaseAnswer.
 * A reply with no text is a real answer to a real call, so it travels with the reason instead
 * of being flattened to an empty bubble. Nothing here names where the answer came from.
 */
public class ChatRestAnswer extends RestBaseAnswer
{
	private String	text;

	// How many blocks the text above was joined from.
	private int		blockCount;

	private String	stopReason;
	private String	refusalText;

	// The model that answered, which is not always the one asked for.
	private String	modelName;

	// The whole call priced in input tokens, the model's own ratios applied.
	private double	effectiveToken;

	public String getText()
	{
		return text;
	}
	public void setText(String text)
	{
		this.text = text;
	}

	public int getBlockCount()
	{
		return blockCount;
	}
	public void setBlockCount(int blockCount)
	{
		this.blockCount = blockCount;
	}

	public String getStopReason()
	{
		return stopReason;
	}
	public void setStopReason(String stopReason)
	{
		this.stopReason = stopReason;
	}

	public String getRefusalText()
	{
		return refusalText;
	}
	public void setRefusalText(String refusalText)
	{
		this.refusalText = refusalText;
	}

	public String getModelName()
	{
		return modelName;
	}
	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	public double getEffectiveToken()
	{
		return effectiveToken;
	}
	public void setEffectiveToken(double effectiveToken)
	{
		this.effectiveToken = effectiveToken;
	}
}
