package com.lc.offgrid.common.pojo.page;

import com.google.gson.annotations.SerializedName;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.common.pojo.part.GalleryItem;
import com.lc.offgrid.common.pojo.part.NoteItem;
import com.lc.offgrid.common.pojo.part.Tags;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * What every page on the site has. Subclasses add what their kind of page needs; a page that
 * needs nothing more is a PageData itself.
 */
public class PageData
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(PageData.class);
	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	/**
	 * How far along a page is. EXTERNAL is a dataset row with no page of its own; no page file
	 * writes it. In lifecycle order.
	 */
	public enum PageState
	{
		@SerializedName("external")		EXTERNAL,
		@SerializedName("boilerplate")	BOILERPLATE,
		@SerializedName("draft")		DRAFT,
		@SerializedName("production")	PRODUCTION;

		// Ignores case; null for a missing or unknown word.
		public static PageState of(String word)
		{
			PageState pageState = null;
			if (null != word)
			{
				String trimmed = word.trim();
				for (PageState candidate : values())
				{
					if (candidate.name().equalsIgnoreCase(trimmed))
					{
						pageState = candidate;
						break;
					}
				}
			}
			return pageState;
		}
	}

	/**
	 * Which page states this environment shows, read once from the profile's yaml. A singleton,
	 * so the list is parsed once rather than by every prototype processor.
	 */
	@Component
	public static class PageStateFilter implements InitializingBean
	{
		public static final PageStateFilter WITH_CONTENT =
				new PageStateFilter();
		static
		{
			WITH_CONTENT.allowedSet = EnumSet.of(PageState.DRAFT, PageState.PRODUCTION);
		}

		// Every state until a profile's yaml says otherwise, so a missing key filters nothing.
		@Value("${PageStateFilter.pageStateList:external,boilerplate,draft,production}")
		private List<String>	pageStateList;

		private EnumSet<PageState>	allowedSet = EnumSet.noneOf(PageState.class);

		public List<String> getPageStateList()
		{
			return pageStateList;
		}

		public EnumSet<PageState> getAllowedSet()
		{
			return allowedSet;
		}

		@Override
		public void afterPropertiesSet() throws Exception
		{
			EnumSet<PageState> parsedSet = EnumSet.noneOf(PageState.class);
			for (String word : getPageStateList())
			{
				PageState pageState = PageState.of(word);
				if (null == pageState)
				{
					getLogger().warn("Unknown page state in PageStateFilter.pageStateList: '%s'", word);
				}
				else
				{
					parsedSet.add(pageState);
				}
			}
			allowedSet = parsedSet;
			WITH_CONTENT.allowedSet.retainAll(allowedSet);
		}

		// A null state is judged as EXTERNAL.
		public boolean isAllowed(PageState pageState)
		{
			PageState judgedState = pageState;
			if (null == judgedState)
			{
				judgedState = PageState.EXTERNAL;
			}
			boolean allowed = getAllowedSet().contains(judgedState);
			return allowed;
		}

		public boolean isAllowed(PageData pageData)
		{
			PageState pageState = pageData.getPageState();
			boolean allowed = isAllowed(pageState);
			return allowed;
		}

		@Override
		public String toString()
		{
			return String.format("%s", getAllowedSet());
		}
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
