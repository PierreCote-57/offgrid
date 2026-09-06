package com.lc.offgrid.common.pojo.external.bc.reststop;

import com.lc.offgrid.common.pojo.external.shared.FeatureProperties;

/**
 * The attributes of one rest area. The named columns are the ones the download carries today;
 * a column the layer gains later is still in the map and still reachable by name.
 */
public class RestStopProperties extends FeatureProperties
{
	private static final String	REST_AREA_ID					= "REST_AREA_ID";
	private static final String	CHRIS_REST_AREA_ID				= "CHRIS_REST_AREA_ID";
	private static final String	ADMIN_UNIT_CODE					= "ADMIN_UNIT_CODE";
	private static final String	ADMIN_UNIT_NAME					= "ADMIN_UNIT_NAME";
	private static final String	CROSS_SECTION_POSITION			= "CROSS_SECTION_POSITION";
	private static final String	REST_AREA_NAME					= "REST_AREA_NAME";
	private static final String	REST_AREA_NUMBER				= "REST_AREA_NUMBER";
	private static final String	REST_AREA_CLASS					= "REST_AREA_CLASS";
	private static final String	ASSOCIATED_NUMBERED_ROUTE		= "ASSOCIATED_NUMBERED_ROUTE";
	private static final String	DISTANCE_FROM_MUNICIPALITY		= "DISTANCE_FROM_MUNICIPALITY";
	private static final String	NUMBER_OF_TOILETS				= "NUMBER_OF_TOILETS";
	private static final String	TOILET_TYPE						= "TOILET_TYPE";
	private static final String	WHEELCHAIR_ACCESS_TOILET_IND	= "WHEELCHAIR_ACCESS_TOILET_IND";
	private static final String	NUMBER_OF_TABLES				= "NUMBER_OF_TABLES";
	private static final String	NUMBER_OF_STANDARD_BARRELS		= "NUMBER_OF_STANDARD_BARRELS";
	private static final String	NUMBER_OF_BEAR_PROOF_BARRELS	= "NUMBER_OF_BEAR_PROOF_BARRELS";
	private static final String	POWER_TYPE						= "POWER_TYPE";
	private static final String	POWER_RESPONSIBILITY			= "POWER_RESPONSIBILITY";
	private static final String	DIRECTION_OF_TRAFFIC			= "DIRECTION_OF_TRAFFIC";
	private static final String	ACCESS_RESTRICTION				= "ACCESS_RESTRICTION";
	private static final String	DECELERATION_LANE_IND			= "DECELERATION_LANE_IND";
	private static final String	ACCELERATION_LANE_IND			= "ACCELERATION_LANE_IND";
	private static final String	DIRECT_ACCESS_IND				= "DIRECT_ACCESS_IND";
	private static final String	ACCOM_COMMERCIAL_TRUCKS_IND		= "ACCOM_COMMERCIAL_TRUCKS_IND";
	private static final String	OPEN_YEAR_ROUND_IND				= "OPEN_YEAR_ROUND_IND";
	private static final String	OPEN_DATE						= "OPEN_DATE";
	private static final String	CLOSE_DATE						= "CLOSE_DATE";
	private static final String	CHRIS_ANCHOR_SECTION_ID			= "CHRIS_ANCHOR_SECTION_ID";
	private static final String	EVENT_LOCATION					= "EVENT_LOCATION";
	private static final String	HIGHWAY_NUMBER					= "HIGHWAY_NUMBER";
	private static final String	OBJECTID						= "OBJECTID";
	private static final String	SE_ANNO_CAD_DATA				= "SE_ANNO_CAD_DATA";

	public Integer getRestAreaId()
	{
		return getInteger(REST_AREA_ID);
	}

	public String getChrisRestAreaId()
	{
		return getString(CHRIS_REST_AREA_ID);
	}

	public String getAdminUnitCode()
	{
		return getString(ADMIN_UNIT_CODE);
	}

	public String getAdminUnitName()
	{
		return getString(ADMIN_UNIT_NAME);
	}

	public String getCrossSectionPosition()
	{
		return getString(CROSS_SECTION_POSITION);
	}

	public String getRestAreaName()
	{
		return getString(REST_AREA_NAME);
	}

	public String getRestAreaNumber()
	{
		return getString(REST_AREA_NUMBER);
	}

	public String getRestAreaClass()
	{
		return getString(REST_AREA_CLASS);
	}

	public String getAssociatedNumberedRoute()
	{
		return getString(ASSOCIATED_NUMBERED_ROUTE);
	}

	public String getDistanceFromMunicipality()
	{
		return getString(DISTANCE_FROM_MUNICIPALITY);
	}

	public Integer getNumberOfToilets()
	{
		return getInteger(NUMBER_OF_TOILETS);
	}

	public String getToiletType()
	{
		return getString(TOILET_TYPE);
	}

	public String getWheelchairAccessToiletInd()
	{
		return getString(WHEELCHAIR_ACCESS_TOILET_IND);
	}

	public Integer getNumberOfTables()
	{
		return getInteger(NUMBER_OF_TABLES);
	}

	public Integer getNumberOfStandardBarrels()
	{
		return getInteger(NUMBER_OF_STANDARD_BARRELS);
	}

	public Integer getNumberOfBearProofBarrels()
	{
		return getInteger(NUMBER_OF_BEAR_PROOF_BARRELS);
	}

	public String getPowerType()
	{
		return getString(POWER_TYPE);
	}

	public String getPowerResponsibility()
	{
		return getString(POWER_RESPONSIBILITY);
	}

	public String getDirectionOfTraffic()
	{
		return getString(DIRECTION_OF_TRAFFIC);
	}

	public String getAccessRestriction()
	{
		return getString(ACCESS_RESTRICTION);
	}

	public String getDecelerationLaneInd()
	{
		return getString(DECELERATION_LANE_IND);
	}

	public String getAccelerationLaneInd()
	{
		return getString(ACCELERATION_LANE_IND);
	}

	public String getDirectAccessInd()
	{
		return getString(DIRECT_ACCESS_IND);
	}

	public String getAccomCommercialTrucksInd()
	{
		return getString(ACCOM_COMMERCIAL_TRUCKS_IND);
	}

	public String getOpenYearRoundInd()
	{
		return getString(OPEN_YEAR_ROUND_IND);
	}

	public String getOpenDate()
	{
		return getString(OPEN_DATE);
	}

	public String getCloseDate()
	{
		return getString(CLOSE_DATE);
	}

	public Integer getChrisAnchorSectionId()
	{
		return getInteger(CHRIS_ANCHOR_SECTION_ID);
	}

	public Double getEventLocation()
	{
		return getDouble(EVENT_LOCATION);
	}

	public String getHighwayNumber()
	{
		return getString(HIGHWAY_NUMBER);
	}

	public Integer getObjectId()
	{
		return getInteger(OBJECTID);
	}

	public String getSeAnnoCadData()
	{
		return getString(SE_ANNO_CAD_DATA);
	}
}
