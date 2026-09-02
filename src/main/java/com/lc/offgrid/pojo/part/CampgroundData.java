package com.lc.offgrid.pojo.part;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Everything that makes a place somewhere you can sleep. A day-use site has none of it, and
 * carries this block as null.
 */
public class CampgroundData
{
	/**
	 * What an external reference is for.
	 */
	public enum ReferenceType
	{
		@SerializedName("homepage")		HOMEPAGE,
		@SerializedName("map")			MAP,
		@SerializedName("reservation")	RESERVATION
	}

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

	/**
	 * An external page about the place, published by whoever runs it.
	 */
	public static class Reference
	{
		private String			label;
		private ReferenceType	type;
		private String			url;

		public String getLabel()
		{
			return label;
		}

		public ReferenceType getType()
		{
			return type;
		}

		public String getUrl()
		{
			return url;
		}
	}
}
