/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

public class TimeUnits extends AbstractUnits
{
	public static final TimeUnits NS		= new TimeUnits("Nano-second", 	"ns",		true,	null, 1);
	public static final TimeUnits US		= new TimeUnits("Micro-second",	"us",		true,	NS, 1000);
	public static final TimeUnits MS		= new TimeUnits("Milli-second",	"ms",		true,	US, 1000);
	public static final TimeUnits SEC		= new TimeUnits("Second",		"sec",		true,	MS, 1000);

	public static final TimeUnits MIN		= new TimeUnits("Minute",		"min",		true,	SEC, 60);
	public static final TimeUnits HOUR		= new TimeUnits("Hour",			"hour",		true,	MIN, 60);
	public static final TimeUnits DAY		= new TimeUnits("Day",			"day",		true,	HOUR, 24);
//	public static final TimeUnits WEEK		= new TimeUnits("Week",			"week",		true,	DAY, 7);
	public static final TimeUnits MONTH		= new TimeUnits("Month",			"month",		false,	DAY, 30);
	public static final TimeUnits YEAR		= new TimeUnits("Year",			"year",		true,	DAY, 365.35);

	protected TimeUnits(String displayName, String abbreviation, boolean useForFormat, MeasureUnit base, double size)
	{
		super(displayName, abbreviation, useForFormat, "%.1f %s", "%2.0f %s %2.0f %s", base, size);
	}
}
