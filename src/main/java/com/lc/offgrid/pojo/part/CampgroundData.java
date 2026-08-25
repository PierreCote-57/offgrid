package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * Everything that makes a place somewhere you can sleep. A day-use site has none of it, and
 * carries this block as null.
 */
public class CampgroundData
{
	private List<String>	amenityList;
	private String			operator;
	private Integer			siteCount;
	private List<Reference>	referenceList;

	public List<String> getAmenityList()
	{
		return amenityList;
	}

	public String getOperator()
	{
		return operator;
	}

	public Integer getSiteCount()
	{
		return siteCount;
	}

	public List<Reference> getReferenceList()
	{
		return referenceList;
	}
}
