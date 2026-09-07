package com.lc.offgrid.common.pojo.part;

/**
 * Where one body lands on a canvas of a given width. Everything here is in viewBox units and
 * means nothing without the canvas that produced it, which is why it is not on SkyBody: the
 * same body drawn at two sizes has two of these.
 */
public class BodyPosition
{
	private double	orbitRadius;
	private double	dotX;
	private double	dotY;
	private double	dotRadius;
	private double	labelX;
	private double	labelY;
	private String	labelAnchor;

	public double getOrbitRadius()
	{
		return orbitRadius;
	}
	public void setOrbitRadius(double orbitRadius)
	{
		this.orbitRadius = orbitRadius;
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

	public void setLabel(double labelX, double labelY, String labelAnchor)
	{
		setLabelX(labelX);
		setLabelY(labelY);
		setLabelAnchor(labelAnchor);
	}
}
