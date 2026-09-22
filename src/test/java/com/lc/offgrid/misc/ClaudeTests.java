package com.lc.offgrid.misc;

import com.anthropic.models.messages.CacheCreation;
import com.anthropic.models.messages.Usage;
import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.misc.claude.ClaudeModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What one call costs in effective tokens, for the counts an answer can carry: plain input and
 * output, a cache read, and a cache write under either time to live.
 */
public class ClaudeTests extends AbstractTests
{
	/** The counts are whole tokens and the ratios are exact, so a sum is right to the penny. */
	private static final double	TOKEN_TOLERANCE	= 0.0001;

	/**
	 * A model, the five counts of one usage, and the effective tokens they add up to. A null
	 * count is one the answer does not carry: null for both cache writes leaves the usage with
	 * no cache creation at all.
	 */
	public static Object[][] UsageEffectiveTokenSource()
	{
		return new Object[][] {
				new Object[] {ClaudeModel.OPUS,		 1_000L,	   500L,	   null,	   null,	   null,	3_500.0},
				new Object[] {ClaudeModel.OPUS,		   100L,	   200L,	 8_000L,	   null,	   null,	1_900.0},
				new Object[] {ClaudeModel.SONNET,	   500L,	   100L,	   null,	 4_000L,	     0L,	6_000.0},
				new Object[] {ClaudeModel.HAIKU,	   200L,	    50L,	 1_000L,	     0L,	 2_000L,	4_550.0},
				new Object[] {ClaudeModel.FABLE,	   300L,	   400L,	10_000L,	 1_000L,	 2_000L,	7_800.0},
		};
	}

	@ParameterizedTest
	@MethodSource("UsageEffectiveTokenSource")
	public void testGetEffectiveToken(ClaudeModel model, long inputToken, long outputToken,
			Long cacheReadToken, Long cacheWrite5mToken, Long cacheWrite1hToken,
			double expectedEffectiveToken) throws Exception
	{
		CacheCreation	cacheCreation	= null;

		if (null != cacheWrite5mToken || null != cacheWrite1hToken)
		{
			CacheCreation.Builder	cacheBuilder	= CacheCreation.builder();

			cacheBuilder.ephemeral5mInputTokens(null == cacheWrite5mToken ? 0L : cacheWrite5mToken);
			cacheBuilder.ephemeral1hInputTokens(null == cacheWrite1hToken ? 0L : cacheWrite1hToken);

			cacheCreation = cacheBuilder.build();
		}
		// The builder requires every field, so what the answer does not carry is set empty.
		Usage.Builder	usageBuilder	= Usage.builder();

		usageBuilder.inputTokens(inputToken);
		usageBuilder.outputTokens(outputToken);
		usageBuilder.cacheReadInputTokens(Optional.ofNullable(cacheReadToken));
		usageBuilder.cacheCreation(Optional.ofNullable(cacheCreation));
		usageBuilder.cacheCreationInputTokens(Optional.empty());
		usageBuilder.inferenceGeo(Optional.empty());
		usageBuilder.serverToolUse(Optional.empty());
		usageBuilder.serviceTier(Optional.empty());

		Usage	usage			= usageBuilder.build();
		double	effectiveToken	= model.getEffectiveToken(usage);

		assertEquals(expectedEffectiveToken, effectiveToken, TOKEN_TOLERANCE,
				String.format("getEffectiveToken(%s: in %d, out %d, read %s, 5m %s, 1h %s)",
						model, inputToken, outputToken, cacheReadToken, cacheWrite5mToken,
						cacheWrite1hToken));
	}
}
