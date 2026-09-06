package com.lc.offgrid.common.pojo.external.overpass.amenities;

import com.lc.offgrid.common.pojo.external.shared.FeatureProperties;

/**
 * The OSM tags on one amenity. The download carries 296 distinct tag keys; the ones named
 * here are the ones worth a method, and every other tag is still in the map, reachable by
 * its own name.
 */
public class AmenityTags extends FeatureProperties
{
	private static final String	AMENITY				= "amenity";
	private static final String	SHOP				= "shop";
	private static final String	TOURISM				= "tourism";
	private static final String	NAME				= "name";
	private static final String	OFFICIAL_NAME		= "official_name";
	private static final String	ALT_NAME			= "alt_name";
	private static final String	BRAND				= "brand";
	private static final String	OPERATOR			= "operator";
	private static final String	CUISINE				= "cuisine";
	private static final String	WEBSITE				= "website";
	private static final String	PHONE				= "phone";
	private static final String	EMAIL				= "email";
	private static final String	OPENING_HOURS		= "opening_hours";
	private static final String	ADDR_HOUSENUMBER	= "addr:housenumber";
	private static final String	ADDR_STREET			= "addr:street";
	private static final String	ADDR_UNIT			= "addr:unit";
	private static final String	ADDR_CITY			= "addr:city";
	private static final String	ADDR_PROVINCE		= "addr:province";
	private static final String	ADDR_POSTCODE		= "addr:postcode";
	private static final String	TAKEAWAY			= "takeaway";
	private static final String	DRIVE_THROUGH		= "drive_through";
	private static final String	OUTDOOR_SEATING		= "outdoor_seating";
	private static final String	INDOOR_SEATING		= "indoor_seating";
	private static final String	INTERNET_ACCESS		= "internet_access";
	private static final String	WHEELCHAIR			= "wheelchair";
	private static final String	TOILETS_DISPOSAL	= "toilets:disposal";
	private static final String	ACCESS				= "access";
	private static final String	FEE					= "fee";
	private static final String	SMOKING				= "smoking";
	private static final String	LEVEL				= "level";
	private static final String	BUILDING			= "building";
	private static final String	CHECK_DATE			= "check_date";
	private static final String	SOURCE				= "source";

	public String getAmenity()
	{
		return getString(AMENITY);
	}

	public String getShop()
	{
		return getString(SHOP);
	}

	public String getTourism()
	{
		return getString(TOURISM);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public String getOfficialName()
	{
		return getString(OFFICIAL_NAME);
	}

	public String getAltName()
	{
		return getString(ALT_NAME);
	}

	public String getBrand()
	{
		return getString(BRAND);
	}

	public String getOperator()
	{
		return getString(OPERATOR);
	}

	public String getCuisine()
	{
		return getString(CUISINE);
	}

	public String getWebsite()
	{
		return getString(WEBSITE);
	}

	public String getPhone()
	{
		return getString(PHONE);
	}

	public String getEmail()
	{
		return getString(EMAIL);
	}

	public String getOpeningHours()
	{
		return getString(OPENING_HOURS);
	}

	public String getAddrHousenumber()
	{
		return getString(ADDR_HOUSENUMBER);
	}

	public String getAddrStreet()
	{
		return getString(ADDR_STREET);
	}

	public String getAddrUnit()
	{
		return getString(ADDR_UNIT);
	}

	public String getAddrCity()
	{
		return getString(ADDR_CITY);
	}

	public String getAddrProvince()
	{
		return getString(ADDR_PROVINCE);
	}

	public String getAddrPostcode()
	{
		return getString(ADDR_POSTCODE);
	}

	public String getTakeaway()
	{
		return getString(TAKEAWAY);
	}

	public String getDriveThrough()
	{
		return getString(DRIVE_THROUGH);
	}

	public String getOutdoorSeating()
	{
		return getString(OUTDOOR_SEATING);
	}

	public String getIndoorSeating()
	{
		return getString(INDOOR_SEATING);
	}

	public String getInternetAccess()
	{
		return getString(INTERNET_ACCESS);
	}

	public String getWheelchair()
	{
		return getString(WHEELCHAIR);
	}

	public String getToiletsDisposal()
	{
		return getString(TOILETS_DISPOSAL);
	}

	public String getAccess()
	{
		return getString(ACCESS);
	}

	public String getFee()
	{
		return getString(FEE);
	}

	public String getSmoking()
	{
		return getString(SMOKING);
	}

	public String getLevel()
	{
		return getString(LEVEL);
	}

	public String getBuilding()
	{
		return getString(BUILDING);
	}

	public String getCheckDate()
	{
		return getString(CHECK_DATE);
	}

	public String getSource()
	{
		return getString(SOURCE);
	}
}
