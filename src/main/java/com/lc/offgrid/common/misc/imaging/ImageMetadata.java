/*
 * Copyright (c) 2026 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.common.misc.imaging;

import com.lc.basics.container.AbstractContainer;
import com.lc.offgrid.common.misc.geography.point.LatLonPoint;
import com.lc.offgrid.common.misc.geography.point.LatLonPointPojo;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * What one image file says about itself, flattened out of the EXIF directories. Everything is
 * optional in a file, so a value that is not there reads as null, or as 0 for a dimension.
 *
 * @author Pierre
 */
public class ImageMetadata
{
	private File				file;
	private String				imageName;
	private Date				dateTaken;
	private int					orientation;
	private int					width;
	private int					height;
	private String				cameraMake;
	private String				cameraModel;
	private Double				latitude;
	private Double				longitude;
	private Double				cameraDirection;
	private String				cameraDirectionRef;
	private String				exposureTime;
	private Double				fNumber;
	private Integer				iso;
	private Double				exposureBias;
	private String				flash;
	private Double				focalLength;
	private Integer				focalLength35mm;
	private String				lensModel;
	private Map<String, String>	tagMap;
	private MediaType			mediaType;

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public String getImageName()
	{
		return imageName;
	}
	public void setImageName(String imageName)
	{
		this.imageName = imageName;
	}

	/** When the shutter fired, null when the file does not say. */
	public Date getDateTaken()
	{
		return dateTaken;
	}
	public void setDateTaken(Date dateTaken)
	{
		this.dateTaken = dateTaken;
	}

	/** EXIF orientation, 1 through 8. */
	public int getOrientation()
	{
		return orientation;
	}
	public void setOrientation(int orientation)
	{
		this.orientation = orientation;
	}

	/** Pixel width as stored. A rotated orientation means this is the height on screen. */
	public int getWidth()
	{
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}

	public String getCameraMake()
	{
		return cameraMake;
	}
	public void setCameraMake(String cameraMake)
	{
		this.cameraMake = cameraMake;
	}

	public String getCameraModel()
	{
		return cameraModel;
	}
	public void setCameraModel(String cameraModel)
	{
		this.cameraModel = cameraModel;
	}

	/** Where the picture was taken, null when the file carries no GPS. */
	public Double getLatitude()
	{
		return latitude;
	}
	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}

	public Double getLongitude()
	{
		return longitude;
	}
	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}

	/**
	 * Where the camera was pointing, in degrees clockwise from north. Null when the file does
	 * not say — most phones write it, most cameras do not.
	 */
	public Double getCameraDirection()
	{
		return cameraDirection;
	}
	public void setCameraDirection(Double cameraDirection)
	{
		this.cameraDirection = cameraDirection;
	}

	/** Whether the direction is against true or magnetic north. */
	public String getCameraDirectionRef()
	{
		return cameraDirectionRef;
	}
	public void setCameraDirectionRef(String cameraDirectionRef)
	{
		this.cameraDirectionRef = cameraDirectionRef;
	}

	/** Shutter speed as photographers read it, "1/250 sec". */
	public String getExposureTime()
	{
		return exposureTime;
	}
	public void setExposureTime(String exposureTime)
	{
		this.exposureTime = exposureTime;
	}

	/** Aperture as the f number itself, 2.8 rather than "f/2.8". */
	public Double getFNumber()
	{
		return fNumber;
	}
	public void setFNumber(Double fNumber)
	{
		this.fNumber = fNumber;
	}

	public Integer getIso()
	{
		return iso;
	}
	public void setIso(Integer iso)
	{
		this.iso = iso;
	}

	/** Exposure compensation in stops, 0 when the shot was not pushed either way. */
	public Double getExposureBias()
	{
		return exposureBias;
	}
	public void setExposureBias(Double exposureBias)
	{
		this.exposureBias = exposureBias;
	}

	/** What the flash did, in words — the EXIF value is a bit field. */
	public String getFlash()
	{
		return flash;
	}
	public void setFlash(String flash)
	{
		this.flash = flash;
	}

	/** Focal length in mm, as the lens actually is. */
	public Double getFocalLength()
	{
		return focalLength;
	}
	public void setFocalLength(Double focalLength)
	{
		this.focalLength = focalLength;
	}

	/** The same focal length expressed for 35mm film, which is the one people compare. */
	public Integer getFocalLength35mm()
	{
		return focalLength35mm;
	}
	public void setFocalLength35mm(Integer focalLength35mm)
	{
		this.focalLength35mm = focalLength35mm;
	}

	public String getLensModel()
	{
		return lensModel;
	}
	public void setLensModel(String lensModel)
	{
		this.lensModel = lensModel;
	}

	public MediaType getMediaType()
	{
		return mediaType;
	}

	public void setMediaType(MediaType mediaType)
	{
		this.mediaType = mediaType;
	}

	/** Every tag the file carries, keyed "Directory/Tag name" — the escape hatch. */
	public Map<String, String> getTagMap()
	{
		return tagMap;
	}
	public void setTagMap(Map<String, String> tagMap)
	{
		this.tagMap = tagMap;
	}

	public LatLonPoint getGeoPoint()
	{
		LatLonPointPojo ppint = new LatLonPointPojo(getLatitude(), getLongitude());
		return ppint;
	}

	public String getDetectedType()
	{
		return getTagMap().get("File Type/Detected File Type Name");
	}

	public boolean isLegal()
	{
		MediaType mediaType = getMediaType();
		String detected = getDetectedType();

		boolean isLegal = mediaType.getSubtype().equalsIgnoreCase(detected);
		return isLegal;
	}
}
