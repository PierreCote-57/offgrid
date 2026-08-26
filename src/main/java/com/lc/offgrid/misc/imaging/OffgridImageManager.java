package com.lc.offgrid.misc.imaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class OffgridImageManager
{
	@Value("${folder.image}")
	private String		folderImage;

	private static final ImageMetadataExtractor EXTRACTOR = new ImageMetadataExtractor();

	public String getFolderImage()
	{
		return folderImage;
	}

	/**
	 * The file one image name refers to. Public because callers reading the bytes want the same
	 * answer this class used.
	 */
	public File getImageFile(String imageName)
	{
		File imageFile = new File(getFolderImage() + "/" + imageName);
		return imageFile;
	}

	public ImageMetadata getImageMetadata(String name)
	{
		File file = getImageFile(name);
		ImageMetadata imageMetadata = EXTRACTOR.getImageMetadata(file);
		return imageMetadata;
	}

}
