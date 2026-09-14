package com.lc.offgrid.common.misc.astronomy.planet;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Where a body stands for an observer at one moment: when it is, and where to look.
 *
 * The time is epoch seconds, the moment itself and no zone with it: whoever shows it to a
 * reader states the zone it is shown in. The two angles are what a person standing outside
 * uses: which way to turn, and how far up from there.
 */
public class HorizonsMoment
{
	/** How the moment is written out, on the clock of whoever reads it. */
	private static final DateTimeFormatter TEXT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");

	/** When this moment is, in seconds from the epoch. */
	private final long		epochSecond;

	/** True bearing in degrees, clockwise from north: 90 is east, 180 is south. */
	private final double	bearing;

	/** How far above the horizon, in degrees. */
	private final double	elevation;

	public HorizonsMoment(long epochSecond, double bearing, double elevation)
	{
		this.epochSecond = epochSecond;
		this.bearing = bearing;
		this.elevation = elevation;
	}

	public long getEpochSecond()
	{
		return epochSecond;
	}

	public double getBearing()
	{
		return bearing;
	}

	public double getElevation()
	{
		return elevation;
	}

	/**
	 * The moment in the zone the machine reading it runs in, then the two angles.
	 */
	@Override
	public String toString()
	{
		Instant instant = Instant.ofEpochSecond(getEpochSecond());
		ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
		String timeText = dateTime.format(TEXT_FORMAT);

		String text = String.format("%s, bearing %.1f°, elevation %.1f°", timeText, getBearing(), getElevation());
		return text;
	}
}
