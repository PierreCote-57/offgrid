package com.lc.offgrid.common.pojo.part;

/**
 * One body on the sky page: a dot on the chart, and a row in the table.
 *
 * Nothing here depends on how big the chart is drawn. The two radii are fractions of the
 * drawing's width, and where the body actually lands is a BodyPosition, which SkyDataChart
 * works out once it is told a canvas.
 */
public class SkyBody
{
	/*
	 * 1. Astronomy. Facts about the body, from com.lc.basics.tools.astronomy. Nothing here
	 * knows there is a picture or a table.
	 */
	private String		name;
	private Double		orbitPeriod;

	/*
	 * 2. Calculated table data. Worked out for one date and one observer.
	 */
	private String		rises;
	private String		transit;
	private String		sets;

	/*
	 * 3. Fixed chart data. Chosen once and the same on every date and at every size: what the
	 * body orbits, how far out its circle sits, how big its dot is, what colour, and what its
	 * label says. Both radii are fractions of the drawing's width, not lengths.
	 */
	private SkyBody		parent;
	private double		orbitFraction;
	private double		dotFraction;
	private String		colourClass;
	private String		labelText;

	/*
	 * 4. Calculated. The heliocentric longitude for the date, in degrees. It is what turns a
	 * fraction into a position once there is a canvas.
	 */
	private double		longitude;

	// 1. Astronomy

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public Double getOrbitPeriod()
	{
		return orbitPeriod;
	}
	public void setOrbitPeriod(Double orbitPeriod)
	{
		this.orbitPeriod = orbitPeriod;
	}

	// 2. Calculated table data

	public String getRises()
	{
		return rises;
	}
	public void setRises(String rises)
	{
		this.rises = rises;
	}

	public String getTransit()
	{
		return transit;
	}
	public void setTransit(String transit)
	{
		this.transit = transit;
	}

	public String getSets()
	{
		return sets;
	}
	public void setSets(String sets)
	{
		this.sets = sets;
	}

	// 3. Fixed chart data

	public SkyBody getParent()
	{
		return parent;
	}
	public void setParent(SkyBody parent)
	{
		this.parent = parent;
	}

	public double getOrbitFraction()
	{
		return orbitFraction;
	}
	public void setOrbitFraction(double orbitFraction)
	{
		this.orbitFraction = orbitFraction;
	}

	public double getDotFraction()
	{
		return dotFraction;
	}
	public void setDotFraction(double dotFraction)
	{
		this.dotFraction = dotFraction;
	}

	public String getColourClass()
	{
		return colourClass;
	}
	public void setColourClass(String colourClass)
	{
		this.colourClass = colourClass;
	}

	public String getLabelText()
	{
		return labelText;
	}
	public void setLabelText(String labelText)
	{
		this.labelText = labelText;
	}

	// 4. Calculated

	public double getLongitude()
	{
		return longitude;
	}
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
}
