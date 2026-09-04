package com.lc.offgrid.misc.files;

import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

// Resources are in the war, from resources folder
public class ResourceFileManager extends AbstractFileManager
{
	private String folderName;

	@Component
	public static class JsonResourceFileManager extends ResourceFileManager
	{
		public JsonResourceFileManager()
		{
			super("/data");
		}

		@Override
		public boolean isValid(File file)
		{
			return file.getName().endsWith(".json");
		}
	}

	public ResourceFileManager(String folderName)
	{
		this.folderName = folderName;
	}

	@Override
	public String getRootFolder()
	{
		URL url = getClass().getResource(folderName);
		String filePath = url.getFile();
		return filePath;
	}
}
