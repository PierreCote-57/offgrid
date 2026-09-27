package com.lc.offgrid.common.misc.files;

import com.lc.offgrid.common.misc.imaging.ImageMetadata;
import com.lc.offgrid.common.misc.imaging.ImageMetadataExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

// Local come from the local data folder
public class LocalFileManager extends AbstractFileManager
{
	@Component
	public static class DocumentFileManager extends LocalFileManager
	{
		public DocumentFileManager()
		{
			super("/document");
		}
	}

	@Component
	public static class ImageFileManager extends LocalFileManager
	{
		public ImageFileManager()
		{
			super("/images");
		}

		@Override
		public boolean isValid(File file)
		{
			try
			{
				ImageMetadata metadata = ImageMetadataExtractor.getImageMetadata(file);
				return metadata != null;
			}
			catch (Exception e)
			{
				return false;
			}
		}

		/**
		 * The image's metadata, read from the file each time. There is no cache, so a caller that
		 * asks per request reads the disk per request.
		 */
		public ImageMetadata getImageMetadata(String name)
		{
			ImageMetadata imageMetadata = null;
			try
			{
				File file = getFile(name);
				imageMetadata = null == file
						? null
						: ImageMetadataExtractor.getImageMetadata(file);
			}
			catch (IOException e)
			{
				imageMetadata = null;
			}
			return imageMetadata;
		}
	}

	@Value("${folder.local}")
	// Initializer for tests. As WEB/bean, it gets from config
	private String data_root_folder = "/Users/pierrecote/Working/offgrid";

	public String getDataRootFolder()
	{
		return data_root_folder;
	}

	private String folderName;
	private String rootPath;

	public LocalFileManager(String folderName)
	{
		this.folderName = folderName;
	}

	public String getRootFolder()
	{
		return rootPath;
	}

	public boolean isValid(File file)
	{
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		rootPath = null == folderName
			? data_root_folder
			: data_root_folder + folderName;

		initNameMap(new File(getRootFolder()));
	}

	private void initNameMap(File file)
	{
		if (file.isFile())
		{
			if (isValid(file))
			{
				FileSystemResource resource = new FileSystemResource(file);
				addResource(resource);
			}
		}
		else // isDirectory
		{
			File[] children = file.listFiles();
			if (null != children)
			{
				for (File child : children)
				{
					initNameMap(child);
				}
			}
		}
	}
}
