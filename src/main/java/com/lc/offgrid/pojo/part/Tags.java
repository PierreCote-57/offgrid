package com.lc.offgrid.pojo.part;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * The vocabularies a page is filed under. typeList drives which page class is built; badgeList
 * and keywordList are for the reader.
 */
public class Tags
{
	/**
	 * What you would do at a place: the coloured pills on the left of the tag row.
	 * Alphabetical, because the order is only the order they are listed in.
	 */
	public enum Badge
	{
		@SerializedName("camping")	CAMPING,
		@SerializedName("fishing")	FISHING,
		@SerializedName("hiking")	HIKING,
		@SerializedName("picnic")	PICNIC
	}

	/**
	 * What KIND of place a destination is. It is also the URL segment a destination is served
	 * under, so the two are one vocabulary.
	 *
	 * A type is never mandatory, and that is what keeps PARK meaningful: a city park is a very
	 * different thing from a provincial park, so it carries no type at all rather than a wrong
	 * one. Alphabetical, because the order is only the order a dropdown lists them in.
	 */
	public enum DestinationType
	{
		@SerializedName("campground")	CAMPGROUND,
		@SerializedName("lake")			LAKE,
		@SerializedName("park")			PARK,
		@SerializedName("rec-site")		REC_SITE
	}

	private List<Badge>				badgeList;
	private List<DestinationType>	typeList;
	private List<String>			keywordList;

	public List<Badge> getBadgeList()
	{
		return badgeList;
	}

	public List<DestinationType> getTypeList()
	{
		return typeList;
	}

	public List<String> getKeywordList()
	{
		return keywordList;
	}
}
