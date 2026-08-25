/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

/**
 * Each unit type consists of several units defined in relation to each other.
 * For each UnitType, there must be a master (base = null), and the tree
 * of definition of all units must be rooted at the master.
 * For example:
 * 1 foot = 12 inches = 2.54 cm = 1e2 m = master
 */
public interface MeasureUnit
{

	/**
	 * The category of measurement (e.g. distance, weight, ...)
	 *
	 * @return
	 */
	String			getUnitType();

	/**
	 * The display name of the unit (e.g. milli-meter).
	 *
	 * @return	The text representation of the formal display name of the unit
	 */
	String getDisplayName();

	/**
	 * Returns the string to use when composing a size.
	 * For example: 2 ft 4 in or 5 lbs 2 oz
	 *
	 * @return		The short name of the unit.
	 */
	String getAbbreviation();

	/**
	 * Formatting string to use for this element.
	 *
	 * @return		// A java formatting string for a double element.
	 */
	String getFormatSingle();
	String getFormatDouble();

	/**
	 * Parse a value in any incoming units and returns the value in the units of this object.
	 * 		e.g. DistanceUnits.CM.parse("2", "Inch") returns 5.08
	 *
	 * @param valueText			Some number (e.g. 2)
	 * @param unitNameFrom		The units of the number (e.g. inch)
	 * @return					The value, in the units of the object (e.g. 5.08 for 5.08 cm in 2.0 inch)
	 */
	double parse(String valueText, String unitNameFrom);
	double parse(double value, String unitNameFrom);

	/**
	 * The Measure unit on which this one is based.
	 * For example, a foot is based on an inch (1 foot = 12 inches).
	 * For foot, base would be inch and size would be 12.
	 *
	 * @return	The other unit on which this one is based.
	 */
	MeasureUnit		getBase();

	/**
	 * Determines the MeasureUnit that is the absolute/MASTER unit for this unit type
	 *
	 * @return		THe master unit for this unit type.
	 */
	MeasureUnit		getAbsoluteBase();

	/**
	 * The size of this MeasureUnit, in units of the base.
	 * For example, a foot is based on an inch (1 foot = 12 inches).
	 * For foot, base would be inch and size would be 12.
	 *
	 * @return	The other unit on which this one is based.
	 */
	double			getSize();

	/**
	 * The size of this MeasureUnit, in unit of the MASTER of this UnitType
	 *
	 * @return	Absolute size, in units of the MASTER of the UnitType
	 */
	double getAbsoluteSize();

	/**
	 * Standard unit conversion, from the value in THIS unit
	 * to the value in the new unit
	 *
	 * @param size			Size from (e.g. 2, from unit CM)
	 * @param unitTo		Units to convert TO (e.g. inches)
	 * @return		Value, int unitTo (e.g. 5.08)
	 */
	double			convert(double size, MeasureUnit unitTo);

	/**
	 * Returns a strings representing the value in people units,
	 * following the chain of units, to ensure proper units
	 * in systems of multiple unit systems (e.g. metric vs US/English).
	 *
	 * @param size		e.g. FOOT.format(2.5)
	 * @return	Human form = 2 feet 6 inches.
	 */
	String			format(double size);
}
