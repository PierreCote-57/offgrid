package com.lc.offgrid.pojo.part;

/**
 * An external page about the place, published by whoever runs it.
 */
public class Reference
{
	private String			label;
	private ReferenceType	type;
	private String			url;

	public String getLabel()
	{
		return label;
	}

	public ReferenceType getType()
	{
		return type;
	}

	public String getUrl()
	{
		return url;
	}
}
