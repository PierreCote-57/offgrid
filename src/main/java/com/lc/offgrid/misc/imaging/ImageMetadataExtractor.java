/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.imaging;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.lc.basics.tools.misc.BasicRuntimeException;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads what an image file says about itself — EXIF, IPTC, XMP and the rest — and hands it back
 * flattened into an ImageMetadata. Images are named the way the site names them, and this is the
 * one place that knows where on disk that name lands.
 *
 * @author Pierre
 */
public class ImageMetadataExtractor
{
	/** EXIF orientation 1 = stored the way it is meant to be viewed. */
	public static final int		ORIENTATION_NORMAL		= 1;

	/**
	 * Everything one image file says about itself. A file that cannot be read at all throws; a
	 * file that simply carries no EXIF answers with an ImageMetadata whose values are empty.
	 */
	public ImageMetadata getImageMetadata(File file)
	{
		Metadata	metadata;

		try
		{
			metadata = ImageMetadataReader.readMetadata(file);
		}
		catch (Exception e)
		{
			throw new BasicRuntimeException(e, "Error reading image metadata: %s", file);
		}

		ImageMetadata answer = new ImageMetadata();

		answer.setFile(file);
		answer.setImageName(file.getName());
		answer.setDateTaken(dateTakenOf(metadata));
		answer.setOrientation(orientationOf(metadata));
		answer.setWidth(intTag(metadata, JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH));
		answer.setHeight(intTag(metadata, JpegDirectory.class, JpegDirectory.TAG_IMAGE_HEIGHT));
		answer.setCameraMake(stringTag(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_MAKE));
		answer.setCameraModel(stringTag(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_MODEL));
		answer.setTagMap(tagMapOf(metadata));

		answer.setExposureTime(descriptionTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
		answer.setFNumber(doubleTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_FNUMBER));
		answer.setIso(integerTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
		answer.setExposureBias(doubleTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_EXPOSURE_BIAS));
		answer.setFlash(descriptionTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_FLASH));
		answer.setFocalLength(doubleTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
		answer.setFocalLength35mm(integerTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH));
		answer.setLensModel(stringTag(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_LENS_MODEL));

		answer.setCameraDirection(doubleTag(metadata, GpsDirectory.class, GpsDirectory.TAG_IMG_DIRECTION));
		answer.setCameraDirectionRef(descriptionTag(metadata, GpsDirectory.class, GpsDirectory.TAG_IMG_DIRECTION_REF));

		GeoLocation geoLocation = geoLocationOf(metadata);
		if (null != geoLocation)
		{
			answer.setLatitude(geoLocation.getLatitude());
			answer.setLongitude(geoLocation.getLongitude());
		}

