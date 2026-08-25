package com.lc.offgrid.pojo.page;

import com.lc.offgrid.pojo.part.Access;
import com.lc.offgrid.pojo.part.CampgroundData;

/**
 * A destination you drive to and stop at, whether or not you can sleep there. A day-use site
 * leaves campgroundData null; nothing in the data distinguishes the two kinds.
 */
public class CampSitePage extends DestinationPage
{
	private Access			access;
	private CampgroundData	campgroundData;

	public Access getAccess()
	{
		return access;
	}

	public CampgroundData getCampgroundData()
	{
		return campgroundData;
	}
}
