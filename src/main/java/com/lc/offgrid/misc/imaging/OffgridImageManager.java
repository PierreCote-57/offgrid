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

	/**
	 * The image's metadata, read from the file each time. There is no cache, so a caller that
	 * asks per request reads the disk per request.
	 */
	public ImageMetadata getImageMetadata(String name)
	{
		File file = getFile(name);
		ImageMetadata imageMetadata = null == file
				? null
				: EXTRACTOR.getImageMetadata(file);
		return imageMetadata;
	}
}
