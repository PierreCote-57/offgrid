package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * One map on a page. It names its subject in one of three ways, in order of precedence: a page
 * filename, a Google location id, or its own coordinates. Pins are drawn on top.
 */
public class GoogleMap extends Point
{
	private String		file;
	private String		locationId;
	private Integer		zoom;
	private List<Place>	pinList;

	public String getFile()
	{
		return file;
	}

	public String getLocationId()
	{
		return locationId;
	}

	public Integer getZoom()
	{
		return zoom;
	}

	public List<Place> getPinList()
	{
		return pinList;
	}
}
