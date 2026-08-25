package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * The vocabularies a page is filed under. typeList drives which page class is built; badgeList
 * and keywordList are for the reader.
 */
public class Tags
{
	private List<String>	badgeList;
	private List<String>	typeList;
	private List<String>	keywordList;

	public List<String> getBadgeList()
	{
		return badgeList;
	}

	public List<String> getTypeList()
	{
		return typeList;
	}

	public List<String> getKeywordList()
	{
		return keywordList;
	}
}
