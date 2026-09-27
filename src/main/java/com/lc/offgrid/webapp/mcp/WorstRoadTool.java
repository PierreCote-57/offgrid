package com.lc.offgrid.webapp.mcp;

import com.lc.basics.tools.file.BasicFileReader;
import com.lc.offgrid.common.pojo.page.DestinationPage;
import com.lc.offgrid.common.pojo.part.Access;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * What the road in to one destination is like at its worst.
 */
@Component
public class WorstRoadTool extends AbstractOffgridMCP
{
	@McpTool(name = "worst-road",
			description = "The roughest stretch of road on the way in to a destination",
			generateOutputSchema = true)
	public Access.Leg getWorstRoad(
			@McpToolParam(description = "The destination, named as the site writes it",
					required = true) String destinationName)
	{
		logMcpCall("getWorstRoad(%s)", destinationName);

		try
		{
			Resource resource = getJsonManager().getResource(destinationName);
			DestinationPage page = BasicFileReader.readJsonFile(resource.getURL(), DestinationPage.class);
			Access access = page.getAccess();
			Access.Leg leg = null == access ? null : access.getRoadLimitingLeg();
			leg = null == leg ? new Access.Leg(Access.RoadType.UNKNOWN, null) : leg;
			return leg;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Unable to answer for destination named '" + destinationName + "'");
		}
	}
}
