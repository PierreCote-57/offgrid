/*
 * Copyright (c) 2016 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AbstractUnits implements MeasureUnit
{
	private static final Map<String, Map<String, AbstractUnits>>		TYPE_NAME_UNIT_MAP		= new TreeMap<>();
	private static final Map<String, Map<String, AbstractUnits>>		TYPE_ALIAS_UNIT_MAP		= new TreeMap<>();

	private final String			m_displayName;
	private final String			m_abbreviation;
	private final boolean			m_useForFormatting;
	private final String			m_formatSingle;
	private final String			m_formatDouble;
	private final MeasureUnit		m_base;
	private final double			m_size;		// Number of units;

	protected AbstractUnits(String displayName, String abbreviation, boolean useForFormatting,
							String formatSingle, String formatDouble, MeasureUnit base, double size)
	{
		m_displayName = displayName;
		m_abbreviation = abbreviation;
		m_useForFormatting = useForFormatting;
		m_formatSingle = formatSingle;
		m_formatDouble = formatDouble;
		m_base = base;
		m_size = size;

		registerUnit(this, TYPE_NAME_UNIT_MAP, false);
		registerUnit(this, TYPE_ALIAS_UNIT_MAP, true);
	}
	private void registerUnit(AbstractUnits units, Map<String, Map<String, AbstractUnits>> typeNameUnitMap, boolean registerAbbreviation)
	{
		String						unitType			= getUnitType();
		Map<String, AbstractUnits>	measureUnitMap		= typeNameUnitMap.computeIfAbsent(unitType.toLowerCase(Locale.US), k -> new TreeMap<>());
		measureUnitMap.put(units.getDisplayName().toLowerCase(Locale.US), units);
		if (registerAbbreviation)
		{
			measureUnitMap.put(units.getAbbreviation().toLowerCase(Locale.US), units);
		}
	}
	public static AbstractUnits getUnit(String unitType, String unitName)
	{
		Map<String, AbstractUnits>		measureUnitMap		= TYPE_ALIAS_UNIT_MAP.get(unitType.toLowerCase(Locale.US));
		return measureUnitMap.get(unitName.toLowerCase(Locale.US));
	}

	@Override
	public String getUnitType()
	{
		return getClass().getSimpleName().replace("Units", "");
	}

	@Override
	public String getDisplayName()
	{
		return m_displayName;
	}

	@Override
	public String getAbbreviation()
	{
		return m_abbreviation;
	}

	@Override
	public String getFormatSingle()
	{
		return m_formatSingle;
	}

	@Override
	public String getFormatDouble()
	{
		return m_formatDouble;
	}

	@Override
	public MeasureUnit getBase()
	{
		return m_base;
	}

	@Override
	public double getSize()
	{
		return m_size;
	}

	@Override
	public MeasureUnit getAbsoluteBase()
	{
		MeasureUnit		measureUnit		= this;
		while (null != measureUnit.getBase())
		{
			measureUnit = measureUnit.getBase();
		}
		return measureUnit;
	}

	@Override
	public double getAbsoluteSize()
	{
		MeasureUnit		measureUnit		= this;
		double			size			= 1.0;
		while (null != measureUnit.getBase())
		{
			size *= measureUnit.getSize();
			measureUnit = measureUnit.getBase();
		}
		return size;
	}

	@Override
	public double convert(double sizeFrom, MeasureUnit unitTo)
	{
		double		baseSizeFrom	= getAbsoluteSize();
		double		baseSizeTo		= unitTo.getAbsoluteSize();
		double		sizeTo			= sizeFrom * baseSizeFrom / baseSizeTo;

		return sizeTo;
	}

	public static double parse(String unitType, String valueText, String unitNameFrom, String unitNameTo)
	{
		AbstractUnits unitTo		= getUnit(unitType, unitNameTo);
		return unitTo.parse(valueText, unitNameFrom);
	}

	@Override
	public double parse(String valueText, String unitNameFrom)
	{
		double			value		= Double.parseDouble(valueText);
		return parse(value, unitNameFrom);
	}

	@Override
	public double parse(double value, String unitNameFrom)
	{
		MeasureUnit		unitFrom	= getUnit(getUnitType(), unitNameFrom);
		return unitFrom.convert(value, this);
	}

	@Override
	public String format(double value)
	{
		return format(this, value);
	}
	private static String format(AbstractUnits unit, double value)
	{
		Map<String, AbstractUnits>		measureUnitMap		= TYPE_NAME_UNIT_MAP.get(unit.getUnitType().toLowerCase(Locale.US));

		while (true)
		{
			MeasureUnit		unitSmaller		= unit.getUnitSmaller(measureUnitMap);
			Double			valueSmaller		= null == unitSmaller ? null : unit.convert(value, unitSmaller);
			MeasureUnit		unitBigger		= unit.getUnitBigger(measureUnitMap);
			Double			valueBigger		= null == unitBigger ? null : unit.convert(value, unitBigger);

			// Very small, try the smaller unit...
			if (value < 1.0 && null != unitSmaller)
			{
				unit = (AbstractUnits) unitSmaller;
				value = valueSmaller;
				continue;
			}

			// Very big, try a bigger unit
			if (null != valueBigger && valueBigger > 1.0)
			{
				unit = (AbstractUnits) unitBigger;
				value = valueBigger;
				continue;
			}

			// Looks right, use this...
			double		actualFloored		= Math.floor(value);
			double		remainder			= value - actualFloored;
			double		actualRemainder		= null == unitSmaller ? 0.0 : unit.convert(remainder, unitSmaller);

			String		text;
			if (10 < value
					|| null == valueSmaller
					|| null == unit.getFormatDouble()
					|| actualRemainder < 0.5
			)
			{
				text = String.format(unit.getFormatSingle(),
						value, unit.getAbbreviation());
			}
			else
			{
				text = String.format(unit.getFormatDouble(),
					actualFloored, unit.getAbbreviation(),
					actualRemainder, unitSmaller.getAbbreviation());
			}
			return text;
		}
	}
	private MeasureUnit getUnitSmaller(Map<String, AbstractUnits> measureUnitMap)
	{
		return getBase();
	}
	private MeasureUnit getUnitBigger(Map<String, AbstractUnits> measureUnitMap)
	{
		MeasureUnit		bigger		= null;

		for (AbstractUnits measureUnit : measureUnitMap.values())
		{
			if (this != measureUnit.getBase() || !measureUnit.m_useForFormatting)
			{
				continue;
			}

			//Find the closest who uses this as its base...
			if (null == bigger)
			{
				bigger = measureUnit;
			}
			else
			{
				bigger = bigger.getSize() <= measureUnit.getSize() ? bigger : measureUnit;
			}
		}

		return bigger;
	}


	/**
	 * Returns a string representation of the object. In general, the
	 * {@code toString} method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * <p>
	 * The {@code toString} method for class {@code Object}
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `{@code @}', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return String.format("%s = %f %s", getDisplayName(), getSize(), null == getBase() ? "-" : getBase().getDisplayName());
	}
}
