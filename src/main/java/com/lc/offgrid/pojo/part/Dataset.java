package com.lc.offgrid.pojo.part;

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
	private String			id;
	private String			file;
	private String			title;
	@SerializedName("options")
	private List<String>	optionList;

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

	public List<String> getOptionList()
	{
		return optionList;
	}
}
