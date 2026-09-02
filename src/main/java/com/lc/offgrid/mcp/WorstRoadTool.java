package com.lc.offgrid.mcp;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * What the road in to one destination is like at its worst.
 *
 * The answer is hard-coded for now. The real one is the limiting leg of that destination's
 * access, which Access.getRoadLimitingLeg already builds.
 */
@Component
public class WorstRoadTool
{
	@McpTool(name = "worst-road",
			description = "The roughest stretch of road on the way in to a destination")
	public String getWorstRoad(
			@McpToolParam(description = "The destination, named as the site writes it",
					required = true) String destinationName)
	{
		String worstRoad = String.format("The way in to %1$s is 4 km of potholes at its worst.",
				destinationName);
		return worstRoad;
	}
}
