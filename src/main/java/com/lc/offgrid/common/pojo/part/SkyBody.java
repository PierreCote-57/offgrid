package com.lc.offgrid.common.pojo.part;

import com.lc.basics.tools.units.TimeUnits;

/**
 * One body on the sky page: a dot on the chart, and a row in the table. Its orbit is a circle
 * drawn at its parent's dot, so the Moon's circle around Earth is the same thing as Earth's
 * circle around the Sun. The root body has no parent, no orbit and no arrowhead.
 *
 * A body with no rise time is drawn but not tabled.
 */
public class SkyBody
{
	private String		name;
	private SkyBody		parent;
	private double		orbitRadius;
	private boolean		clockwise;

	private double		dotX;
	private double		dotY;
	private double		dotRadius;
	private String		colourClass;

	private String		labelText;
	private double		labelX;
	private double		labelY;
	private String		labelAnchor;

	private String		arrowheadPoints;

	private Double		orbitPeriod;
	private String		rises;
	private String		transit;
	private String		sets;

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public SkyBody getParent()
	{
		return parent;
	}
	public void setParent(SkyBody parent)
	{
		this.parent = parent;
	}

	public double getOrbitRadius()
	{
		return orbitRadius;
	}
	public void setOrbitRadius(double orbitRadius)
	{
		this.orbitRadius = orbitRadius;
	}

	public boolean isClockwise()
	{
		return clockwise;
	}
	public void setClockwise(boolean clockwise)
	{
		this.clockwise = clockwise;
	}

	public double getDotX()
	{
		return dotX;
	}
	public void setDotX(double dotX)
	{
		this.dotX = dotX;
	}

	public double getDotY()
	{
		return dotY;
	}
	public void setDotY(double dotY)
	{
		this.dotY = dotY;
	}

	public double getDotRadius()
	{
		return dotRadius;
	}
	public void setDotRadius(double dotRadius)
	{
		this.dotRadius = dotRadius;
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

	public double getLabelX()
	{
		return labelX;
	}
	public void setLabelX(double labelX)
	{
		this.labelX = labelX;
	}

	public double getLabelY()
	{
		return labelY;
	}
	public void setLabelY(double labelY)
	{
		this.labelY = labelY;
	}

	public String getLabelAnchor()
	{
		return labelAnchor;
	}
	public void setLabelAnchor(String labelAnchor)
	{
		this.labelAnchor = labelAnchor;
	}

	/**
	 * The label, set as one thing because its text, its position and its anchor are decided
	 * together.
	 */
	public void setLabel(String labelText, double labelX, double labelY, String labelAnchor)
	{
		setLabelText(labelText);
		setLabelX(labelX);
		setLabelY(labelY);
		setLabelAnchor(labelAnchor);
	}

	public String getArrowheadPoints()
	{
		return arrowheadPoints;
	}
	public void setArrowheadPoints(String arrowheadPoints)
	{
		this.arrowheadPoints = arrowheadPoints;
	}

	public Double getOrbitPeriod()
	{
		return orbitPeriod;
	}
	public void setOrbitPeriod(Double orbitPeriod)
	{
		this.orbitPeriod = orbitPeriod;
	}

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

	/**
	 * The orbit period in the largest unit that keeps it above one, or an empty cell for a body
	 * that orbits nothing.
	 */
	public String getOrbitPeriodText()
	{
		if (null == getOrbitPeriod())
		{
			return "";
		}

		String periodText = TimeUnits.DAY.format(getOrbitPeriod());
		return periodText;
	}
}
