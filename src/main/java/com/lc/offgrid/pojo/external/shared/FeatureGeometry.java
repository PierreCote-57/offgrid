package com.lc.offgrid.pojo.external.shared;

import com.google.gson.annotations.SerializedName;

/**
 * The shape a GeoJSON feature sits on. The coordinates come back as Gson builds them: a Point
 * is an ArrayList of two Double, [lng, lat]; a LineString is an ArrayList of those.
 */
public class FeatureGeometry
{
	/**
	 * The seven shapes GeoJSON defines. Gson returns null for anything it cannot match, so
	 * they are all here whether or not a download uses them.
	 */
	public enum GeometryType
	{
		@SerializedName("Point")				POINT,
		@SerializedName("MultiPoint")			MULTI_POINT,
		@SerializedName("LineString")			LINE_STRING,
		@SerializedName("MultiLineString")		MULTI_LINE_STRING,
		@SerializedName("Polygon")				POLYGON,
		@SerializedName("MultiPolygon")			MULTI_POLYGON,
		@SerializedName("GeometryCollection")	GEOMETRY_COLLECTION
	}

	private GeometryType	type;
	private Object			coordinates;

	public GeometryType getType()
	{
		return type;
	}

	public Object getCoordinates()
	{
		return coordinates;
	}
}
