package com.lc.offgrid.common.pojo.page;

import com.google.gson.annotations.SerializedName;
import com.lc.offgrid.common.pojo.part.GalleryItem;
import com.lc.offgrid.common.pojo.part.NoteItem;
import com.lc.offgrid.common.pojo.part.Tags;
import java.util.List;
import java.util.TreeMap;

/**
 * What every page on the site has. Subclasses add what their kind of page needs; a page that
 * needs nothing more is a PageData itself.
 */
public class PageData
{
	/**
	 * How far along a page is. EXTERNAL is a dataset row with no page of its own; no page file
	 * writes it. In lifecycle order.
	 */
	public enum PageState
	{
		@SerializedName("external")		EXTERNAL,
		@SerializedName("boilerplate")	BOILERPLATE,
		@SerializedName("draft")		DRAFT,
		@SerializedName("production")	PRODUCTION
	}

	private String											name;
	private PageState										pageState;
	private String											featuredImage;
	private String											excerpt;
	private Tags											tags;
	private TreeMap<String, List<NoteItem>>					noteMap;
	private TreeMap<String, TreeMap<String, GalleryItem>>	photoGalleries;
	private List<String>									relatedDestinationList;

	public String getName()
	{
		return name;
	}

	public PageState getPageState()
	{
		return pageState;
	}

	public String getFeaturedImage()
	{
		return featuredImage;
	}

	public String getExcerpt()
	{
		return excerpt;
	}

	public Tags getTags()
	{
		return tags;
	}

	public TreeMap<String, List<NoteItem>> getNoteMap()
	{
		return noteMap;
	}

	public TreeMap<String, TreeMap<String, GalleryItem>> getPhotoGalleries()
	{
		return photoGalleries;
	}

	public List<String> getRelatedDestinationList()
	{
		return relatedDestinationList;
	}

	@Override
	public String toString()
	{
		return String.format("%s", getName());
	}
}
