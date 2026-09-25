package com.lc.offgrid.webapp.mcp;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractOffgridMCP
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(AbstractOffgridMCP.class);

	/**
	 * One row per call this server answers. The name is what routes it: log4j2-spring.xml gives
	 * offgrid.mcp its own appender and does not let it reach the others.
	 */
	private static final BasicLogger MCP_LOGGER = BasicLogger.getLogger("offgrid.mcp");

	@Autowired
	private ResourceFileManager.JsonResourceFileManager jsonManager ;

	@Autowired
	private LocalFileManager.ImageFileManager imageManager;

	@Autowired
	private LocalFileManager.DocumentFileManager documentManager;

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static BasicLogger getMcpLogger()
	{
		return MCP_LOGGER;
	}

	// The row one call leaves in the MCP log, whoever made it: the call as methodName(parameters).
	public static void logMcpCall(String format, Object... args)
	{
		String	call	= String.format(format, args);
		getMcpLogger().info("%s", call);
	}

	public LocalFileManager.ImageFileManager getImageManager()
	{
		return imageManager;
	}

	public AbstractFileManager getJsonManager()
	{
		return jsonManager;
	}

	public AbstractFileManager getDocumentManager()
	{
		return documentManager;
	}

}
