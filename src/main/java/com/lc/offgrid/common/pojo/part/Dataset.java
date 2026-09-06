package com.lc.offgrid.common.pojo.part;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * One entry in datasets.json: what the browser page can be pointed at.
 *
 * The id names the dataset and the file holds its rows; the title is what the dataset is
 * called, and the option list names which controls the browser page offers for it.
 */
public class Dataset
{
	/**
	 * A control the browser page can offer for a dataset. The list on a dataset names which of
	 * them it shows, and in which order.
	 */
	public enum DatasetOption
	{
		@SerializedName("view")		VIEW,
		@SerializedName("types")	TYPES,
		@SerializedName("keywords")	KEYWORDS,
		@SerializedName("badges")	BADGES,
		@SerializedName("access")	ACCESS,
		@SerializedName("search")	SEARCH,
		@SerializedName("booklet")	BOOKLET
	}

	private String				id;
	private String				file;
	private String				title;
	@SerializedName("options")
	private List<DatasetOption>	optionList;

	public String getId()
	{
		return id;
	}

	public String getFile()
	{
		return file;
	}

	public String getTitle()
	{
		return title;
	}

	public List<DatasetOption> getOptionList()
	{
		return optionList;
	}
}
