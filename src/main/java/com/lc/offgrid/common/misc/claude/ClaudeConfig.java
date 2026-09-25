package com.lc.offgrid.common.misc.claude;

import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageCreateParams.Builder;
import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.misc.BasicRuntimeException;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.URL;

import static com.lc.offgrid.common.misc.claude.ClaudeModel.*;

public enum ClaudeConfig
{
	SIMPLE(OPUS, ClaudeConfig.MAX_TOKENS_SMALL)
			{
				@Override
				public Builder makeBuilder(McpSyncServer server, String systemFileName)
				{
					Builder builder = super.makeBuilder(server, systemFileName);
					return builder;
				}
			},
	WITH_MCP(OPUS, ClaudeConfig.MAX_TOKENS_LARGE)
			{
				@Override
				public Builder makeBuilder(McpSyncServer server, String systemFileName)
				{
					Builder builder = super.makeBuilder(server, systemFileName);
					ClaudeUtil.addTools(builder, server);
					return builder;
				}
			};

	// A ceiling on one answer, not a target: an answer that reaches it is cut off mid-sentence.
	public static final long MAX_TOKENS_SMALL		=  2_000L;
	public static final long MAX_TOKENS_MEDIUM		=  5_000L;
	public static final long MAX_TOKENS_LARGE		= 16_000L;

	public static final String SYSTEM_PATH	= "/claude/%s.md";

	private ClaudeModel model;
	private Long maxTokens;

	ClaudeConfig(ClaudeModel model, Long maxTokens)
	{
		this.model = model;
		this.maxTokens = maxTokens;
	}

	public ClaudeModel getModel()
	{
		return model;
	}

	public Long getMaxTokens()
	{
		return maxTokens;
	}

	// Read as a classpath resource, since a file inside the jar has no File path.
	public static String getSystemPrompt(String systemFileName)
	{
		String	path	= String.format(SYSTEM_PATH, systemFileName);
		URL		url		= ClaudeConfig.class.getResource(path);
		if (null == url)
		{
			throw new BasicRuntimeException("getSystemPrompt(%s) found no %s", systemFileName, path);
		}
		String	systemPrompt	= BasicFileReader.readTextFile(url);
		return systemPrompt;
	}

	public Builder makeBuilder(McpSyncServer server, String systemFileName)
	{
		Builder builder = MessageCreateParams.builder();
		builder.model(getModel().getModelId());
		builder.maxTokens(getMaxTokens());
		builder.system(getSystemPrompt(systemFileName));
		return builder;
	}
}
