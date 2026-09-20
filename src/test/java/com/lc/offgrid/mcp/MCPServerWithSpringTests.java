package com.lc.offgrid.mcp;

import com.lc.basics.tools.function.TriFunction;
import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.OffgridTestApplication;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.DocumentFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager.ImageFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager.JsonResourceFileManager;
import com.lc.offgrid.common.misc.imaging.ImageSize;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry;
import com.lc.offgrid.webapp.mcp.NextMaintenanceTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

	@Test
	public void testNextMaintenance()
	{
		Map<String, MaintenanceEntry.NextDue> map = getNextMaintenanceTool().getNextMaintenance();
		assertNotNull(map);
	}


}
