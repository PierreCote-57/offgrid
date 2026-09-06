package com.lc.offgrid.common.pojo.part;

import java.util.TreeMap;

/**
 * What the province publishes about a lake: its identifier in the fisheries registry, its
 * surface area, the bathymetric charts, and the name it is stocked under.
 */
public class FishingReferences
{
	private String					bcIdentifier;
	private Double					areaKm2;
	private TreeMap<String, String>	lakeChartMap;
	private String					stockingName;

	public String getBcIdentifier()
	{
		return bcIdentifier;
	}

	public Double getAreaKm2()
	{
		return areaKm2;
	}

	public TreeMap<String, String> getLakeChartMap()
	{
		return lakeChartMap;
	}

	public String getStockingName()
	{
		return stockingName;
	}
}
