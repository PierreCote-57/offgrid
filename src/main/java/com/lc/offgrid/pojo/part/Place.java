package com.lc.offgrid.pojo.part;

import com.google.gson.annotations.SerializedName;

/**
 * A coordinate that can be drawn and described: a page's location, and a pin on a map. The two
 * are the same shape, so one class serves both.
 */
public class Place extends Point
{
	/**
	 * The marker drawn for a place. NONE carries the empty string, which is what a file writes
	 * when it has coordinates but no opinion about the marker.
	 */
	public enum MapIcon
	{
		@SerializedName("")				NONE,
		@SerializedName("lake")			LAKE,
		@SerializedName("tent")			TENT,
		@SerializedName("campground")	CAMPGROUND,
		@SerializedName("picnic")		PICNIC,
		@SerializedName("park")			PARK,
		@SerializedName("home")			HOME,
		@SerializedName("outhouse")		OUTHOUSE
	}

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
