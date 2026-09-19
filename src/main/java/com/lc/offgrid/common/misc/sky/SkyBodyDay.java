package com.lc.offgrid.common.misc.sky;

import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMoment;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMomentName;

import java.util.Map;

/**
 * A moment that does not happen on the day is a null.
 */
public class SkyBodyDay
{
	private final Map<HorizonsMomentName, HorizonsMoment>	momentMap;

	private final double	distanceAu;
	private final double	orbitRadiusAu;
	private final Double	apparentMagnitude;

	/** The constellation the body sits in, or null where Horizons named one that is not one of the 88. */
	private final Constellation	constellation;

	private final double	litFraction;

	public SkyBodyDay(Map<HorizonsMomentName, HorizonsMoment> momentMap, double distanceAu,
			double orbitRadiusAu, Double apparentMagnitude, Constellation constellation,
			double litFraction)
	{
		this.momentMap = momentMap;
		this.distanceAu = distanceAu;
		this.orbitRadiusAu = orbitRadiusAu;
		this.apparentMagnitude = apparentMagnitude;
		this.constellation = constellation;
		this.litFraction = litFraction;
	}

	public Map<HorizonsMomentName, HorizonsMoment> getMomentMap()
	{
		return momentMap;
	}

	public double getDistanceAu()
	{
		return distanceAu;
	}

	public double getOrbitRadiusAu()
	{
		return orbitRadiusAu;
	}

	public Double getApparentMagnitude()
	{
		return apparentMagnitude;
	}

	public Constellation getConstellation()
	{
		return constellation;
	}

	public double getLitFraction()
	{
		return litFraction;
	}
}
