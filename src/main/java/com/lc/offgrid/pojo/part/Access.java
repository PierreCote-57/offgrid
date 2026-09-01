package com.lc.offgrid.pojo.part;

import java.util.List;
import java.util.TreeMap;

/**
 * How hard the place is to reach: how far it is from the towns a reader starts from, and what
 * the road does along the way.
 */
public class Access
{
	/**
	 * Drive surfaces, easiest first. The one furthest along this list wins the badge. The order
	 * follows the van, not the road, so another vehicle reorders it here and nothing else moves.
	 */
	public static final List<RoadType>	ROAD_RANK			= List.of(RoadType.UNPAVED, RoadType.DIRT,
			RoadType.POTHOLES, RoadType.SHARP_ROCK, RoadType.RUGGED);

	/** Leg types where you are no longer in the van. Any one of them decides the whole approach. */
	public static final List<RoadType>	NON_DRIVE_LEG_TYPES	= List.of(RoadType.WALK, RoadType.HIKE, RoadType.BOAT);

	private TreeMap<String, Integer>	haversineMap;
	private List<Leg>					legList;

	public TreeMap<String, Integer> getHaversineMap()
	{
		return haversineMap;
	}

	public List<Leg> getLegList()
	{
		return legList;
	}

	/**
	 * The one leg that decides how the approach reads. Back country as soon as a leg leaves the
	 * van, otherwise the hardest drive surface named in legList, and pavement when the list is
	 * empty. A null legList means nobody has filled the approach in yet, and the answer is null.
	 *
	 * Its km is every leg of that same kind added together, wherever those legs fall in the
	 * drive: potholes 3, dirt 5, potholes 2, dirt 1 gives potholes 5. Pavement has nothing to
	 * measure and carries a null km.
	 *
	 * The answer is built rather than picked out of legList, because pavement and back country
	 * are not surfaces any leg names.
	 *
	 * A leg whose type the vocabulary does not know is ignored, and so is an unpaved leg with
	 * no km, since unpaved asserts a measured tail and claims nothing without one.
	 */
	public Leg getRoadLimitingLeg()
	{
		if (null == legList)
		{
			return null;
		}
		if (legList.isEmpty())
		{
			Leg pavementLeg = new Leg(RoadType.PAVEMENT, null);
			return pavementLeg;
		}

		int worstRank = -1;
		for (Leg leg : legList)
		{
			RoadType type = leg.getType();
			boolean nonDrive = NON_DRIVE_LEG_TYPES.contains(type);
			int driveRank = ROAD_RANK.indexOf(type);
			if (!nonDrive && driveRank < 0)
			{
				continue;
			}
			if (RoadType.UNPAVED == type && !isMeasured(leg))
			{
				continue;
			}
			if (nonDrive)
			{
				Double backCountryKm = sumKm(NON_DRIVE_LEG_TYPES);
				Leg backCountryLeg = new Leg(RoadType.BACK_COUNTRY, backCountryKm);
				return backCountryLeg;
			}
			if (driveRank > worstRank)
			{
				worstRank = driveRank;
			}
		}

		if (worstRank < 0)
		{
			return null;
		}

		RoadType worstType = ROAD_RANK.get(worstRank);
		List<RoadType> wantedList = List.of(worstType);
		Double worstKm = sumKm(wantedList);
		Leg limitingLeg = new Leg(worstType, worstKm);
		return limitingLeg;
	}

	/** The km of every leg whose type is one of the wanted ones, or null when they add to nothing. */
	private Double sumKm(List<RoadType> wantedList)
	{
		double total = 0;
		for (Leg leg : legList)
		{
			RoadType type = leg.getType();
			if (!wantedList.contains(type))
			{
				continue;
			}
			Double km = leg.getKm();
			if (null == km)
			{
				continue;
			}
			total += km;
		}
		Double totalKm = 0 == total ? null : total;
		return totalKm;
	}

	/** Whether the leg states a distance someone actually walked off. */
	private boolean isMeasured(Leg leg)
	{
		Double km = leg.getKm();
		boolean measured = null != km && km > 0;
		return measured;
	}
}
