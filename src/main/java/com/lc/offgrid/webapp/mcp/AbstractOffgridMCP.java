package com.lc.offgrid.webapp.mcp;

import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.misc.files.AbstractFileManager;
import com.lc.offgrid.common.misc.files.LocalFileManager;
import com.lc.offgrid.common.misc.files.ResourceFileManager;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractOffgridMCP
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(AbstractOffgridMCP.class);

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
