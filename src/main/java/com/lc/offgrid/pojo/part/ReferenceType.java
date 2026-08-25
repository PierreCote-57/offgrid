package com.lc.offgrid.pojo.part;

import com.google.gson.annotations.SerializedName;

/**
 * What an external reference is for.
 */
public enum ReferenceType
{
	@SerializedName("homepage")		HOMEPAGE,
	@SerializedName("map")			MAP,
	@SerializedName("reservation")	RESERVATION
}
