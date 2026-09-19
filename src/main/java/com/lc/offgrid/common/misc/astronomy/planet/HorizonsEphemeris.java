package com.lc.offgrid.common.misc.astronomy.planet;

import com.lc.offgrid.common.misc.astronomy.constellation.Constellation;
import com.lc.offgrid.webapp.spring.site.OffgridUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Where a body is, at a moment, from the JPL Horizons files on disk.
 *
 * One of these is built for one date and reads, there and then, the rows of every body for the
 * three days on each side of it. That window covers a local day for any observer on Earth: the
 * world's offsets run from -12 to +14, so the widest local day still ends inside date+2. Three
 * is the paranoid two.
 *
 * Loading at construction is what settles, once, whether one year's file is enough or two are
 * needed. After it, nothing reads a file and nothing is lazy.
 *
 * The files hold one row a day. Which file a body's year sits in, which two rows bracket a
 * moment and how they are interpolated are this class's own business: nothing outside it ever
 * holds a row.
 *
 * The moments are instants, with no zone. The same instant gives the same position for every
 * observer, so a caller holding a local day converts it here and not before. A moment outside
 * the window is a caller's mistake and throws.
 *
 * One of these serves one request and then goes away. It is not a bean, so the local folder
 * comes from OffgridUtil, which holds it statically.
 */
public class HorizonsEphemeris
{
	/** Where the years sit under the local folder. */
	private static final String EPHEMERIS_FOLDER = "ephemeris";

	/** The path of one body's file, under the local folder: year, then body. */
	private static final String FILE_PATH = "%1$s/%2$s/%3$d/%4$s.csv";

	/** The folder the years sit in, under the local folder. */
	private static final String YEAR_FOLDER_PATH = "%1$s/%2$s";

	/** What readYear answers for a name that is not a year. */
	private static final int NO_YEAR = -1;

	/** How many days on each side of the date are read. */
	private static final int WINDOW_DAY_COUNT = 3;

	/** Two rows are the least an interpolation can work from. */
	private static final int MINIMUM_ROW_COUNT = 2;

	/** Greenwich sidereal time at J2000 in degrees, and how much of it a day adds. */
	private static final double SIDEREAL_AT_J2000 = 280.46061837;
	private static final double SIDEREAL_PER_DAY = 360.98564736629;

	/** The Julian Date of J2000, and of the moment a millisecond count starts from. */
	private static final double JULIAN_AT_J2000 = 2451545.0;
	private static final double JULIAN_AT_EPOCH = 2440587.5;
	private static final double MILLIS_PER_DAY = 86400000.0;

	private static final double DEGREES_AROUND = 360.0;

	private final Instant						fromInstant;
	private final Instant						toInstant;
	private final Map<HorizonsBody, List<HorizonsRow>>	rowListMap;

	/**
	 * Reads every body's rows for the date and the three days on each side of it.
	 */
	public HorizonsEphemeris(Instant instant) throws IOException, ParseException
	{
		this.fromInstant = instant.minus(3, ChronoUnit.DAYS);
		this.toInstant = instant.plus(3, ChronoUnit.DAYS);;
		this.rowListMap = readRowListMap(fromInstant.atZone(ZoneOffset.UTC).getYear(), toInstant.atZone(ZoneOffset.UTC).getYear());
	}

	/**
	 * The folder the ephemeris files are read from, which is {@code folder.local}.
	 */
	public static String getDataRootFolder()
	{
		return OffgridUtil.getDataRootFolder();
	}

