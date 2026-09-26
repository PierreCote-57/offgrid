package com.lc.offgrid.webapp.mcp;

import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsBody;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMoment;
import com.lc.offgrid.common.misc.astronomy.planet.HorizonsMomentName;
import com.lc.offgrid.common.misc.sky.SkyBodyAnalyser;
import com.lc.offgrid.common.misc.sky.SkyBodyDay;
import com.lc.offgrid.webapp.pojo.sky.SkyBodyListMcpAnswer;
import com.lc.offgrid.webapp.pojo.sky.SkyBodyMcpAnswer;
import com.lc.offgrid.webapp.pojo.sky.SkyDarknessMcpAnswer;
import com.lc.offgrid.common.misc.OffgridUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What the sky does for one observer on one day, read off the ephemeris.
 */
@Component
public class SkyTool extends AbstractOffgridMCP
{
	/** Every time the sky answers is on the observer's clock, to the minute. */
	private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private static final String DATE_DESCRIPTION =
			"The day, written YYYY-MM-DD. Absent, it is today where the observer is";
	private static final String TIME_DESCRIPTION =
			"The time of day, written HH:mm on a 24 hour clock. State it to be told where each "
			+ "body stands at that moment; absent, the answer is the day's rises and sets alone";
	private static final String LATITUDE_DESCRIPTION =
			"The observer's latitude in degrees, north positive. Absent, the time zone's own place";
	private static final String LONGITUDE_DESCRIPTION =
			"The observer's longitude in degrees, east positive. Absent, the time zone's own place";
	private static final String TIME_ZONE_DESCRIPTION =
			"The observer's time zone, named as the tz database does, like America/Vancouver. "
			+ "Absent, America/Vancouver. Every time in the answer is on this clock";

	/**
	 * Sunset, the sunrise that ends that night, and the Moon that is up in between. The
	 * parameters arrive as text so a value that does not parse falls back rather than failing
	 * the call.
	 */
	@McpTool(name = "dark-tonight",
			description = "When it gets dark at a place on a night, and how much moon is in it",
			generateOutputSchema = true)
	public SkyDarknessMcpAnswer getDarkTonight(
			@McpToolParam(description = DATE_DESCRIPTION, required = false) String date,
			@McpToolParam(description = LATITUDE_DESCRIPTION, required = false) String lat,
			@McpToolParam(description = LONGITUDE_DESCRIPTION, required = false) String lng,
			@McpToolParam(description = TIME_ZONE_DESCRIPTION, required = false) String timezone)
	{
		ZoneId		timeZone	= readTimeZone(timezone);
		double		latitude	= OffgridUtil.parseLatitude(lat, timeZone);
		double		longitude	= OffgridUtil.parseLongitude(lng, timeZone);
		LocalDate	localDate	= OffgridUtil.parseDate(date, timeZone);

		logMcpCall("getDarkTonight(%s, %s, %s, %s)", localDate, latitude, longitude, timeZone);

		SkyBodyAnalyser	analyser	= makeAnalyser(localDate, null, timeZone, latitude, longitude);
		SkyBodyDay		sunDay		= analyser.getSkyBodyDay(HorizonsBody.SUN, timeZone);
		SkyBodyDay		moonDay		= analyser.getSkyBodyDay(HorizonsBody.MOON, timeZone);

		// The sunrise that ends the night is the next day's, which is a second read of the ephemeris.
		SkyBodyAnalyser	nextAnalyser	= makeAnalyser(localDate.plusDays(1), null, timeZone, latitude, longitude);
		SkyBodyDay		sunNextDay		= nextAnalyser.getSkyBodyDay(HorizonsBody.SUN, timeZone);

		HorizonsMoment	sunset		= sunDay.getMomentMap().get(HorizonsMomentName.SET);
		HorizonsMoment	sunrise		= sunNextDay.getMomentMap().get(HorizonsMomentName.RISE);

		SkyDarknessMcpAnswer answer = new SkyDarknessMcpAnswer(localDate, timeZone, latitude, longitude,
				formatMoment(sunset, timeZone),
				formatMoment(sunrise, timeZone),
				formatDuration(sunset, sunrise),
				formatMoment(moonDay.getMomentMap().get(HorizonsMomentName.RISE), timeZone),
				formatMoment(moonDay.getMomentMap().get(HorizonsMomentName.SET), timeZone),
				makePercent(moonDay.getLitFraction()));
		return answer;
	}

