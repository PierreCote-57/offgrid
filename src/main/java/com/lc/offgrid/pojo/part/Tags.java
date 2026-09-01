package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * The vocabularies a page is filed under. typeList drives which page class is built; badgeList
 * and keywordList are for the reader.
 */
public class Tags
{
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
