package com.lc.offgrid.common.misc;

import java.util.Map;
import java.util.TreeMap;

public class QueryUtil
{
	private static Map<String, String> DISPLAY_NAME_MAP = new TreeMap<>();
	static
	{
		DISPLAY_NAME_MAP.put("destination", "destinations");
		DISPLAY_NAME_MAP.put("van-howto", "how to");
		DISPLAY_NAME_MAP.put("van-checklist", "checklists");
	}

	public static String extractQueryParam(String urlText)
	{
		if (null != urlText && urlText.contains("?"))
		{
			return urlText.substring(urlText.indexOf("?") + 1);
		}
		else
		{
			return null;
		}
	}

	public static String extractQueryParam(String urlText, String paramName)
	{
		if (null == urlText || !urlText.contains("?"))
		{
			return null;
		}
		int start = urlText.indexOf(paramName);
		if (-1 == start)
		{
			return null;
		}
		String value = urlText.substring(start + paramName.length() + 1);
		int end = value.indexOf('&');
		if (-1 != end)
		{
			value = value.substring(0, end);
		}
		return value;
	}
	public static String extractQueryParamPretty(String urlText, String paramName)
	{
		String value = extractQueryParam(urlText, paramName);
		value = null == value ? null : DISPLAY_NAME_MAP.get(value);
		return value;
	}
}