	/**
	 * The first and last year there are ephemeris files for, read from the folder the years sit
	 * in. A name that is not a year is skipped, and a folder with nothing in it answers null:
	 * there is no range to state, and a pair of zeroes would read as one.
	 */
	public static int[] getYearRange()
	{
		String rootFolder = getDataRootFolder();
		String path = String.format(YEAR_FOLDER_PATH, rootFolder, EPHEMERIS_FOLDER);
		File[] fileList = new File(path).listFiles();

		int firstYear = Integer.MAX_VALUE;
		int lastYear = Integer.MIN_VALUE;

		if (null != fileList)
		{
			for (File file : fileList)
			{
				int year = readYear(file);
				if (NO_YEAR == year)
				{
					continue;
				}
				firstYear = Math.min(firstYear, year);
				lastYear = Math.max(lastYear, year);
			}
		}

		int[] yearRange = lastYear < firstYear
				? null
				: new int[] {firstYear, lastYear};
		return yearRange;
	}

	/**
	 * The year a folder is named for, or NO_YEAR when it is not a folder or its name is not a
	 * number.
	 */
	private static int readYear(File file)
	{
		if (!file.isDirectory())
		{
			return NO_YEAR;
		}

		String name = file.getName();
		int year;
		try
		{
			year = Integer.parseInt(name);
		}
		catch (NumberFormatException failure)
		{
			return NO_YEAR;
		}
		return year;
	}

	/**
	 * The first moment this ephemeris answers for.
	 */
	public Instant getFromInstant()
	{
		return fromInstant;
	}

	/**
	 * The last moment this ephemeris answers for.
	 */
	public Instant getToInstant()
	{
		return toInstant;
	}

	/**
	 * Where a body is at one moment, interpolated between the two rows that bracket it.
	 */
	public HorizonsPosition getPosition(HorizonsBody body, Instant instant)
	{
		checkInstant(instant);

		List<HorizonsRow> rowList = getRowList(body);
		int index = getBracketIndex(rowList, instant);
		HorizonsRow firstRow = rowList.get(index);
		HorizonsRow secondRow = rowList.get(index + 1);

		HorizonsPosition position = interpolate(firstRow, secondRow, instant);
		return position;
	}

	/**
	 * Where a body is at every step from one moment to another, both ends included when the
	 * step lands on them.
	 */
	public List<HorizonsPosition> getPositionList(HorizonsBody body, Instant startInstant, Instant endInstant, Duration step)
	{
		checkInstant(startInstant);
		checkInstant(endInstant);
		checkStep(step);

		List<HorizonsPosition> positionList = new ArrayList<>();
		Instant instant = startInstant;

		while (!instant.isAfter(endInstant))
		{
			HorizonsPosition position = getPosition(body, instant);
			positionList.add(position);
			instant = instant.plus(step);
		}

		return positionList;
	}

	/**
	 * Where a body stands for an observer at one moment: which way to turn, and how far up.
	 * The latitude and longitude are the observer's place, in degrees, north and east positive.
	 *
	 * The direction is the one the files state, turned from the star background onto the
	 * observer's horizon: no refraction, no disc and no parallax, so a body is a point.
	 */
	public HorizonsMoment getMoment(HorizonsBody body, Instant instant, double latitude, double longitude)
	{
		HorizonsPosition position = getPosition(body, instant);

		double siderealTime = getLocalSiderealTime(instant, longitude);
		double hourAngle = normalise(siderealTime - position.getRightAscension());
		double hourAngleRadians = Math.toRadians(hourAngle);
		double declinationRadians = Math.toRadians(position.getDeclination());
		double latitudeRadians = Math.toRadians(latitude);

		double north = Math.sin(declinationRadians) * Math.cos(latitudeRadians)
				- Math.cos(declinationRadians) * Math.cos(hourAngleRadians) * Math.sin(latitudeRadians);
		double east = -Math.cos(declinationRadians) * Math.sin(hourAngleRadians);
		double up = Math.sin(declinationRadians) * Math.sin(latitudeRadians)
				+ Math.cos(declinationRadians) * Math.cos(hourAngleRadians) * Math.cos(latitudeRadians);

		double bearingDegrees = Math.toDegrees(Math.atan2(east, north));
		double bearing = normalise(bearingDegrees);
		double elevation = Math.toDegrees(Math.asin(clamp(up)));

		HorizonsMoment moment = new HorizonsMoment(instant.getEpochSecond(), bearing, elevation);
		return moment;
	}

