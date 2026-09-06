package com.lc.offgrid.common.pojo.external.bc.offramp;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.common.pojo.external.shared.FeatureGeometry;

/**
 * One road segment in the download: a GeoJSON Feature whose geometry is the line the segment
 * runs along.
 */
public class OfframpFeature
{
	private String				type;
	private String				id;
	private FeatureGeometry		geometry;
	@SerializedName("geometry_name")
	private String				geometryName;
	private OfframpProperties	properties;

	public String getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public FeatureGeometry getGeometry()
	{
		return geometry;
	}

	public String getGeometryName()
	{
		return geometryName;
	}

	public OfframpProperties getProperties()
	{
		return properties;
	}
}
