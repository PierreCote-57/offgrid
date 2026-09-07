package com.lc.basics.tools.astronomy;

/**
 * Where a body is, in heliocentric ecliptic coordinates: the rectangular x, y and z in
 * astronomical units, and the longitude and radius that follow from x and y.
 *
 * A chart looking down on the plane uses the longitude and throws z away; anything working
 * out where a body sits in a local sky needs it.
 */
public class EclipticPosition
{
	private final double	x;
	private final double	y;
	private final double	z;

	public EclipticPosition(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public double getZ()
	{
		return z;
	}

	/**
	 * The ecliptic longitude, 0 to 360 degrees.
	 */
	public double getLongitude()
	{
		double radians = Math.atan2(getY(), getX());
		double degrees = Math.toDegrees(radians);
		double longitude = (degrees % 360 + 360) % 360;
		return longitude;
	}

	/**
	 * The distance from the Sun in the ecliptic plane, in astronomical units.
	 */
	public double getRadius()
	{
		double radius = Math.hypot(getX(), getY());
		return radius;
	}
}