	/**
	 * Every body's rows for the years the window spans, which is one year or two. A body there
	 * is no ephemeris for has no file to read and is not in the map.
	 */
	private Map<HorizonsBody, List<HorizonsRow>> readRowListMap(int firstYear, int lastYear) throws IOException, ParseException
	{
		Map<HorizonsBody, List<HorizonsRow>> map = new EnumMap<>(HorizonsBody.class);

		for (HorizonsBody body : HorizonsBody.values())
		{
			if (!body.hasEphemeris())
			{
				continue;
			}

			List<HorizonsRow> rowList = readRowList(body, firstYear, lastYear);
			map.put(body, rowList);
		}

		return map;
	}

	/**
	 * One body's rows inside the window, in the order the files hold them.
	 */
	private List<HorizonsRow> readRowList(HorizonsBody body, int firstYear, int lastYear) throws IOException, ParseException
	{
		List<HorizonsRow> rowList = new ArrayList<>();

		for (int year = firstYear; year <= lastYear; year++)
		{
			File file = getFile(body, year);
			if (!file.exists())
			{
				String message = String.format("No ephemeris file for %1$s in %2$d: %3$s", body, year, file.getPath());
				throw new FileNotFoundException(message);
			}

			List<String> lineList = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			addWindowRowList(lineList, rowList);
		}

		if (rowList.size() < MINIMUM_ROW_COUNT)
		{
			String message = String.format("%1$s has %2$d rows between %3$s and %4$s, which cannot be interpolated",
					body, rowList.size(), fromInstant, toInstant);
			throw new IOException(message);
		}

		return rowList;
	}

	/**
	 * Parses the lines that are rows and keeps the ones inside the window. The column header
	 * Horizons writes above them is not a row, and neither is a blank line.
	 */
	private void addWindowRowList(List<String> lineList, List<HorizonsRow> rowList) throws ParseException
	{
		for (String line : lineList)
		{
			if (line.isBlank() || line.startsWith(HorizonsRow.HEADER_PREFIX))
			{
				continue;
			}

			HorizonsRow row = new HorizonsRow(line);
			Instant rowInstant = row.getDate().toInstant();
			if (rowInstant.isBefore(fromInstant) || rowInstant.isAfter(toInstant))
			{
				continue;
			}

			rowList.add(row);
		}
	}

	/**
	 * One body's rows, as they were read.
	 */
	private List<HorizonsRow> getRowList(HorizonsBody body)
	{
		List<HorizonsRow> rowList = rowListMap.get(body);
		return rowList;
	}

	/**
	 * The index of the first row of the pair that brackets a moment. A moment sitting on the
	 * last row is answered by the last pair.
	 */
	private int getBracketIndex(List<HorizonsRow> rowList, Instant instant)
	{
		int lastPairIndex = rowList.size() - 2;

		for (int index = 0; index < lastPairIndex; index++)
		{
			HorizonsRow nextRow = rowList.get(index + 1);
			Instant nextInstant = nextRow.getDate().toInstant();
			if (!instant.isAfter(nextInstant))
			{
				return index;
			}
		}

		return lastPairIndex;
	}

