package com.lc.offgrid.common.pojo.part;

import com.lc.offgrid.common.misc.sky.SkyDataMaker;

import java.util.Map;

/**
 * The chart, drawn at whatever width it is asked for.
 *
 * Nothing here is a length until setCanvas is called. The bodies carry fractions; the canvas
 * turns them into a drawing, and the positions come back keyed by body name. Ask for a second
 * width and the map is replaced, so a caller draws between the two calls.
 */
public class SkyDataChart extends SkyData
{
	private String						caption;
	private int							width;
	private Map<String, BodyPosition>	positionMap;

	public String getCaption()
	{
		return caption;
	}
	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public int getWidth()
	{
		return width;
	}

	/**
	 * The drawing is square, so the height is the width plus the band the caption sits in.
	 */
	public double getHeight()
	{
		double height = getWidth() + SkyDataMaker.captionStrip();
		return height;
	}

	public double getCaptionX()
	{
		double captionX = getWidth() / 2.0;
		return captionX;
	}

	public double getCaptionY()
	{
		double captionY = SkyDataMaker.captionBaseline(getWidth());
		return captionY;
	}

	/**
	 * The viewBox attribute, as the svg element wants it.
	 */
	public String getViewBox()
	{
		String viewBoxText = String.format("0 0 %1$s %2$s", getWidth(), getHeight());
		return viewBoxText;
	}

	public Map<String, BodyPosition> getPositionMap()
	{
		return positionMap;
	}

	/**
	 * Draw at this width: every fraction becomes a length, every dot gets a place, and every
	 * label is placed against the others and the bounds. Answers the positions so the caller
	 * can hold them, keyed by body name.
	 */
	public Map<String, BodyPosition> setCanvas(int width)
	{
		this.width = width;

		SkyDataMaker skyDataMaker = new SkyDataMaker();
		this.positionMap = skyDataMaker.placeOnCanvas(this);

		return this.positionMap;
	}
}
