package com.lc.offgrid.webapp.pojo.sky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * When the night gets dark and how much moon is in it. A moment that does not happen on the
 * day is left out of the answer: above the Arctic circle in summer there is no sunset, and the
 * Moon skips a rise or a set roughly every other day anywhere.
 *
 * Left out rather than null, because the generated output schema types each of these as a
 * string and the client validates against it. A field that can be missing says so with
 * {@code required = false}, which is what SpringAiSchemaModule reads off @JsonProperty.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkyDarknessMcpAnswer extends SkyMcpAnswer
{
	@JsonProperty(required = false)
	private final String	sunsetText;

	/** The morning after, which is why it is read off the next day rather than this one. */
	@JsonProperty(required = false)
	private final String	sunriseText;

	/** Sunset to that sunrise, written as hours and minutes. */
	@JsonProperty(required = false)
	private final String	darkDurationText;

	@JsonProperty(required = false)
	private final String	moonriseText;

	@JsonProperty(required = false)
	private final String	moonsetText;

	/** How much of the Moon's disc is lit, 0 at new and 100 at full. */
	private final long		moonLitPercent;

	public SkyDarknessMcpAnswer(LocalDate localDate, ZoneId timeZone, double latitude, double longitude,
			String sunsetText, String sunriseText, String darkDurationText,
			String moonriseText, String moonsetText, long moonLitPercent)
	{
		super(localDate, timeZone, latitude, longitude);
		this.sunsetText = sunsetText;
		this.sunriseText = sunriseText;
		this.darkDurationText = darkDurationText;
		this.moonriseText = moonriseText;
		this.moonsetText = moonsetText;
		this.moonLitPercent = moonLitPercent;
	}

	public String getSunsetText()
	{
		return sunsetText;
	}

	public String getSunriseText()
	{
		return sunriseText;
	}

	public String getDarkDurationText()
	{
		return darkDurationText;
	}

	public String getMoonriseText()
	{
		return moonriseText;
	}

	public String getMoonsetText()
	{
		return moonsetText;
	}

	public long getMoonLitPercent()
	{
		return moonLitPercent;
	}
}
