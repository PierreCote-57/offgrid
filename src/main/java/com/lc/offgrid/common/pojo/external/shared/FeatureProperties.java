package com.lc.offgrid.common.pojo.external.shared;

import java.util.HashMap;

/**
 * The properties block of one GeoJSON feature, held exactly as it arrives. Columns nobody has
 * named are still in here and still reachable. Gson hands back every JSON number as a Double,
 * which is why the typed accessors convert rather than cast.
 */
public class FeatureProperties extends HashMap<String, Object>
{
	public String getString(String name)
	{
		Object value = get(name);
		if (null == value)
		{
			return null;
		}
		String text = value.toString();
		return text;
	}

	public Double getDouble(String name)
	{
		Object value = get(name);
		if (null == value)
		{
			return null;
		}
		Double number = (Double) value;
		return number;
	}

	public Integer getInteger(String name)
	{
		Double number = getDouble(name);
		if (null == number)
		{
			return null;
		}
		Integer count = number.intValue();
		return count;
	}
}
