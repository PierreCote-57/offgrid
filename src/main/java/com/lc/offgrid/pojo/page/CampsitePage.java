package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.CampgroundData;

/**
 * A destination you drive to and stop at, whether or not you can sleep there. A day-use site
 * leaves campgroundData null; nothing in the data distinguishes the two kinds.
 */
public class CampsitePage extends DestinationPage
{
	private CampgroundData	campgroundData;

	public CampgroundData getCampgroundData()
	{
		return campgroundData;
	}
}
