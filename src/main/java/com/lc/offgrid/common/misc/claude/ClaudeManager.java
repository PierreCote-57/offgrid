package com.lc.offgrid.common.misc.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.services.blocking.MessageService;
import com.lc.basics.tools.file.BaseFileHandler;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.OffgridUtil;
import com.lc.offgrid.common.pojo.claude.ChatMessage;
import com.lc.offgrid.common.pojo.claude.ChatRequest;
import io.modelcontextprotocol.server.McpServerFeatures.*;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

	/**
	 * One row per question put to Claude. The name is what routes it: log4j2-spring.xml gives
	 * offgrid.claude its own appender and does not let it reach the others.
	 */
	private static final BasicLogger CLAUDE_LOGGER = BasicLogger.getLogger("offgrid.claude");

	// How many of the transcript's latest lines Claude reads, the question included. Odd, so the
	// window opens on a visitor's line, which is what the first message has to be.
	private static final int MESSAGE_COUNT_MAX_CHAT = 5;


	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static BasicLogger getClaudeLogger()
	{
		return CLAUDE_LOGGER;
	}

	@Value("${anthropic.claude.key}")
	private String claudeKey;

	@Autowired
	@Lazy
	private McpSyncServer mcpSyncServer;

	// What Spring AI built /mcp from: each definition beside the handler that runs it. Spring AI
	// declares more than one list of each kind, and the server merges them, so these do the same.
	@Lazy
	@Autowired
	private ObjectProvider<List<SyncToolSpecification>> toolSpecificationProvider;

	@Lazy
	@Autowired
	private ObjectProvider<List<SyncResourceSpecification>> resourceSpecificationProvider;

	@Lazy
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
		int firstIndex = Math.max(0, messageList.size() - MESSAGE_COUNT_MAX_CHAT);

		ClaudeConfig config = ClaudeConfig.WITH_MCP;
		MessageCreateParams.Builder builder = config.makeBuilder(getMcpSyncServer(), "site");
		// Context is added late to facilitate caching
		// Question comes last and alone so users of ClaudeAnswer have access to it
		addMessageList(builder, messageList, firstIndex, lastIndex);
		builder.addUserMessage(makeContextText(chatRequest));
		builder.addUserMessage(messageList.get(lastIndex).getText());

		MessageCreateParams params = builder.build();

		long startTime = System.nanoTime();
		ClaudeAnswer answer = send(params);
		logClaudeCall("chat", answer, startTime);
		getLogger().debug("chat(%s) answered %s on %s",
				answer.getQuestionText(), answer.getTextList(), answer.getModel());

		return answer;
	}

	// Each line from iMin up to, not including, iMax, as the turn its role says it is.
	public static void addMessageList(MessageCreateParams.Builder builder,
			List<ChatMessage> messageList, int iMin, int iMax)
	{
		for (int i = iMin; i < iMax; i++)
		{
			ChatMessage message = messageList.get(i);
			if (ChatMessage.Role.USER == message.getRole())
			{
				builder.addUserMessage(message.getText());
			}
			else
			{
				builder.addAssistantMessage(message.getText());
			}
		}
	}

	/**
	 * What Claude cannot know about the visitor, in front of the question: their local date and
	 * time, and where they are when the browser was allowed to say. It rides in the user turn, not
	 * the system prompt, which stays the same on every call and so can be cached. A zone the
	 * browser did not state, or one that does not parse, falls back to the server's own, named.
	 */
	private static String makeContextText(ChatRequest chatRequest)
	{
		ZoneId zone = OffgridUtil.parseTimeZone(chatRequest.getTimeZone());
		ZonedDateTime now = ZonedDateTime.now(zone);
		String nowText = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

		List<String> lineList = new ArrayList<>();
		lineList.add(String.format("Visitor's local time: %s, %s", nowText, zone.getId()));

		Double latitude = chatRequest.getLatitudeDeg();
		Double longitude = chatRequest.getLongitudeDeg();
		if (null != latitude && null != longitude)
		{
			// Double's own text, not %f: a locale with a decimal comma would make the pair ambiguous.
			lineList.add(String.format("Visitor's location: %s, %s", latitude, longitude));
		}
		String contextText = String.join("\n", lineList);
		return contextText;
	}

	private static final int MAX_TOOL_USE_CALL_COUNT = 3;
	public ClaudeAnswer send(MessageCreateParams paramsIn)
	{
		MessageService messageService = getClaudeClient().messages();
		int callCount = 0;
		ClaudeAnswer answer = null;
		List<Message> messageList = new ArrayList<>();
		MessageCreateParams params = paramsIn;
		while (callCount < MAX_TOOL_USE_CALL_COUNT)
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
		answer = new ClaudeAnswer(paramsIn, answer.getMessageList());

		return answer;
	}

	/**
	 * The row one question leaves in the Claude log: who asked, the model that answered, why it
	 * stopped, the effective tokens of every round, how long it took, and the question last —
	 * the one column that can hold anything, with its tabs and line breaks made spaces.
	 */
	public static void logClaudeCall(String callerName, ClaudeAnswer answer, long startTime)
	{
		long	elapsedNs		= System.nanoTime() - startTime;
		double	elapsedSecond	= elapsedNs / 1e9;
		double	effectiveToken	= answer.getEffectiveToken();
		String	questionLine	= answer.getQuestionText().replaceAll("[\\t\\r\\n]+", " ");

		getClaudeLogger().info("%1$s\t%2$s\t%3$s\t%4$,.0f\t%5$.3f\t%6$s",
				callerName, answer.getModelName(),
				answer.getStopReasonText(), effectiveToken,
				elapsedSecond, questionLine);
	}

	private MessageCreateParams processTool(ClaudeAnswer answer)
	{
		// We are answering the last message
		Message message = answer.getLastMessage();

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
		// A tool with an output schema answers in structuredContent alone, with no text content.
		Object	structuredContent	= toolResult.structuredContent();
		if (null != structuredContent)
		{
			String	structuredText	= BaseFileHandler.getGson().toJson(structuredContent);
			textList.add(structuredText);
		}
		String	resultText	= String.join("\n", textList);
		return resultText;
	}
}
