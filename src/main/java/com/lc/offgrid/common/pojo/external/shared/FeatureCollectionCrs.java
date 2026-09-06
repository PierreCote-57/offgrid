package com.lc.offgrid.common.pojo.external.shared;

/**
 * The coordinate reference system a WFS collection was served in.
 */
public class FeatureCollectionCrs
{
	/**
	 * The name of the system, such as urn:ogc:def:crs:EPSG::4326.
	 */
	public static class CrsProperties
	{
		private String	name;

		public String getName()
		{
			return name;
		}
	}

	private String			type;
	private CrsProperties	properties;

	public String getType()
	{
		return type;
	}

	public CrsProperties getProperties()
	{
		return properties;
	}
}
