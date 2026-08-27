package com.lc.offgrid.pojo.external.bc.reststop;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.pojo.external.shared.FeatureGeometry;

/**
 * One rest area in the download: a GeoJSON Feature carrying a point and the WFS attributes.
 */
public class RestStopFeature
{
	private String				type;
	private String				id;
	private FeatureGeometry		geometry;
	@SerializedName("geometry_name")
	private String				geometryName;
	private RestStopProperties	properties;

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

	public RestStopProperties getProperties()
	{
		return properties;
	}
}
