package com.lc.offgrid.common.misc.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUnion;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.ToolUseBlockParam;
import com.anthropic.services.blocking.MessageService;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.basics.tools.units.TimeUnits;
import com.lc.offgrid.common.pojo.claude.ChatMessage;
import com.lc.offgrid.common.pojo.claude.ChatRequest;
import io.modelcontextprotocol.server.McpServerFeatures.*;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Questions put to Claude over the Anthropic API, and the answers that come back. One call is
 * one question: nothing is kept between them, so everything the answer depends on travels on
 * the call that asks.
 */
@Component
public class ClaudeManager implements InitializingBean
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(ClaudeManager.class);

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@Value("${anthropic.claude.key}")
	private String claudeKey;

	@Autowired
	@Lazy
	private McpSyncServer mcpSyncServer;

	// What Spring AI built /mcp from: each definition beside the handler that runs it. Spring AI
	// declares more than one list of each kind, and the server merges them, so these do the same.
	// A provider resolves only when asked, so nothing here is built before the tool beans exist.
	@Autowired
	private ObjectProvider<List<SyncToolSpecification>> toolSpecificationProvider;

	@Autowired
	private ObjectProvider<List<SyncResourceSpecification>> resourceSpecificationProvider;

	@Autowired
	private ObjectProvider<List<SyncResourceTemplateSpecification>> resourceTemplateSpecificationProvider;

	private AnthropicClient claudeClient;

	public McpSyncServer getMcpSyncServer()
	{
		return mcpSyncServer;
	}

	public ObjectProvider<List<SyncToolSpecification>> getToolSpecificationProvider()
	{
		return toolSpecificationProvider;
	}

	public ObjectProvider<List<SyncResourceSpecification>> getResourceSpecificationProvider()
	{
		return resourceSpecificationProvider;
	}

	public ObjectProvider<List<SyncResourceTemplateSpecification>> getResourceTemplateSpecificationProvider()
	{
		return resourceTemplateSpecificationProvider;
	}

	public List<SyncToolSpecification> getToolSpecificationList()
	{
		return flattenProvider(getToolSpecificationProvider());
	}

	public List<SyncResourceSpecification> getResourceSpecificationList()
	{
		return flattenProvider(getResourceSpecificationProvider());
	}

	public List<SyncResourceTemplateSpecification> getResourceTemplateSpecificationList()
	{
		return flattenProvider(getResourceTemplateSpecificationProvider());
	}

	// Every list the provider has, in order, as one list.
	private static <T> List<T> flattenProvider(ObjectProvider<List<T>> listProvider)
	{
		List<T> flatList = new ArrayList<>();

		for (List<T> providedList : listProvider.orderedStream().toList())
		{
			flatList.addAll(providedList);
		}
		return flatList;
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
	 * answer carries what came back — a stop reason and no text when Claude declined.
	 */
	public ClaudeAnswer chat(ChatRequest chatRequest)
	{
		List<ChatMessage> messageList = chatRequest.getMessageList();
		int lastIndex = messageList.size() - 1;
		ChatMessage lastMessage = messageList.get(lastIndex);
		String questionText = lastMessage.getText();

		ClaudeConfig config = ClaudeConfig.WITH_MCP;
		MessageCreateParams.Builder builder = config.makeBuilder(getMcpSyncServer(), "site");
		builder.addUserMessage(questionText);
		MessageCreateParams params = builder.build();

		ClaudeAnswer answer = send(params);
		getLogger().debug("chat(%s) answered %s on %s",
				questionText, answer.getTextList(), answer.getModel());

		return answer;
	}

	private static final int MAX_CALL_COUNT = 3;
	public ClaudeAnswer send(MessageCreateParams params)
	{
		MessageService messageService = getClaudeClient().messages();
		int callCount = 0;
		ClaudeAnswer answer = null;
		List<Message> messageList = new ArrayList<>();
		while (callCount < MAX_CALL_COUNT)
		{
			callCount++;
			Message message = messageService.create(params);
			messageList.add(message);
			answer = new ClaudeAnswer(params, messageList);
			StopReason reason = message.stopReason().orElse(StopReason.END_TURN);
			if (StopReason.TOOL_USE.equals(reason))
			{
				params = processTool(answer);
			}
			else
			{
				// Any other StopReason means end of turn
				break;
			}
		}

		return answer;
	}

	private MessageCreateParams processTool(ClaudeAnswer answer)
	{
		// We are answering the last message
		Message message = answer.getMessage();

		// Creating the new MessageCreateParams
		MessageCreateParams.Builder builder = answer.getParams().toBuilder();
		builder.addMessage(message);                                 // Claude's turn,

		List<ContentBlock> blockList = message.content();
		List<ContentBlockParam> resultList = new ArrayList<>();
		for (ContentBlock block : blockList)
		{
			if (!block.isToolUse())
			{
				continue;
			}
			ToolUseBlock useBlock = block.asToolUse();
			ContentBlockParam blockParam = callUseBlock(useBlock);
			resultList.add(blockParam);
		}
		builder.addUserMessageOfBlockParams(resultList);
		MessageCreateParams params = builder.build();
		return params;
	}

	/**
	 * Runs one tool Claude asked for through the handler /mcp runs it with, so the chat reads the
	 * same answer an outside client does. The exchange is null: in process there is no MCP session.
	 */
	@SuppressWarnings("unchecked")
	private ContentBlockParam callUseBlock(ToolUseBlock useBlock)
	{
		String				name		= useBlock.name();
		Map<String, Object>	argumentMap	= useBlock._input().convert(Map.class);

		SyncToolSpecification toolSpec = findToolSpecification(name);
		SyncResourceSpecification resourceSpec = findResourceSpecification(name);
		SyncResourceTemplateSpecification templateSpec = findResourceTemplateSpecification(name);

		ContentBlockParam	blockParam;
		if (null != toolSpec)
		{
			McpSchema.CallToolRequest	request			= new McpSchema.CallToolRequest(name, argumentMap);
			McpSchema.CallToolResult toolResult = toolSpec.callHandler().apply(null, request);
			blockParam	= translateToolResultBlockParam(useBlock, toolResult);
		}
		else if (null != resourceSpec)
		{
			String uri = resourceSpec.resource().uri();
			McpSchema.ReadResourceRequest request = new McpSchema.ReadResourceRequest(uri);
			McpSchema.ReadResourceResult readResult = resourceSpec.readHandler().apply(null, request);
			blockParam = translateReadResultBlockParam(useBlock, readResult);
		}
		else if (null != templateSpec)
		{
			String uriTemplate = templateSpec.resourceTemplate().uriTemplate();
			String uri = ClaudeUtil.fillUriTemplate(uriTemplate, argumentMap);
			McpSchema.ReadResourceRequest request = new McpSchema.ReadResourceRequest(uri);
			McpSchema.ReadResourceResult readResult = templateSpec.readHandler().apply(null, request);
			blockParam = translateReadResultBlockParam(useBlock, readResult);
		}
		else
		{
			McpSchema.CallToolResult toolResult = makeMissingToolResult(name);
			blockParam = translateToolResultBlockParam(useBlock, toolResult);
		}

		return blockParam;
	}
	private SyncToolSpecification findToolSpecification(String name)
	{
		SyncToolSpecification toolSpecification = null;
		for (SyncToolSpecification spec : getToolSpecificationList())
		{
			if (spec.tool().name().equals(name))
			{
				toolSpecification = spec;
			}
		}
		return toolSpecification;
	}
	private SyncResourceSpecification findResourceSpecification(String name)
	{
		SyncResourceSpecification resourceSpecification = null;
		for (SyncResourceSpecification spec : getResourceSpecificationList())
		{
			if (spec.resource().name().equals(name))
			{
				resourceSpecification = spec;
			}
		}
		return resourceSpecification;
	}
	private SyncResourceTemplateSpecification findResourceTemplateSpecification(String name)
	{
		SyncResourceTemplateSpecification resourceTemplateSpecification = null;
		for (SyncResourceTemplateSpecification spec : getResourceTemplateSpecificationList())
		{
			if (spec.resourceTemplate().name().equals(name))
			{
				resourceTemplateSpecification = spec;
			}
		}
		return resourceTemplateSpecification;
	}
	private static McpSchema.CallToolResult makeMissingToolResult(String name)
	{
		McpSchema.CallToolResult.Builder	missingBuilder	= McpSchema.CallToolResult.builder();
		missingBuilder.addTextContent(String.format("No tool is named %s", name));
		missingBuilder.isError(true);
		McpSchema.CallToolResult			toolResult		= missingBuilder.build();
		return toolResult;
	}

	// The answer to one tool_use, matched to it by its id.
	private static ContentBlockParam translateToolResultBlockParam(ToolUseBlock useBlock,
			McpSchema.CallToolResult toolResult)
	{
		String	resultText	= translateResultText(toolResult);
		boolean	isError		= Boolean.TRUE.equals(toolResult.isError());

		ToolResultBlockParam.Builder	resultBuilder	= ToolResultBlockParam.builder();
		resultBuilder.toolUseId(useBlock.id());
		resultBuilder.content(resultText);
		resultBuilder.isError(isError);

		ContentBlockParam	blockParam	= ContentBlockParam.ofToolResult(resultBuilder.build());
		return blockParam;
	}

	// The answer to one tool_use that read a resource, matched to it by its id.
	private static ContentBlockParam translateReadResultBlockParam(ToolUseBlock useBlock,
			McpSchema.ReadResourceResult readResult)
	{
		String resultText = translateReadResultText(readResult);

		ToolResultBlockParam.Builder resultBuilder = ToolResultBlockParam.builder();
		resultBuilder.toolUseId(useBlock.id());
		resultBuilder.content(resultText);

		ContentBlockParam blockParam = ContentBlockParam.ofToolResult(resultBuilder.build());
		return blockParam;
	}

	// Every text content joined. A blob is named rather than sent: its base64 would be read as text.
	private static String translateReadResultText(McpSchema.ReadResourceResult readResult)
	{
		List<String> textList = new ArrayList<>();

		for (McpSchema.ResourceContents contents : readResult.contents())
		{
			if (contents instanceof McpSchema.TextResourceContents textContents)
			{
				textList.add(textContents.text());
			}
			else if (contents instanceof McpSchema.BlobResourceContents blobContents)
			{
				textList.add(String.format("%s is %s and was not sent", blobContents.uri(),
						blobContents.mimeType()));
			}
		}
		String resultText = String.join("\n", textList);
		return resultText;
	}

	// Every text content joined, which is where Spring AI writes a tool's answer, as JSON text.
	private static String translateResultText(McpSchema.CallToolResult toolResult)
	{
		List<String>	textList	= new ArrayList<>();

		for (McpSchema.Content content : toolResult.content())
		{
			if (content instanceof McpSchema.TextContent textContent)
			{
				textList.add(textContent.text());
			}
		}
		String	resultText	= String.join("\n", textList);
		return resultText;
	}
}
