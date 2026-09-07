package com.lc.offgrid.common.pojo.part;

import java.util.List;
import java.util.Locale;

/**
 * The solar system chart on the sky page. It carries the canvas and the bodies; where the
 * centre is comes from the root body's dot, not from a field here.
 *
 * The bodies are drawn in three passes over the one list — orbits, dots, then labels — so a
 * body's position in the list decides nothing.
 */
public class SkyChart
{
	private int				viewBoxWidth;
	private int				viewBoxHeight;
	private String			title;
	private String			description;
	private String			caption;
	private int				captionX;
	private int				captionY;
	private List<SkyBody>	bodyList;

	private double			latitude;
	private double			longitude;
	private String			timeZoneName;
	private String			dateText;

	public int getViewBoxWidth()
	{
		return viewBoxWidth;
	}
	public void setViewBoxWidth(int viewBoxWidth)
	{
		this.viewBoxWidth = viewBoxWidth;
	}

	public int getViewBoxHeight()
	{
		return viewBoxHeight;
	}
	public void setViewBoxHeight(int viewBoxHeight)
	{
		this.viewBoxHeight = viewBoxHeight;
	}

	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getCaption()
	{
		return caption;
	}
	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public int getCaptionX()
	{
		return captionX;
	}
	public void setCaptionX(int captionX)
	{
		this.captionX = captionX;
	}

	public int getCaptionY()
	{
		return captionY;
	}
	public void setCaptionY(int captionY)
	{
		this.captionY = captionY;
	}

	public List<SkyBody> getBodyList()
	{
		return bodyList;
	}
	public void setBodyList(List<SkyBody> bodyList)
	{
		this.bodyList = bodyList;
	}

	public double getLatitude()
	{
		return latitude;
	}
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public String getTimeZoneName()
	{
		return timeZoneName;
	}
	public void setTimeZoneName(String timeZoneName)
	{
		this.timeZoneName = timeZoneName;
	}

	public String getDateText()
	{
		return dateText;
	}
	public void setDateText(String dateText)
	{
		this.dateText = dateText;
	}

	/**
	 * Where the observer stands. A southern latitude or an eastern longitude carries its own
	 * letter, so the numbers are always written unsigned.
	 */
	public String getCoordinateText()
	{
		char latitudeLetter = 0 <= getLatitude() ? 'N' : 'S';
		char longitudeLetter = 0 <= getLongitude() ? 'E' : 'W';

		String coordinateText = String.format(Locale.US, "%1$.4f\u00b0%2$s, %3$.4f\u00b0%4$s",
				Math.abs(getLatitude()), latitudeLetter, Math.abs(getLongitude()), longitudeLetter);
		return coordinateText;
	}

	/**
	 * The viewBox attribute, as the svg element wants it.
	 */
	public String getViewBox()
	{
		String viewBoxText = String.format("0 0 %1$s %2$s", getViewBoxWidth(), getViewBoxHeight());
		return viewBoxText;
	}
}
