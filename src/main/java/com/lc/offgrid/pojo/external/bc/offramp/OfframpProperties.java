package com.lc.offgrid.pojo.external.bc.offramp;

import com.lc.offgrid.pojo.external.shared.FeatureProperties;

/**
 * The Digital Road Atlas attributes of one road segment. The named columns are the ones the
 * download carries today; a column the layer gains later is still in the map.
 */
public class OfframpProperties extends FeatureProperties
{
	private static final String	DIGITAL_ROAD_ATLAS_LINE_ID	= "DIGITAL_ROAD_ATLAS_LINE_ID";
	private static final String	FEATURE_TYPE				= "FEATURE_TYPE";
	private static final String	HIGHWAY_EXIT_NUMBER			= "HIGHWAY_EXIT_NUMBER";
	private static final String	HIGHWAY_ROUTE_NUMBER		= "HIGHWAY_ROUTE_NUMBER";
	private static final String	SEGMENT_LENGTH_2D			= "SEGMENT_LENGTH_2D";
	private static final String	SEGMENT_LENGTH_3D			= "SEGMENT_LENGTH_3D";
	private static final String	ROAD_NAME_ALIAS1			= "ROAD_NAME_ALIAS1";
	private static final String	ROAD_NAME_ALIAS2			= "ROAD_NAME_ALIAS2";
	private static final String	ROAD_NAME_ALIAS3			= "ROAD_NAME_ALIAS3";
	private static final String	ROAD_NAME_ALIAS4			= "ROAD_NAME_ALIAS4";
	private static final String	ROAD_NAME_FULL				= "ROAD_NAME_FULL";
	private static final String	ROAD_SURFACE				= "ROAD_SURFACE";
	private static final String	ROAD_CLASS					= "ROAD_CLASS";
	private static final String	NUMBER_OF_LANES				= "NUMBER_OF_LANES";
	private static final String	DATA_CAPTURE_DATE			= "DATA_CAPTURE_DATE";
	private static final String	FEATURE_CODE				= "FEATURE_CODE";
	private static final String	OBJECTID					= "OBJECTID";
	private static final String	SE_ANNO_CAD_DATA			= "SE_ANNO_CAD_DATA";
	private static final String	FEATURE_LENGTH_M			= "FEATURE_LENGTH_M";

	public Integer getDigitalRoadAtlasLineId()
	{
		return getInteger(DIGITAL_ROAD_ATLAS_LINE_ID);
	}

	public String getFeatureType()
	{
		return getString(FEATURE_TYPE);
	}

	public String getHighwayExitNumber()
	{
		return getString(HIGHWAY_EXIT_NUMBER);
	}

	public String getHighwayRouteNumber()
	{
		return getString(HIGHWAY_ROUTE_NUMBER);
	}

	public Double getSegmentLength2d()
	{
		return getDouble(SEGMENT_LENGTH_2D);
	}

	public Double getSegmentLength3d()
	{
		return getDouble(SEGMENT_LENGTH_3D);
	}

	public String getRoadNameAlias1()
	{
		return getString(ROAD_NAME_ALIAS1);
	}

	public String getRoadNameAlias2()
	{
		return getString(ROAD_NAME_ALIAS2);
	}

	public String getRoadNameAlias3()
	{
		return getString(ROAD_NAME_ALIAS3);
	}

	public String getRoadNameAlias4()
	{
		return getString(ROAD_NAME_ALIAS4);
	}

	public String getRoadNameFull()
	{
		return getString(ROAD_NAME_FULL);
	}

	public String getRoadSurface()
	{
		return getString(ROAD_SURFACE);
	}

	public String getRoadClass()
	{
		return getString(ROAD_CLASS);
	}

	public Integer getNumberOfLanes()
	{
		return getInteger(NUMBER_OF_LANES);
	}

	public String getDataCaptureDate()
	{
		return getString(DATA_CAPTURE_DATE);
	}

	public String getFeatureCode()
	{
		return getString(FEATURE_CODE);
	}

	public Integer getObjectId()
	{
		return getInteger(OBJECTID);
	}

	public String getSeAnnoCadData()
	{
		return getString(SE_ANNO_CAD_DATA);
	}

	public Double getFeatureLengthM()
	{
		return getDouble(FEATURE_LENGTH_M);
	}
}