		answer.setMediaType(mediaTypeOf(answer.getImageName()));
		return answer;
	}

	/**
	 * When the shutter fired, from the EXIF original date, falling back to the file's own EXIF
	 * date. Null when neither is present.
	 */
	private Date dateTakenOf(Metadata metadata)
	{
		Date				answer			= null;
		ExifSubIFDDirectory	subDirectory	= metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

		if (null != subDirectory)
		{
			answer = subDirectory.getDateOriginal();
		}
		if (null == answer)
		{
			ExifIFD0Directory mainDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (null != mainDirectory)
			{
				answer = mainDirectory.getDate(ExifIFD0Directory.TAG_DATETIME);
			}
		}
		return answer;
	}

	/**
	 * The EXIF orientation, 1 through 8. A file that does not say is reported as normal, since
	 * that is how a viewer treats it.
	 */
	private int orientationOf(Metadata metadata)
	{
		int	answer = intTag(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_ORIENTATION);

		if (0 == answer)
		{
			answer = ORIENTATION_NORMAL;
		}
		return answer;
	}

	/**
	 * Where the picture was taken, null when the file carries no GPS or carries it unset.
	 */
	private GeoLocation geoLocationOf(Metadata metadata)
	{
		GeoLocation		answer			= null;
		GpsDirectory	gpsDirectory	= metadata.getFirstDirectoryOfType(GpsDirectory.class);

		if (null != gpsDirectory)
		{
			GeoLocation geoLocation = gpsDirectory.getGeoLocation();
			if (null != geoLocation && !geoLocation.isZero())
			{
				answer = geoLocation;
			}
		}
		return answer;
	}

	/**
	 * Every tag in the file, keyed "Directory/Tag name", with the human-readable description as
	 * the value. This is the dump — useful to see what a given camera actually wrote.
	 */
	private Map<String, String> tagMapOf(Metadata metadata)
	{
		Map<String, String> answer = new LinkedHashMap<>();

		for (Directory directory : metadata.getDirectories())
		{
			for (Tag tag : directory.getTags())
			{
				answer.put(directory.getName() + "/" + tag.getTagName(), tag.getDescription());
			}
		}
		return answer;
	}

	/**
	 * One integer tag out of one directory, 0 when the directory or the tag is absent.
	 */
	private <T extends Directory> int intTag(Metadata metadata, Class<T> clazz, int tagType)
	{
		int	answer		= 0;
		T	directory	= metadata.getFirstDirectoryOfType(clazz);

		if (null != directory && directory.containsTag(tagType))
		{
			answer = directory.getInteger(tagType);
		}
		return answer;
	}

	/**
	 * One string tag out of one directory, null when the directory or the tag is absent.
	 */
	private <T extends Directory> String stringTag(Metadata metadata, Class<T> clazz, int tagType)
	{
		String	answer		= null;
		T		directory	= metadata.getFirstDirectoryOfType(clazz);

		if (null != directory)
		{
			answer = directory.getString(tagType);
		}
		return answer;
	}

	/**
	 * One decimal tag out of one directory, null when the directory or the tag is absent.
	 */
	private <T extends Directory> Double doubleTag(Metadata metadata, Class<T> clazz, int tagType)
	{
		Double	answer		= null;
		T		directory	= metadata.getFirstDirectoryOfType(clazz);

		if (null != directory && directory.containsTag(tagType))
		{
			answer = directory.getDoubleObject(tagType);
		}
		return answer;
	}

	/**
	 * One whole-number tag out of one directory, null when the directory or the tag is absent.
	 * This is the nullable twin of intTag, for values where 0 is a real answer.
	 */
	private <T extends Directory> Integer integerTag(Metadata metadata, Class<T> clazz, int tagType)
	{
		Integer	answer		= null;
		T		directory	= metadata.getFirstDirectoryOfType(clazz);

		if (null != directory && directory.containsTag(tagType))
		{
			answer = directory.getInteger(tagType);
		}
		return answer;
	}

	/**
	 * One tag rendered the way a person reads it — "1/250 sec", "Flash did not fire" — rather
	 * than the raw stored value. Null when the directory or the tag is absent.
	 */
	private <T extends Directory> String descriptionTag(Metadata metadata, Class<T> clazz, int tagType)
	{
		String	answer		= null;
		T		directory	= metadata.getFirstDirectoryOfType(clazz);

		if (null != directory && directory.containsTag(tagType))
		{
			answer = directory.getDescription(tagType);
		}
		return answer;
	}

	/**
	 * The media type an image filename implies, from its extension. Anything unrecognized is
	 * served as raw bytes.
	 */
	public static MediaType mediaTypeOf(String imageName)
	{
		String		lowerName	= imageName.toLowerCase();
		MediaType	answer		= MediaType.APPLICATION_OCTET_STREAM;

		if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg"))
		{
			answer = MediaType.IMAGE_JPEG;
		}
		else if (lowerName.endsWith(".png"))
		{
			answer = MediaType.IMAGE_PNG;
		}
		else if (lowerName.endsWith(".gif"))
		{
			answer = MediaType.IMAGE_GIF;
		}
		else if (lowerName.endsWith(".webp"))
		{
			answer = MediaType.valueOf("image/webp");
		}
		return answer;
	}
}
