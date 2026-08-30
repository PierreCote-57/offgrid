package com.lc.offgrid.misc.files;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

@Component
public class FileTools implements InitializingBean
{
	@Value("${folder.local}")
	// Initializer for tests. As WEB/bean, it gets from config
	private String local_root_folder = "/Users/pierrecote/Pictures/offgrid";

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
		URL url = ClassLoader.getSystemClassLoader().getResource("");
		String rootName = url.getFile();
		processPath(rootName);

		// Process everything in the data folder
		processPath(local_root_folder);
	}
	private void processPath(String path) throws Exception
	{
		AbstractFileManager manager = new LocalFileManager(path);
		manager.afterPropertiesSet();
		for (File file : manager.getNameMap().values())
		{
			long fileLastModified = file.lastModified();
			lastModifiedTime = Math.max(lastModifiedTime, fileLastModified);
		}

	}
}
