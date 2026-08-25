/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

/**
 * Created by Pierre on 12/10/16.
 */
public class DistanceUnits extends AbstractUnits
{
	public static final DistanceUnits PM		= new DistanceUnits("Pico-meter",	"pm",true,		null, 1);
	public static final DistanceUnits NM		= new DistanceUnits("Nano-meter", 	"nm",true,			PM, 1000);
	public static final DistanceUnits UM		= new DistanceUnits("Micro-meter",	"um",true,			NM, 1000);
	public static final DistanceUnits MM		= new DistanceUnits("Milli-meter",	"mm",true,			UM, 1000);
	public static final DistanceUnits CM		= new DistanceUnits("Centi-meter",	"cm",true,			MM, 10);
	public static final DistanceUnits M			= new DistanceUnits("Meter",		"m",	true,			CM, 100);
	public static final DistanceUnits KM		= new DistanceUnits("Kilo-meter",	"km",true,			 M, 1000);

	public static final DistanceUnits INCH		= new DistanceUnits("Inch",			"in",true,			CM, 2.54);
	public static final DistanceUnits FOOT		= new DistanceUnits("Foot",			"ft",true,			INCH, 12);
	public static final DistanceUnits YARD		= new DistanceUnits("Yard",			"yd",false,			FOOT, 3);
	public static final DistanceUnits MILE		= new DistanceUnits("Mile",			"Mile",true,		FOOT, 5280);

	public static final DistanceUnits NMILE		= new DistanceUnits("Nautical Mile","Naut-mile",false,	FOOT, 3.1E16);

	public static final DistanceUnits LIGHT_YEAR	= new DistanceUnits("Light-year", "Light-year",true,M, 9.461e15);
	public static final DistanceUnits PARSEC	= new DistanceUnits("Parsec",			"Mile",true,	LIGHT_YEAR, 3.26);

	public DistanceUnits(String displayName, String abbreviation, boolean useForFormat, MeasureUnit base, double size)
	{
		super(displayName, abbreviation, useForFormat, "%.1f %s","%.0f %s %.0f %s", base, size);
	}
}