	/**
	 * Every body the ephemeris covers, on the day and at the place asked about. A time of day is
	 * the one parameter the other tool does not take: it decides where each body stands, and
	 * darkness is a question about the whole night.
	 */
	@McpTool(name = "sky-bodies",
			description = "The Sun, the Moon and the planets at a place on a day: "
					+ "when each rises, transits and sets, and where to look",
			generateOutputSchema = true)
	public SkyBodyListMcpAnswer getSkyBodyList(
			@McpToolParam(description = DATE_DESCRIPTION, required = false) String date,
			@McpToolParam(description = TIME_DESCRIPTION, required = false) String time,
			@McpToolParam(description = LATITUDE_DESCRIPTION, required = false) String lat,
			@McpToolParam(description = LONGITUDE_DESCRIPTION, required = false) String lng,
			@McpToolParam(description = TIME_ZONE_DESCRIPTION, required = false) String timezone)
	{
		ZoneId		timeZone	= readTimeZone(timezone);
		double		latitude	= OffgridUtil.parseLatitude(lat, timeZone);
		double		longitude	= OffgridUtil.parseLongitude(lng, timeZone);
		LocalDate	localDate	= OffgridUtil.parseDate(date, timeZone);
		LocalTime	localTime	= OffgridUtil.parseTime(time);

		logMcpCall("getSkyBodyList(%s, %s, %s, %s, %s)",
				localDate, localTime, latitude, longitude, timeZone);

		SkyBodyAnalyser					analyser	= makeAnalyser(localDate, localTime, timeZone, latitude, longitude);
		Map<HorizonsBody, SkyBodyDay>	bodyDayMap	= analyser.getSkyBodyDayMap(timeZone);

		List<SkyBodyMcpAnswer> bodyList = new ArrayList<>();
		for (Map.Entry<HorizonsBody, SkyBodyDay> entry : bodyDayMap.entrySet())
		{
			SkyBodyMcpAnswer bodyAnswer = makeBodyAnswer(entry.getKey(), entry.getValue(),
					localTime, timeZone);
			bodyList.add(bodyAnswer);
		}

		SkyBodyListMcpAnswer answer = new SkyBodyListMcpAnswer(localDate, localTime, timeZone,
				latitude, longitude, bodyList);
		return answer;
	}

	/**
	 * A null localTime leaves the two angles off the row: the analyser was built at noon to get
	 * the date right, and noon's angles are not an answer to a question nobody asked.
	 */
	private static SkyBodyMcpAnswer makeBodyAnswer(HorizonsBody body, SkyBodyDay bodyDay,
			LocalTime localTime, ZoneId timeZone)
	{
		HorizonsMoment	rise		= bodyDay.getMomentMap().get(HorizonsMomentName.RISE);
		HorizonsMoment	transit		= bodyDay.getMomentMap().get(HorizonsMomentName.TRANSIT);
		HorizonsMoment	set			= bodyDay.getMomentMap().get(HorizonsMomentName.SET);
		HorizonsMoment	now			= null == localTime
				? null
				: bodyDay.getMomentMap().get(HorizonsMomentName.NOW);
		Constellation	constellation	= bodyDay.getConstellation();

		SkyBodyMcpAnswer bodyAnswer = new SkyBodyMcpAnswer(body.getDisplayName(),
				formatMoment(rise, timeZone),
				formatMoment(transit, timeZone),
				formatMoment(set, timeZone),
				null == rise ? null : rise.getBearing(),
				null == transit ? null : transit.getElevation(),
				null == now ? null : now.getBearing(),
				null == now ? null : now.getElevation(),
				null == constellation ? null : constellation.getEnglishName(),
				bodyDay.getApparentMagnitude(),
				bodyDay.getDistanceAu(),
				makePercent(bodyDay.getLitFraction()));
		return bodyAnswer;
	}

	/**
	 * The zone the answer is stated on, never null: a name that is not a zone falls back the
	 * same way an absent one does.
	 */
	private static ZoneId readTimeZone(String timeZoneText)
	{
		ZoneId timeZone = OffgridUtil.parseTimeZone(timeZoneText);
		timeZone = null == timeZone ? ZoneId.of(OffgridUtil.getDefaultTimeZone()) : timeZone;
		return timeZone;
	}

	/**
	 * A null localTime is noon, so the day the analyser reads is the day asked for whatever the
	 * zone's offset does. Rise, transit and set come off the whole day either way; the time only
	 * decides where the NOW moment falls.
	 */
	private static SkyBodyAnalyser makeAnalyser(LocalDate localDate, LocalTime localTime,
			ZoneId timeZone, double latitude, double longitude)
	{
		LocalTime	analyserTime	= null == localTime ? LocalTime.NOON : localTime;
		Instant		instant			= localDate.atTime(analyserTime).atZone(timeZone).toInstant();
		SkyBodyAnalyser analyser = new SkyBodyAnalyser(instant, latitude, longitude);
		return analyser;
	}

	private static String formatMoment(HorizonsMoment moment, ZoneId timeZone)
	{
		String text = null;
		if (null != moment)
		{
			ZonedDateTime dateTime = Instant.ofEpochSecond(moment.getEpochSecond()).atZone(timeZone);
			text = dateTime.format(CLOCK_FORMAT);
		}
		return text;
	}

	private static String formatDuration(HorizonsMoment fromMoment, HorizonsMoment toMoment)
	{
		String text = null;
		if (null != fromMoment && null != toMoment)
		{
			Duration duration = Duration.ofSeconds(toMoment.getEpochSecond() - fromMoment.getEpochSecond());
			text = String.format("%dh %02dm", duration.toHours(), duration.toMinutesPart());
		}
		return text;
	}

	private static long makePercent(double fraction)
	{
		long percent = Math.round(fraction * 100);
		return percent;
	}
}
