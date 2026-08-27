package com.lc.offgrid.pojo.external.overpass.amenities;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.pojo.external.shared.OverpassOsm3s;

import java.util.List;

/**
 * The whole bc_exits_amenities.json download: everything Overpass found within 1 km of a BC
 * motorway junction. The junctions themselves are bc_exits.json, and nothing here points back
 * at them — the radius was the query, not a stored link.
 */
public class AmenityFile
{
	private Double					version;
	private String					generator;
	private OverpassOsm3s			osm3s;
	@SerializedName("elements")
	private List<AmenityElement>	elementList;

	public Double getVersion()
	{
		return version;
	}

	public String getGenerator()
	{
		return generator;
	}

	public OverpassOsm3s getOsm3s()
	{
		return osm3s;
	}

	public List<AmenityElement> getElementList()
	{
		return elementList;
	}
}
