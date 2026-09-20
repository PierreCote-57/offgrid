package com.lc.offgrid.webapp.mcp;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.basics.tools.file.SimpleFilterString;
import com.lc.offgrid.common.pojo.page.MaintenancePage;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry.NextDue;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * What each vehicle is due for next.
 */
@Component
public class NextMaintenanceTool extends AbstractOffgridMCP
{
	private static final SimpleFilterString FILTER = new SimpleFilterString(null, "/maintenance/", null);

	@McpTool(name = "next-maintenance",
			description = "The next scheduled maintenance for the van and for the Bronco",
			generateOutputSchema = true)
	public Map<String, NextDue> getNextMaintenance()
	{
		Map<String, File> fileMap = getJsonManager().filterMap(FILTER);
		Map<String, NextDue> dueMap = new TreeMap<>();
		for (Map.Entry<String, File> entry : fileMap.entrySet())
		{
			String name =  entry.getKey();
			File file = entry.getValue();
			try
			{
				MaintenancePage page = BasicFileReader.readJsonFile(file, MaintenancePage.class);
				NextDue nextDue = page.getNextMaintenance();
				dueMap.put(page.getName(), nextDue);
			}
			catch (IOException e)
			{
				getLogger().warn("Unable to read maintenance file %s", file.getAbsolutePath());
				continue;
			}
		}

		return dueMap;
	}
}
