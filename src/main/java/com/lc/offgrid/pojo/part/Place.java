package com.lc.offgrid.pojo.part;

/**
 * A coordinate that can be drawn and described: a page's location, and a pin on a map. The two
 * are the same shape, so one class serves both.
 */
public class Place extends Point
{
	private String	label;
	private MapIcon	icon;
	private String	img;
	private String	url;

	public String getLabel()
	{
		return label;
	}

	public MapIcon getIcon()
	{
		return icon;
	}

	public String getImg()
	{
		return img;
	}

	public String getUrl()
	{
		return url;
	}
}
