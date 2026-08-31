package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.FishingReferences;

/**
 * An area of water. The province publishes fishing data about it.
 */
public class LakePage extends DestinationPage
{
	private FishingReferences	fishingReferences;

	public FishingReferences getFishingReferences()
	{
		return fishingReferences;
	}
}
