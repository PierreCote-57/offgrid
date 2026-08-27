package com.lc.offgrid.misc.geography;


import com.lc.offgrid.misc.geography.object.Angle;
import com.lc.offgrid.misc.geography.object.PolarPointComplete;
import com.lc.offgrid.misc.geography.point.LatLonPoint;

public class GeoComparator
{
	public static double getDistanceKM(LatLonPoint point1, LatLonPoint point2)
	{
		PolarPointComplete pointSmart1 = new PolarPointComplete(null, Angle.fromDeg(point1.getLatitudeDeg()), Angle.fromDeg(point1.getLongitudeDeg()));
		PolarPointComplete pointSmart2 = new PolarPointComplete(null, Angle.fromDeg(point2.getLatitudeDeg()), Angle.fromDeg(point2.getLongitudeDeg()));

		return getDistanceKM(pointSmart1, pointSmart2);
	}
	public static double getDistanceKM(PolarPointComplete pointSmart1, PolarPointComplete pointSmart2)
	{
		double		term1		= pointSmart1.getSinLat() * pointSmart2.getSinLat();
		double		lon1		= pointSmart1.getLongitude().getAngleRad();
		double		lon2		= pointSmart2.getLongitude().getAngleRad();
		double		term2		= pointSmart1.getCosLat() * pointSmart2.getCosLat() * Math.cos(lon2 - lon1);
		double		cosAngle	= term1 + term2;
		double		angle		= Math.acos(cosAngle);
		return angle * GeographyConstants.Planet.Earth.getRadiusKM();
	}
}
