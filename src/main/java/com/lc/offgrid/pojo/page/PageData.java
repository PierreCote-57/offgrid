package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.Gallery;
import com.lc.offgrid.pojo.part.NoteSection;
import com.lc.offgrid.pojo.part.Tags;
import java.util.List;
import java.util.Map;

/**
 * What every page on the site has. Subclasses add what their kind of page needs; a page that
 * needs nothing more is a PageData itself.
 */
public class PageData
{
	private String					name;
	private String					featuredImage;
	private String					excerpt;
	private Tags					tags;
	private List<NoteSection>		noteList;
	private Map<String, Gallery>	photoGalleries;
	private List<String>			relatedDestinationList;

	public String getName()
	{
		return name;
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

	public List<NoteSection> getNoteList()
	{
		return noteList;
	}

	public Map<String, Gallery> getPhotoGalleries()
	{
		return photoGalleries;
	}

	public List<String> getRelatedDestinationList()
	{
		return relatedDestinationList;
	}
}
