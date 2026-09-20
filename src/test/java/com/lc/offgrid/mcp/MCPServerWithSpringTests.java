package com.lc.offgrid.mcp;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.OffgridTestApplication;
import com.lc.offgrid.common.misc.files.LocalFileManager.DocumentFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.ImageFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager.JsonResourceFileManager;
import com.lc.offgrid.common.pojo.part.Access;
import com.lc.offgrid.common.pojo.part.Access.*;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry;
import com.lc.offgrid.webapp.mcp.NextMaintenanceTool;
import com.lc.offgrid.webapp.mcp.WorstRoadTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The file managers taken from the Spring context rather than constructed: the root folder is
 * the one the profile states, and afterPropertiesSet has already run on each bean.
 */
@SpringBootTest(classes = OffgridTestApplication.class)
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MCPServerWithSpringTests extends AbstractTests
{
	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private DocumentFileManager documentFileManager;

	@Autowired
	private ImageFileManager imageFileManager;

	@Autowired
	private JsonResourceFileManager jsonResourceFileManager;

	@Autowired
	private NextMaintenanceTool nextMaintenanceTool;

	@Autowired
	private WorstRoadTool worstRoadTool;

	public DocumentFileManager getDocumentFileManager()
	{
		return documentFileManager;
	}
	public ImageFileManager getImageFileManager()
	{
		return imageFileManager;
	}
	public JsonResourceFileManager getJsonResourceFileManager()
	{
		return jsonResourceFileManager;
	}
	public NextMaintenanceTool getNextMaintenanceTool()
	{
		return nextMaintenanceTool;
	}
	public WorstRoadTool getWorstRoadTool()
	{
		return worstRoadTool;
	}

	@Test
	public void testNextMaintenance()
	{
		Map<String, MaintenanceEntry.NextDue> map = getNextMaintenanceTool().getNextMaintenance();
		assertNotNull(map);
	}

	public Object[][] WorstRoadDate()
	{
		return new Object[][] {
				new Object[] {"morton-lake-park", null},
				new Object[] {"pacific-playgrounds-resort", RoadType.PAVEMENT},
				new Object[] {"salmon-point-resort", RoadType.PAVEMENT},
				new Object[] {"amor-lake-rec0174", null},
				new Object[] {"beavertail-lake-dayuse", RoadType.POTHOLES},
				new Object[] {"echo-lake-dayuse", RoadType.UNPAVED},
				new Object[] {"mohun-lake-rec0184", null},
				new Object[] {"roberts-lake-rec0191", RoadType.DIRT},
		};
	}

	@ParameterizedTest
	@MethodSource("WorstRoadDate")
	public void testWorstRoad(String destination, RoadType roadType) throws IOException
	{
		Leg leg = getWorstRoadTool().getWorstRoad(destination);
		if (null == roadType)
		{
			assertNull(leg);
		}
		else
		{
			assertNotNull(leg);
			assertEquals(roadType, leg.getType());
		}
	}

}
