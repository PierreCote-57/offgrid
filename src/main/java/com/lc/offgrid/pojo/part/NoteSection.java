package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * A titled block of further reading on a page.
 */
public class NoteSection
{
	private String			sectionName;
	private List<NoteItem>	itemList;

	public String getSectionName()
	{
		return sectionName;
	}

	public List<NoteItem> getItemList()
	{
		return itemList;
	}
}
