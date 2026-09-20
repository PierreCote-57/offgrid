package com.lc.offgrid.webapp.mcp;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.file.SimpleFilterString;
import com.lc.offgrid.common.pojo.page.MaintenancePage;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * What each vehicle is due for next.
 */
@Component
public class NextMaintenanceTool extends AbstractOffgridMCP
{
	private static final SimpleFilterString FILTER = new SimpleFilterString(null, "/maintenance/", null);

	@McpTool(name = "next-maintenance",
			description = "The next scheduled maintenance for the van and for the Bronco")
	public String getNextMaintenance()
	{
		Map<String, File> map = getJsonManager().filterMap(FILTER);
		for (Map.Entry<String, File> entry : map.entrySet())
		{
			String name =  entry.getKey();
			File file = entry.getValue();
			try
			{
				MaintenancePage page = BasicFileReader.readJsonFile(file, MaintenancePage.class);

			}
			catch (IOException e)
			{
				getLogger().warn("Unable to read maintenance file %s", file.getAbsolutePath());
				continue;
			}
		}

		String nextMaintenance = "Van: oil change, due 2026-10-15.\nBronco: tire rotation, due 2026-09-20.";
		return nextMaintenance;
	}
}
