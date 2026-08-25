/*
 * Copyright (c) 2016 LogicielCote.COM All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.units;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Pierre on 12/10/16.
 */
public class UnitsList
{
	private static final Map<String, MeasureClassWrapper>		MEASURE_UNIT_CLASS_MAP		= new HashMap<>();

	static
	{
		registerUnits(DistanceUnits.class);
		registerUnits(MemoryUnits.class);
	}

	private static void registerUnits(Class<? extends MeasureUnit> clazz)
	{
		MeasureClassWrapper		wrapper		= new MeasureClassWrapper(clazz);
		MEASURE_UNIT_CLASS_MAP.put(wrapper.getUnitType(), wrapper);
	}

	public Set<String> getUnitTypeSet()
	{
		return MEASURE_UNIT_CLASS_MAP.keySet();
	}

	public MeasureClassWrapper getMeasureClassWrapper(String unitTypeName)
	{
		return MEASURE_UNIT_CLASS_MAP.get(unitTypeName);
	}





	private static class MeasureClassWrapper
	{
		private Class<? extends MeasureUnit>		m_clazz;

		public MeasureClassWrapper(Class<? extends MeasureUnit> clazz)
		{
			m_clazz = clazz;
		}

		public String getUnitType()
		{
			return m_clazz.getSimpleName().replace("Units", "");
		}
	}
}
