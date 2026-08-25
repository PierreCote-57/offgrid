package com.lc.offgrid.pojo.part;

import java.util.List;

/**
 * One entry in a note section. The description is carried a line at a time.
 */
public class NoteItem
{
	private String			name;
	private String			url;
	private List<String>	description;

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return url;
	}

	public List<String> getDescription()
	{
		return description;
	}
}
