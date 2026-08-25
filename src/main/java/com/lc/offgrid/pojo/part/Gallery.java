package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * A named set of photos. A page may carry several, keyed by name.
 */
public class Gallery
{
	private String				name;
	private List<GalleryItem>	itemList;

	public String getName()
	{
		return name;
	}

	public List<GalleryItem> getItemList()
	{
		return itemList;
	}
}
