package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * What the province publishes about a lake: its identifier in the fisheries registry, its
 * surface area, the bathymetric charts, and the name it is stocked under.
 */
public class FishingReferences
{
	private String			bcIdentifier;
	private Double			areaKm2;
	private List<LakeChart>	lakeChartList;
	private String			stockingName;

	public String getBcIdentifier()
	{
		return bcIdentifier;
	}

	public Double getAreaKm2()
	{
		return areaKm2;
	}

	public List<LakeChart> getLakeChartList()
	{
		return lakeChartList;
	}

	public String getStockingName()
	{
		return stockingName;
	}
}
