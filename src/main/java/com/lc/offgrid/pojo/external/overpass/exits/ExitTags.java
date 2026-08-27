package com.lc.offgrid.pojo.external.overpass.exits;

import com.lc.offgrid.pojo.external.shared.FeatureProperties;

/**
 * The OSM tags on one junction. The named tags are the ones the download carries today; a tag
 * a mapper adds later is still in the map and still reachable by name.
 */
public class ExitTags extends FeatureProperties
{
	private static final String	HIGHWAY			= "highway";
	private static final String	REF				= "ref";
	private static final String	NOREF			= "noref";
	private static final String	LOC_NAME		= "loc_name";
	private static final String	REF_RIGHT		= "ref:right";
	private static final String	REF_LEFT		= "ref:left";
	private static final String	UNSIGNED_REF	= "unsigned_ref";
	private static final String	NOTE			= "note";
	private static final String	FUTURE_REF		= "future_ref";
	private static final String	FUT_REF			= "fut_ref";
	private static final String	OLD_REF			= "old_ref";
	private static final String	NONAME			= "noname";
	private static final String	DESTINATION		= "destination";

	public String getHighway()
	{
		return getString(HIGHWAY);
	}

	public String getRef()
	{
		return getString(REF);
	}

	public String getNoref()
	{
		return getString(NOREF);
	}

	public String getLocName()
	{
		return getString(LOC_NAME);
	}

	public String getRefRight()
	{
		return getString(REF_RIGHT);
	}

	public String getRefLeft()
	{
		return getString(REF_LEFT);
	}

	public String getUnsignedRef()
	{
		return getString(UNSIGNED_REF);
	}

	public String getNote()
	{
		return getString(NOTE);
	}

	public String getFutureRef()
	{
		return getString(FUTURE_REF);
	}

	public String getFutRef()
	{
		return getString(FUT_REF);
	}

	public String getOldRef()
	{
		return getString(OLD_REF);
	}

	public String getNoname()
	{
		return getString(NONAME);
	}

	public String getDestination()
	{
		return getString(DESTINATION);
	}
}
