/*
 * Copyright (c) 2020 LogicielCote.COM All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.misc.geography.object;


import com.lc.offgrid.misc.geography.point.LatLonPointNamed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PolarPointList
{
	// 1 nm == 1' latitude
	// 1 / 10 deg latitude = 6 nm
	private static final int		LAT_COUNT	= 1_800;

	private Map<String, PolarPointComplete>		m_map					= new HashMap<>();
	private int										m_latCount;
	private ArrayList<PolarPointComplete>[]
											m_byLatListList;

	public PolarPointList(Collection<? extends LatLonPointNamed> pointList)
	{
		this(LAT_COUNT);

		for (LatLonPointNamed point : pointList)
		{
			Angle		lat		= Angle.fromDeg(point.getLatitudeDeg());
			Angle		lon		= Angle.fromDeg(point.getLongitudeDeg());
			PolarPointComplete
						polarPoint		= new PolarPointComplete(point.getReference(), lat, lon);
			add(polarPoint);
		}
		markComplete();
	}
	public PolarPointList(int latCount)
	{
		m_latCount = latCount;
	}
	public int getLatCount()
	{
		return m_latCount;
	}

	public void add(LatLonPointNamed point)
	{
		add(new PolarPointComplete(point.getReference(),
				Angle.fromDeg(point.getLatitudeDeg()),
				Angle.fromDeg(point.getLongitudeDeg())));
	}
	private void add(PolarPointComplete point)
	{
		m_map.put(point.getKey(), point);
	}

	@SuppressWarnings("unchecked")
	public void markComplete()
	{
		int							latCount	= m_latCount;
		Set<PolarPointComplete>[]	setList		= new Set[latCount];
		for (int i = 0; i < latCount; i++)
		{
			setList[i] = new TreeSet<>();
		}
		for (PolarPointComplete point : getList())
		{
			int			index		= getLatIndex(point);
			setList[index].add(point);
		}

		m_byLatListList = new ArrayList[latCount];
		for (int i = 0; i < latCount; i++)
		{
			Set<PolarPointComplete>		set		= setList[i];
			m_byLatListList[i] = new ArrayList<>(set.size());
			m_byLatListList[i].addAll(set);
		}
	}
	private int getLatIndex(PolarPointComplete point)
	{
		double		lat		= point.getLatitude().getAngleDeg();
		return getLatIndexFromDeg(lat);
	}
	//	                     getLatCount()
	//	index = (lat + 90) * -------------
	//	                          180
	public int getLatIndexFromDeg(double lat)
	{
		int			index	= (int) ((lat + 90) * getLatCount() / 180);
		index = Math.max(0, index);
		index = Math.min(getLatCount() - 1, index);
		return index;
	}
	public int getLatIndexFromRad(double lat)
	{
		int			index	= (int) ((lat + (Math.PI / 2)) * getLatCount() / Math.PI);
		index = Math.max(0, index);
		index = Math.min(getLatCount() - 1, index);
		return index;
	}
	//	       index * 180
	//	lat = ------------- - 90
	//	      getLatCount()
	public double getLatDegAtIndex(int index)
	{
		double		lat		= (index * 180.0 / getLatCount()) - 90.0;
		return lat;
	}
	public double getLatRadAtIndex(int index)
	{
		double		lat		= (index * Math.PI / getLatCount()) - (Math.PI / 2);
		return lat;
	}

	public Map<String, PolarPointComplete> getMap()
	{
		return m_map;
	}
	public int size()
	{
		return getList().size();
	}
	public boolean isEmpty()
	{
		return getMap().isEmpty();
	}
	public PolarPointComplete getPoint(String key)
	{
		return getMap().get(key);
	}
	private Collection<PolarPointComplete> getList()
	{
		return getMap().values();
	}
	public List<PolarPointComplete> getList(int index)
	{
		return m_byLatListList[index];
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
		return String.format("Collection(%,d points)", getMap().size());
	}
}
