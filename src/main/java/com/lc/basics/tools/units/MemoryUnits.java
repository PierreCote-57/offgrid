/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

/**
 * Created by Pierre on 12/10/16.
 */
public class MemoryUnits extends AbstractUnits
{
	public static final MemoryUnits		Byte	= new MemoryUnits("Byte", 			"Bytes", null, 1);
	public static final MemoryUnits		KB		= new MemoryUnits("Kilo-byte", 		"KB", Byte, 1024);
	public static final MemoryUnits		MB		= new MemoryUnits("Mega-byte", 		"MB", KB, 1024);
	public static final MemoryUnits		GB		= new MemoryUnits("Giga-byte", 		"GB", MB, 1024);
	public static final MemoryUnits		TB		= new MemoryUnits("Tera-byte", 		"TB", GB, 1024);
	public static final MemoryUnits		PB		= new MemoryUnits("Penta-byte", 	"PB", TB, 1024);

	protected MemoryUnits(String displayName, String abbreviation, MeasureUnit base, double size)
	{
		super(displayName, abbreviation, true, "%.1f %s", null, base, size);
	}
}
