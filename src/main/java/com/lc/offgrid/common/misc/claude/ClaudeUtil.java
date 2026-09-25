package com.lc.offgrid.common.misc.claude;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolUnion;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClaudeUtil
{
	// A variable in a uri template, like the fileName in offgrid://image/{fileName}.
	private static final Pattern	TEMPLATE_VARIABLE_PATTERN	= Pattern.compile("\\{([^}]+)\\}");

	// The uri a template names once each {variable} is replaced by the argument of the same name.
	public static String fillUriTemplate(String uriTemplate, Map<String, Object> argumentMap)
	{
		Matcher variableMatcher = TEMPLATE_VARIABLE_PATTERN.matcher(uriTemplate);
		StringBuilder uriBuilder = new StringBuilder();

		while (variableMatcher.find())
		{
			String variableName = variableMatcher.group(1);
			String variableValue = String.valueOf(argumentMap.get(variableName));
			variableMatcher.appendReplacement(uriBuilder, Matcher.quoteReplacement(variableValue));
		}
		variableMatcher.appendTail(uriBuilder);

		String uri = uriBuilder.toString();
		return uri;
	}

	public static void addTools(MessageCreateParams.Builder builder, McpSyncServer mcpSyncServer)
	{
		addToolList(builder, mcpSyncServer.listTools(), ClaudeUtil::translateToolUnion);
		addToolList(builder, mcpSyncServer.listResources(), ClaudeUtil::translateResourceToolUnion);
		addToolList(builder, mcpSyncServer.listResourceTemplates(), ClaudeUtil::translateResourceTemplateToolUnion);
	}

	private static <T> void addToolList(MessageCreateParams.Builder builder, List<T> toolList, Function<T, ToolUnion> toolFunction)
	{
		for (T toolItem : toolList)
		{
			builder.addTool(toolFunction.apply(toolItem));
		}
	}


	@SuppressWarnings("unchecked")
	public static ToolUnion translateToolUnion(McpSchema.Tool mcpTool)
	{
		Map<String, Object> schemaMap		= mcpTool.inputSchema();
		Map<String, Object>	propertyMap		= (Map<String, Object>) schemaMap.getOrDefault("properties", Map.of());
		List<String>		requiredList	= (List<String>) schemaMap.getOrDefault("required", List.of());

		Tool.InputSchema.Properties.Builder	propertiesBuilder	= Tool.InputSchema.Properties.builder();
		for (Map.Entry<String, Object> propertyEntry : propertyMap.entrySet())
		{
			propertiesBuilder.putAdditionalProperty(propertyEntry.getKey(), JsonValue.from(propertyEntry.getValue()));
		}

		Tool.InputSchema.Builder	schemaBuilder	= Tool.InputSchema.builder();
		schemaBuilder.properties(propertiesBuilder.build());
		schemaBuilder.required(requiredList);

		Tool.Builder	toolBuilder	= Tool.builder();
		toolBuilder.name(mcpTool.name());
		toolBuilder.description(mcpTool.description());
		toolBuilder.inputSchema(schemaBuilder.build());

		ToolUnion	toolUnion	= ToolUnion.ofTool(toolBuilder.build());
		return toolUnion;
	}

	// A resource's uri holds no variable, so its tool takes no input.
	public static ToolUnion translateResourceToolUnion(McpSchema.Resource resource)
	{
		Tool.InputSchema.Properties	properties	= Tool.InputSchema.Properties.builder().build();

		Tool.InputSchema.Builder	schemaBuilder	= Tool.InputSchema.builder();
		schemaBuilder.properties(properties);

		Tool.Builder	toolBuilder	= Tool.builder();
		toolBuilder.name(resource.name());
		toolBuilder.description(resource.description());
		toolBuilder.inputSchema(schemaBuilder.build());

		ToolUnion	toolUnion	= ToolUnion.ofTool(toolBuilder.build());
		return toolUnion;
	}

	// Each variable in the template becomes a required string parameter of the same name.
	public static ToolUnion translateResourceTemplateToolUnion(McpSchema.ResourceTemplate resourceTemplate)
	{
		Matcher								variableMatcher		= TEMPLATE_VARIABLE_PATTERN.matcher(resourceTemplate.uriTemplate());
		Tool.InputSchema.Properties.Builder	propertiesBuilder	= Tool.InputSchema.Properties.builder();
		List<String>						requiredList		= new ArrayList<>();

		while (variableMatcher.find())
		{
			String				variableName		= variableMatcher.group(1);
			Map<String, String>	variableProperty	= Map.of("type", "string");

			propertiesBuilder.putAdditionalProperty(variableName, JsonValue.from(variableProperty));
			requiredList.add(variableName);
		}

		Tool.InputSchema.Builder	schemaBuilder	= Tool.InputSchema.builder();
		schemaBuilder.properties(propertiesBuilder.build());
		schemaBuilder.required(requiredList);

		Tool.Builder	toolBuilder	= Tool.builder();
		toolBuilder.name(resourceTemplate.name());
		toolBuilder.description(resourceTemplate.description());
		toolBuilder.inputSchema(schemaBuilder.build());

		ToolUnion	toolUnion	= ToolUnion.ofTool(toolBuilder.build());
		return toolUnion;
	}

}
