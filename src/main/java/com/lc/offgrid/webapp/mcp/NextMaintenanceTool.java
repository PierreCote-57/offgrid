package com.lc.offgrid.webapp.mcp;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * What each vehicle is due for next.
 */
@Component
public class NextMaintenanceTool
{
	@McpTool(name = "next-maintenance",
			description = "The next scheduled maintenance for the van and for the Bronco")
	public String getNextMaintenance()
	{
		String nextMaintenance = "Van: oil change, due 2026-10-15.\nBronco: tire rotation, due 2026-09-20.";
		return nextMaintenance;
	}
}
