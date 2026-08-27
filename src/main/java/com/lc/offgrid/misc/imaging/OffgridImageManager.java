package com.lc.offgrid.misc.imaging;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

@Component
public class OffgridImageManager implements InitializingBean
{
	private static final ImageMetadataExtractor EXTRACTOR = new ImageMetadataExtractor();

	@Value("${folder.image}")
	// Initializer for tests. As WEB/bean, it gets from config
	private String folderImage = "/Users/pierrecote/Pictures/offgrid";

	private final Map<String, File> nameMap = new TreeMap<>();

	public String getFolderImage()
	{
		return folderImage;
	}

	public Map<String, File> getNameMap()
	{
		return nameMap;
	}

	public File getFile(String name)
	{
		return nameMap.get(name);
	}

	public ImageMetadata getImageMetadata(String name)
	{
		File file = getFile(name);
		ImageMetadata imageMetadata = EXTRACTOR.getImageMetadata(file);
		return imageMetadata;
	}


	@Override
	public void afterPropertiesSet() throws Exception
	{
		initNameMap(new File(getFolderImage()));
	}

	private void initNameMap(File file)
	{
		if (file.isFile())
		{
			if (isImage(file))
			{
				String filename = file.getName();
				String imageName = filename.substring(0, filename.lastIndexOf('.'));
				nameMap.put(imageName, file);
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

	private boolean isImage(File file)
	{
		try
		{
			ImageMetadata metadata = EXTRACTOR.getImageMetadata(file);
			return metadata != null;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
