package com.lc.offgrid.webapp.mcp;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The pre-made checklists this server hands to a client, published as MCP resources.
 */
@Component
public class ChecklistResource extends AbstractOffgridMCP
{
	// A checklist is a page whose json sits under this folder, named for the page.
	private static final String	CHECKLIST_FOLDER	= "/data/hardware/checklist/";
	private static final String	CHECKLIST_URL		= "%s://%s:%s/hardware/checklist/%s";

	// Where a visitor reaches the site, which is not where it listens when something sits in front.
	@Value("${server.publicProtocol}")
	private String	publicProtocol;

	@Value("${server.publicHostName}")
	private String	publicHostName;

	@Value("${server.publicPort}")
	private String	publicPort;

	public String getPublicProtocol()
	{
		return publicProtocol;
	}

	public String getPublicHostName()
	{
		return publicHostName;
	}

	public String getPublicPort()
	{
		return publicPort;
	}

	/**
	 * The checklists this server can hand out, one name per line.
	 * <p>
	 * requestUri is unused and cannot be dropped: a resource whose uri holds no variable must
	 * still declare a String or a ReadResourceRequest parameter, and a String receives the uri
	 * the client asked for.
	 */
	@McpResource(uri = "offgrid://checklist", name = "checklist-list",
			description = "The pre-made checklists this site can hand you, one name per line",
			mimeType = "text/plain")
	public String readChecklistList(String requestUri)
	{
		logMcpCall("readChecklistList(%s)", requestUri);

		List<String>	checklistNameList	= getChecklistNameList();
		String			checklistNameText	= String.join("\n", checklistNameList);
		return checklistNameText;
	}

	/**
	 * The link to one checklist's page, named by a name off that list. Spring AI matches the read
	 * uri against the template and hands the {checklistName} it found to the parameter.
	 */
	@McpResource(uri = "offgrid://checklist/{checklistName}", name = "checklist",
			description = "The link to one pre-made checklist's page on this site, by the name "
					+ "the checklist list gives",
			mimeType = "text/plain")
	public String readChecklist(String checklistName)
	{
		logMcpCall("readChecklist(%s)", checklistName);

		List<String>	checklistNameList	= getChecklistNameList();
		String			checklistText		= checklistNameList.contains(checklistName)
				? String.format(CHECKLIST_URL, getPublicProtocol(), getPublicHostName(),
						getPublicPort(), checklistName)
				: String.format("No checklist is named %s", checklistName);
		return checklistText;
	}

	// The json file names under CHECKLIST_FOLDER, in name order.
	private List<String> getChecklistNameList()
	{
		List<String>	checklistNameList	= new ArrayList<>();

		for (Map.Entry<String, File> fileEntry : getJsonManager().getNameMap().entrySet())
		{
			String	filePath	= fileEntry.getValue().getAbsolutePath();
			if (filePath.contains(CHECKLIST_FOLDER))
			{
				checklistNameList.add(fileEntry.getKey());
			}
		}
		return checklistNameList;
	}
}
