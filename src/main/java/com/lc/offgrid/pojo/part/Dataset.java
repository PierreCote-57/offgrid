package com.lc.offgrid.pojo.part;

/**
 * One entry in datasets.json: what the browser page can be pointed at.
 *
 * The server reads only the two fields it needs — the id it was asked for, and the file that
 * holds those rows. The rest of the entry (title, options) is the browser's business, which
 * fetches datasets.json itself.
 */
public class Dataset
{
	private String	id;
	private String	file;

	public String getId()
	{
		return id;
	}

	public String getFile()
	{
		return file;
	}
}
