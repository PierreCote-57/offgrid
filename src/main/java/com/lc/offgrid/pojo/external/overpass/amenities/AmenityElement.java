package com.lc.offgrid.pojo.external.overpass.amenities;

import com.lc.offgrid.pojo.external.shared.OverpassCenter;
import com.lc.offgrid.pojo.external.shared.OverpassElementType;

/**
 * One amenity. A node carries lat/lon directly; a way or a relation carries neither and has a
 * center instead, so reading only lat/lon drops every building — which is most of the shops,
 * hotels and supermarkets.
 */
public class AmenityElement
{
	private OverpassElementType	type;
	private Long				id;
	private Double				lat;
	private Double				lon;
	private OverpassCenter		center;
	private AmenityTags			tags;

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

	public OverpassCenter getCenter()
	{
		return center;
	}

	public AmenityTags getTags()
	{
		return tags;
	}
}
