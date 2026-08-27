package com.lc.offgrid.pojo.external.shared;

import com.google.gson.annotations.SerializedName;

/**
 * When Overpass built the answer, and the licence the data carries.
 */
public class OverpassOsm3s
{
	@SerializedName("timestamp_osm_base")
	private String	timestampOsmBase;
	@SerializedName("timestamp_areas_base")
	private String	timestampAreasBase;
	private String	copyright;

	public String getTimestampOsmBase()
	{
		return timestampOsmBase;
	}

	public String getTimestampAreasBase()
	{
		return timestampAreasBase;
	}

	public String getCopyright()
	{
		return copyright;
	}
}
