package com.lc.offgrid.common.pojo.external.bc.offramp;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.common.pojo.external.shared.FeatureCollectionCrs;

import java.util.List;

/**
 * The whole bc_offramp.json download: a GeoJSON FeatureCollection from the DataBC WFS layer
 * WHSE_BASEMAPPING.DRA_DGTL_ROAD_ATLAS_MPAR_SP. The query took anything carrying an exit
 * number OR named an offramp, so onramps, flyovers and mainline segments are in here too.
 */
public class OfframpFile
{
	private String					type;
	@SerializedName("features")
	private List<OfframpFeature>	featureList;
	private Integer					totalFeatures;
	private Integer					numberMatched;
	private Integer					numberReturned;
	private String					timeStamp;
	private FeatureCollectionCrs	crs;

	public String getType()
	{
		return type;
	}

	public List<OfframpFeature> getFeatureList()
	{
		return featureList;
	}

	public Integer getTotalFeatures()
	{
		return totalFeatures;
	}

	public Integer getNumberMatched()
	{
		return numberMatched;
	}

	public Integer getNumberReturned()
	{
		return numberReturned;
	}

	public String getTimeStamp()
	{
		return timeStamp;
	}

	public FeatureCollectionCrs getCrs()
	{
		return crs;
	}
}
