package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.GoogleMap;
import com.lc.offgrid.pojo.part.Place;
import java.util.Map;

/**
 * A place a reader can go to. It sits somewhere, and it can show maps of itself.
 */
public class DestinationPage extends PageData
{
	private Place					location;
	private Map<String, GoogleMap>	googleMap;

	public Place getLocation()
	{
		return location;
	}

	public Map<String, GoogleMap> getGoogleMap()
	{
		return googleMap;
	}
}
