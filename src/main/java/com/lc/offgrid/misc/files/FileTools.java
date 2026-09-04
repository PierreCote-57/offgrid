package com.lc.offgrid.misc.files;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileTools implements InitializingBean
{
	private static FileTools INSTANCE;

	private long lastModifiedTime;

	public FileTools()
	{
		INSTANCE = this;
	}

	public static long getLastModified()
	{
		return INSTANCE.lastModifiedTime;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		lastModifiedTime = 0;
		// Process everything in the war
		processPath(new ResourceFileManager("/"));

		// Process everything in the data folder
		processPath(new LocalFileManager("/documents"));
		processPath(new LocalFileManager("/images"));
	}

	// Walks the path to determine most recent lastModifiedTime
	private void processPath(AbstractFileManager manager) throws Exception
	{
		manager.afterPropertiesSet();
		for (File file : manager.getNameMap().values())
		{
			long fileLastModified = file.lastModified();
			lastModifiedTime = Math.max(lastModifiedTime, fileLastModified);
		}
	}
}
