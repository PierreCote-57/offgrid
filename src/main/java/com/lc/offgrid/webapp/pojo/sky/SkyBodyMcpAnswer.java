package com.lc.offgrid.webapp.pojo.sky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One body's day, as a reader standing outside needs it: when to look, where to look, and
 * whether there is anything to see. A moment the body does not reach that day is left out of
 * the answer rather than written as a null, because the generated output schema types each of
 * these and the client validates against it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkyBodyMcpAnswer
{
	private final String	name;

	@JsonProperty(required = false)
	private final String	riseText;

	@JsonProperty(required = false)
	private final String	transitText;

	@JsonProperty(required = false)
	private final String	setText;

	/** True bearing in degrees where it rises, clockwise from north: 90 is east. */
	@JsonProperty(required = false)
	private final Double	riseBearing;

	/** How far above the horizon it gets at transit, in degrees. */
	@JsonProperty(required = false)
	private final Double	transitElevation;

	/** Where to turn at the time asked about, and how far up from there. Both are left out when
	 *  the caller named no time: noon's angles are not an answer to a question nobody asked. A
	 *  negative elevation is the body under the horizon, which is the answer rather than a null. */
	@JsonProperty(required = false)
	private final Double	bearing;

	@JsonProperty(required = false)
	private final Double	elevation;

	/** The constellation it sits in, or null where Horizons named one that is not one of the 88. */
	@JsonProperty(required = false)
	private final String	constellationName;

	/** Lower is brighter: Venus runs near -4, the faintest thing an eye sees is near 6. */
	@JsonProperty(required = false)
	private final Double	apparentMagnitude;

	/** From the observer, in astronomical units. */
	private final double	distanceAu;

	/** How much of the disc is lit, 0 at new and 100 at full. */
	private final long		litPercent;

	public SkyBodyMcpAnswer(String name, String riseText, String transitText, String setText,
			Double riseBearing, Double transitElevation, Double bearing, Double elevation,
			String constellationName, Double apparentMagnitude, double distanceAu, long litPercent)
	{
		this.name = name;
		this.riseText = riseText;
		this.transitText = transitText;
		this.setText = setText;
		this.riseBearing = riseBearing;
		this.transitElevation = transitElevation;
		this.bearing = bearing;
		this.elevation = elevation;
		this.constellationName = constellationName;
		this.apparentMagnitude = apparentMagnitude;
		this.distanceAu = distanceAu;
		this.litPercent = litPercent;
	}

	public String getName()
	{
		return name;
	}

	public String getRiseText()
	{
		return riseText;
	}

	public String getTransitText()
	{
		return transitText;
	}

	public String getSetText()
	{
		return setText;
	}

	public Double getRiseBearing()
	{
		return riseBearing;
	}

	public Double getTransitElevation()
	{
		return transitElevation;
	}

	public Double getBearing()
	{
		return bearing;
	}

	public Double getElevation()
	{
		return elevation;
	}

	public String getConstellationName()
	{
		return constellationName;
	}

	public Double getApparentMagnitude()
	{
		return apparentMagnitude;
	}

	public double getDistanceAu()
	{
		return distanceAu;
	}

	public long getLitPercent()
	{
		return litPercent;
	}
}
