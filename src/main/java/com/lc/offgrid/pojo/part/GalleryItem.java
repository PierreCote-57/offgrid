package com.lc.offgrid.pojo.part;

/**
 * One photo in a gallery. A map pin refers to it as galleryKey/id.
 */
public class GalleryItem
{
	private String	id;
	private String	img;
	private String	label;

	public String getId()
	{
		return id;
	}

	public String getImg()
	{
		return img;
	}

	public String getLabel()
	{
		return label;
	}
}
