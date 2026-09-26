package com.lc.offgrid.common.misc.claude;

import com.anthropic.models.messages.CacheCreation;
import com.anthropic.models.messages.Usage;

/**
 * The Claude models this site asks, and what their tokens cost. One constant per model family,
 * naming the current model in that family: an older model in the same family is priced
 * differently and is a constant of its own.
 */
public enum ClaudeModel
{
	OPUS	("claude-opus-5-5",		 4.00)
	{
		// The published rate: Opus 5.5 reads its cache at half the usual rate.
		@Override
		public double getCacheReadRatio()
		{
			return 0.05;
		}
	},
	OPUS_5	("claude-opus-5",		 5.00),
	SONNET	("claude-sonnet-5",		 2.00),
	HAIKU	("claude-haiku-4-5",	 1.00),
	FABLE	("claude-fable-5-1",	10.00)
	{
		// The published rate: Fable 5.1 reads its cache at a quarter of the usual rate.
		@Override
		public double getCacheReadRatio()
		{
			return 0.025;
		}
	},
	UNKNOWN	(null,					 0.00)
	{
		// A model no constant names has no published rate to count its tokens by.
		@Override
		public double getEffectiveToken(Usage usage)
		{
			return 0.0;
		}
	}
	;

	private String	modelId;

	// Dollars per million input tokens, which is the price every ratio below multiplies.
	private double	costPerMillionToken;

	ClaudeModel(String modelId, double costPerMillionToken)
	{
		this.modelId				= modelId;
		this.costPerMillionToken	= costPerMillionToken;
	}

	// UNKNOWN has no id, so it is only ever the fallback, never a match.
	public static ClaudeModel of(String modelId)
	{
		ClaudeModel	model	= UNKNOWN;

		for (ClaudeModel candidate : values())
		{
			if (null != candidate.getModelId() && candidate.getModelId().equals(modelId))
			{
				model = candidate;
				break;
			}
		}
		return model;
	}

	// What the API is asked for, which is not the constant's own name.
	public String getModelId()
	{
		return modelId;
	}
	public double getCostPerMillionToken()
	{
		return costPerMillionToken;
	}

	// Every ratio is a multiple of the base input price, which costPerMillionToken holds.
	public double getInputRatio()
	{
		return 1.00;
	}
	public double getOutputRatio()
	{
		return 5.00;
	}
	public double getCacheWrite5mRatio()
	{
		return 1.25;
	}
	public double getCacheWrite1hRatio()
	{
		return 2.00;
	}
	public double getCacheReadRatio()
	{
		return 0.10;
	}

	/**
	 * One call's whole cost converted to input tokens
	 */
	public double getEffectiveToken(Usage usage)
	{
		double			inputToken		= usage.inputTokens() * getInputRatio();
		double			outputToken		= usage.outputTokens() * getOutputRatio();
		double			cacheReadToken	= usage.cacheReadInputTokens().orElse(0L) * getCacheReadRatio();
		CacheCreation	cacheCreation	= usage.cacheCreation().orElse(null);
		double			cacheWriteToken	= null == cacheCreation ? 0.0
				: cacheCreation.ephemeral5mInputTokens() * getCacheWrite5mRatio()
					+ cacheCreation.ephemeral1hInputTokens() * getCacheWrite1hRatio();

		double			effectiveToken	= inputToken + outputToken + cacheReadToken
				+ cacheWriteToken;

		return effectiveToken;
	}

	// One call's whole cost in dollars.
	public double getCost(Usage usage)
	{
		double	effectiveToken	= getEffectiveToken(usage);
		double	cost			= effectiveToken * getCostPerMillionToken() / 1_000_000;
		return cost;
	}
}
