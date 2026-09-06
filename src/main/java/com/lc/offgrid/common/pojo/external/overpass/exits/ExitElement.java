package com.lc.offgrid.common.pojo.external.overpass.exits;

import com.lc.offgrid.common.pojo.external.shared.OverpassElementType;

/**
 * One motorway junction. OSM carries the coordinates on the element itself, so there is no
 * geometry block here.
 */
public class ExitElement
{
	private OverpassElementType	type;
	private Long				id;
	private Double				lat;
	private Double				lon;
	private ExitTags			tags;

	public OverpassElementType getType()
	{
		return type;
	}

	public Long getId()
	{
		return id;
	}

	public Double getLat()
	{
		return lat;
	}

	public Double getLon()
	{
		return lon;
	}

	public ExitTags getTags()
	{
		return tags;
	}
}
