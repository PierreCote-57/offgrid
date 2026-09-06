package com.lc.offgrid.common.pojo.external.bc.reststop;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.common.pojo.external.shared.FeatureCollectionCrs;

import java.util.List;

/**
 * The whole bc_reststop.json download: a GeoJSON FeatureCollection from the DataBC WFS layer
 * WHSE_IMAGERY_AND_BASE_MAPS.MOT_REST_AREAS_SP.
 */
public class RestStopFile
{
	private String					type;
	@SerializedName("features")
	private List<RestStopFeature>	featureList;
	private Integer					totalFeatures;
	private Integer					numberMatched;
	private Integer					numberReturned;
	private String					timeStamp;
	private FeatureCollectionCrs	crs;

	public String getType()
	{
		return type;
	}

	public List<RestStopFeature> getFeatureList()
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
