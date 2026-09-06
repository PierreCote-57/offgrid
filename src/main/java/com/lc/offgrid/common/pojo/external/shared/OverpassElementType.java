package com.lc.offgrid.common.pojo.external.shared;

import com.google.gson.annotations.SerializedName;

/**
 * What kind of OSM object an Overpass element is.
 */
public enum OverpassElementType
{
	@SerializedName("node")		NODE,
	@SerializedName("way")		WAY,
	@SerializedName("relation")	RELATION
}
