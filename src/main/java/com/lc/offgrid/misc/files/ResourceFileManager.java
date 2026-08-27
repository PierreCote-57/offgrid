package com.lc.offgrid.misc.files;

import com.lc.offgrid.misc.imaging.OffgridImageManager;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

public class ResourceFileManager extends AbstractFileManager
{
	private String extension;

	@Component
	public static class JsonResourceFileManager extends ResourceFileManager
	{
		public JsonResourceFileManager()
		{
			super("json");
		}
	}

	public ResourceFileManager(String extension)
	{
		this.extension = extension;
	}

	@Override
	public String getRootFolder()
	{
		URL url = getClass().getResource("/data");
		String filePath = url.getFile();
		return filePath;
	}

	@Override
	public boolean isValid(File file)
	{
		return file.getName().endsWith(extension);
	}
}