	/**
	 * The position between two rows at a moment. The two directions are turned into vectors
	 * from the Earth's centre, the vectors are interpolated, and the result is turned back:
	 * interpolating right ascension on its own would run the wrong way around zero. The
	 * constellation and the magnitude come from the closer of the two rows: one is a name, and
	 * the other is missing wherever the model does not cover the phase angle.
	 */
	private HorizonsPosition interpolate(HorizonsRow firstRow, HorizonsRow secondRow, Instant instant)
	{
		Instant firstInstant = firstRow.getDate().toInstant();
		Instant secondInstant = secondRow.getDate().toInstant();
		double spanMilli = Duration.between(firstInstant, secondInstant).toMillis();
		double intoMilli = Duration.between(firstInstant, instant).toMillis();
		double fraction = intoMilli / spanMilli;

		double[] firstVector = toVector(firstRow);
		double[] secondVector = toVector(secondRow);
		double x = firstVector[0] + fraction * (secondVector[0] - firstVector[0]);
		double y = firstVector[1] + fraction * (secondVector[1] - firstVector[1]);
		double z = firstVector[2] + fraction * (secondVector[2] - firstVector[2]);

		double range = Math.sqrt(x * x + y * y + z * z);
		double declination = Math.toDegrees(Math.asin(z / range));
		double rightAscension = Math.toDegrees(Math.atan2(y, x));
		if (rightAscension < 0.0)
		{
			rightAscension = rightAscension + 360.0;
		}

		HorizonsRow closestRow = fraction < 0.5 ? firstRow : secondRow;
		Constellation constellation = closestRow.getConstellation();
		Double apparentMagnitude = closestRow.getApparentMagnitude();

		HorizonsPosition position = new HorizonsPosition(instant, rightAscension, declination, range,
				constellation, apparentMagnitude);
		return position;
	}

	/**
	 * A row's direction and distance as x, y and z from the Earth's centre.
	 */
	private double[] toVector(HorizonsRow row)
	{
		double rightAscension = Math.toRadians(row.getRightAscension());
		double declination = Math.toRadians(row.getDeclination());
		double range = row.getRange();

		double x = range * Math.cos(declination) * Math.cos(rightAscension);
		double y = range * Math.cos(declination) * Math.sin(rightAscension);
		double z = range * Math.sin(declination);

		double[] vector = { x, y, z };
		return vector;
	}

	/**
	 * Sidereal time at a longitude, in degrees: the right ascension that stands due south
	 * there at that moment.
	 */
	private double getLocalSiderealTime(Instant instant, double longitude)
	{
		double julianDate = getJulianDate(instant);
		double dayCount = julianDate - JULIAN_AT_J2000;
		double greenwichDegrees = SIDEREAL_AT_J2000 + SIDEREAL_PER_DAY * dayCount;
		double siderealTime = normalise(greenwichDegrees + longitude);
		return siderealTime;
	}

	/**
	 * The Julian Date at a moment, which carries the time of day as its fraction.
	 */
	private double getJulianDate(Instant instant)
	{
		double milliCount = instant.toEpochMilli();
		double dayCount = milliCount / MILLIS_PER_DAY;
		double julianDate = dayCount + JULIAN_AT_EPOCH;
		return julianDate;
	}

	/**
	 * An angle brought back into 0 to 360.
	 */
	public static double normalise(double degrees)
	{
		double remainder = degrees % DEGREES_AROUND;
		if (remainder < 0.0)
		{
			remainder = remainder + DEGREES_AROUND;
		}
		return remainder;
	}

	/**
	 * A cosine or a sine held inside -1 to 1, where rounding can put it just outside.
	 */
	public static double clamp(double value)
	{
		double clamped = Math.max(-1.0, Math.min(1.0, value));
		return clamped;
	}

	/**
	 * Refuses a moment this ephemeris was not built for.
	 */
	private void checkInstant(Instant instant)
	{
		if (instant.isBefore(fromInstant) || instant.isAfter(toInstant))
		{
			String message = String.format("%s is outside the ephemeris which runs %s to %s",
					instant, fromInstant, toInstant);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Refuses a step that would never reach the end.
	 */
	private void checkStep(Duration step)
	{
		if (step.isZero() || step.isNegative())
		{
			String message = String.format("A step of %1$s never advances", step);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * The file one body's year is stored in, whether or not it is there.
	 */
	private File getFile(HorizonsBody body, int year)
	{
		String rootFolder = getDataRootFolder();
		String fileStem = body.getFileStem();
		String path = String.format(FILE_PATH, rootFolder, EPHEMERIS_FOLDER, year, fileStem);
		File file = new File(path);
		return file;
	}
}
