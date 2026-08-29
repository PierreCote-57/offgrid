package com.lc.offgrid.pojo.part;

import com.google.gson.annotations.SerializedName;

/**
 * The marker drawn for a place. NONE carries the empty string, which is what a file writes
 * when it has coordinates but no opinion about the marker.
 */
public enum MapIcon
{
	@SerializedName("")				NONE,
	@SerializedName("lake")			LAKE,
	@SerializedName("tent")			TENT,
	@SerializedName("campground")	CAMPGROUND,
	@SerializedName("picnic")		PICNIC,
	@SerializedName("park")			PARK,
	@SerializedName("home")			HOME,
	@SerializedName("outhouse")		OUTHOUSE
}
