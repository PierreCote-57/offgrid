package com.lc.offgrid.pojo.external.overpass.exits;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.pojo.external.shared.OverpassOsm3s;

import java.util.List;

/**
 * The whole bc_exits.json download: Overpass JSON holding every motorway junction node in BC.
 */
public class ExitFile
{
	private Double				version;
	private String				generator;
	private OverpassOsm3s		osm3s;
	@SerializedName("elements")
	private List<ExitElement>	elementList;

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

	public List<ExitElement> getElementList()
	{
		return elementList;
	}
}
