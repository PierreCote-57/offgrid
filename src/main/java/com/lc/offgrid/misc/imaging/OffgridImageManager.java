package com.lc.offgrid.misc.imaging;

import com.lc.offgrid.misc.files.AbstractFileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

@Component
public class OffgridImageManager extends AbstractFileManager
{
	private static final ImageMetadataExtractor EXTRACTOR = new ImageMetadataExtractor();

	@Value("${folder.image}")
	// Initializer for tests. As WEB/bean, it gets from config
	private String folderImage = "/Users/pierrecote/Pictures/offgrid";

	@Override
	public String getRootFolder()
	{
		return folderImage;
	}

	@Override
	public boolean isValid(File file)
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

	public ImageMetadata getImageMetadata(String name)
	{
		File file = getFile(name);
		ImageMetadata imageMetadata = null == file
				? null
				: EXTRACTOR.getImageMetadata(file);
		return imageMetadata;
	}
}
