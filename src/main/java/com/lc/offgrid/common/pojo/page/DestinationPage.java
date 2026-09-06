package com.lc.offgrid.common.pojo.page;

import com.lc.offgrid.common.pojo.part.Access;
import com.lc.offgrid.common.pojo.part.GoogleMap;
import com.lc.offgrid.common.pojo.part.Place;
import java.util.Map;

/**
 * A place a reader can go to. It sits somewhere, and it can show maps of itself. What the
 * approach is like is part of that; a destination reached along its edge rather than at one
 * spot leaves access null.
 */
public class DestinationPage extends PageData
{
	private Place					location;
	private Map<String, GoogleMap>	googleMap;
	private Access					access;

	public Place getLocation()
	{
		return location;
	}

	public Map<String, GoogleMap> getGoogleMap()
	{
		return googleMap;
	}

	public Access getAccess()
	{
		return access;
	}
}
