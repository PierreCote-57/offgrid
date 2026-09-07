package com.lc.offgrid.common.misc.sky;

import com.lc.basics.tools.astronomy.Body;
import com.lc.basics.tools.astronomy.EclipticPosition;
import com.lc.basics.tools.astronomy.Ephemeris;
import com.lc.offgrid.common.pojo.part.BodyPosition;
import com.lc.offgrid.common.pojo.part.SkyBody;
import com.lc.offgrid.common.pojo.part.SkyDataChart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes the sky data, in two halves that meet at a canvas.
 *
 * makeBodyList answers what is true of a date whatever size it is drawn at: the astronomy, and
 * the fractions that say how far out each body's circle sits and how big its dot is.
 * placeOnCanvas turns those fractions into a drawing, once a width is known.
 *
 * The astronomy itself is in com.lc.basics.tools.astronomy; everything here is screen work.
 */
public class SkyDataMaker
{
	/**
	 * The space kept clear outside the outermost orbit, in viewBox units. It holds half a
	 * label — 23 for "Neptune" at 12px — so it is a text measurement and does not scale with
	 * the canvas.
	 */
	private static final double MARGIN = 25;

	/** The band under the drawing that holds the caption, and where its baseline sits in it. */
	private static final double CAPTION_STRIP = 25;
	private static final double CAPTION_BASELINE = 15;

	/**
	 * Orbit and dot sizes as fractions. An orbit is a fraction of the width available to the
	 * drawing, so the outermost is 0.5 and fills it; a dot is a fraction of the same width.
	 * The spacing is deliberately not to scale, so the inner planets stay apart.
	 */
	private static final double ORBIT_BASE = 340;
	private static final double DOT_BASE = 380;

	private static final double MOON_ORBIT_RADIUS = 10;
	private static final double MOON_DOT_RADIUS = 2.5;
	private static final double MOON_ORBIT_PERIOD = 27.32;
	private static final double SUN_DOT_RADIUS = 9;

	/** The label's line box. Its height is what decides whether two labels read as stacked. */
	private static final double LABEL_ASCENT = 9;
	private static final double LABEL_DESCENT = 3;
	private static final double LABEL_LINE_HEIGHT = 14;

	/**
	 * Average advance of a 12px system sans character. The chart has no font metrics, so a
	 * label's width is estimated; it decides collisions, never where the text is drawn.
	 */
	private static final double LABEL_CHARACTER_WIDTH = 6.6;

	private static final double VIEW_BOX_MARGIN = 2;

	private static final String EARTH_MOON_LABEL = "Earth+Moon";

	/**
	 * Everything fixed about one of the eight planets, in the order they sit outward from the
	 * Sun. The two radii are the viewBox lengths the chart was drawn at first; they are turned
	 * into fractions below, so changing one here keeps its proportion at any size.
	 */
	private enum Planet
	{
		MERCURY(Body.MERCURY, 20, 4.5, "c-coral", 87.97),
		VENUS(Body.VENUS, 40, 4.5, "c-coral", 224.70),
		EARTH(Body.EARTH, 60, 4.5, "c-coral", 365.26),
		MARS(Body.MARS, 80, 4.5, "c-coral", 686.98),
		JUPITER(Body.JUPITER, 110, 5.5, "c-amber", 4332.59),
		SATURN(Body.SATURN, 130, 5.5, "c-amber", 10759.22),
		URANUS(Body.URANUS, 150, 5.0, "c-teal", 30688.5),
		NEPTUNE(Body.NEPTUNE, 170, 5.0, "c-teal", 60182.0);

		private final Body		body;
		private final double	orbitRadius;
		private final double	dotRadius;
		private final String	colourClass;
		private final double	orbitPeriod;

		Planet(Body body, double orbitRadius, double dotRadius, String colourClass, double orbitPeriod)
		{
			this.body = body;
			this.orbitRadius = orbitRadius;
			this.dotRadius = dotRadius;
			this.colourClass = colourClass;
			this.orbitPeriod = orbitPeriod;
		}
	}

	/**
	 * One candidate position for a label: where the text is anchored, and the box it occupies.
	 */
	private static class LabelBox
	{
		private final double	x;
		private final double	y;
		private final String	anchor;
		private final double	left;
		private final double	top;
		private final double	right;
		private final double	bottom;

		private LabelBox(String text, double x, double y, String anchor)
		{
			this.x = x;
			this.y = y;
			this.anchor = anchor;

			double width = text.length() * LABEL_CHARACTER_WIDTH;
			double boxLeft;
			if ("middle".equals(anchor))
			{
				boxLeft = x - width / 2;
			}
			else if ("start".equals(anchor))
			{
				boxLeft = x;
			}
			else
			{
				boxLeft = x - width;
			}

			this.left = boxLeft;
			this.right = boxLeft + width;
			this.top = y - LABEL_ASCENT;
			this.bottom = y + LABEL_DESCENT;
		}
	}

	/**
	 * The Sun, the Moon and the eight planets on one date. Nothing here depends on how big the
	 * chart will be drawn; the table reads the same list.
	 */
	public List<SkyBody> makeBodyList(LocalDate date)
	{
		double julianDate = Ephemeris.getJulianDate(date);

		SkyBody sun = makeBody("Sun", null, 0, SUN_DOT_RADIUS, "c-sun", 0);

		List<SkyBody> planetList = new ArrayList<>();
		SkyBody earth = null;

		for (Planet planet : Planet.values())
		{
			EclipticPosition position = Ephemeris.getHeliocentric(planet.body, julianDate);
			double bodyLongitude = position.getLongitude();

			SkyBody skyBody = makeBody(planet.body.getDisplayName(), sun, planet.orbitRadius,
					planet.dotRadius, planet.colourClass, bodyLongitude);
			skyBody.setOrbitPeriod(planet.orbitPeriod);
			skyBody.setLabelText(Planet.EARTH == planet ? EARTH_MOON_LABEL : skyBody.getName());
			planetList.add(skyBody);

			if (Planet.EARTH == planet)
			{
				earth = skyBody;
			}
		}

		double moonLongitude = Ephemeris.getMoonLongitude(julianDate);
		SkyBody moon = makeBody("Moon", earth, MOON_ORBIT_RADIUS, MOON_DOT_RADIUS, "c-moon", moonLongitude);
		moon.setOrbitPeriod(MOON_ORBIT_PERIOD);

		List<SkyBody> bodyList = new ArrayList<>();
		bodyList.add(sun);
		bodyList.add(moon);
		bodyList.addAll(planetList);

		return bodyList;
	}

	/**
	 * Where every body lands on a canvas of the chart's width, keyed by body name. The parents
	 * are placed before their children, because a child's circle is centred on its parent's dot.
	 */
	public Map<String, BodyPosition> placeOnCanvas(SkyDataChart skyChart)
	{
		double width = skyChart.getWidth();
		double widthOrbit = width - 2 * MARGIN;
		double centre = width / 2;

		Map<String, BodyPosition> positionMap = new LinkedHashMap<>();
		List<SkyBody> bodyList = skyChart.getBodyList();

		for (SkyBody skyBody : orderedByParent(bodyList))
		{
			BodyPosition position = new BodyPosition();
			position.setOrbitRadius(skyBody.getOrbitFraction() * widthOrbit);
			position.setDotRadius(skyBody.getDotFraction() * widthOrbit);

			SkyBody parent = skyBody.getParent();
			double aroundX = centre;
			double aroundY = centre;
			if (null != parent)
			{
				BodyPosition parentPosition = positionMap.get(parent.getName());
				aroundX = parentPosition.getDotX();
				aroundY = parentPosition.getDotY();
			}

			double radians = Math.toRadians(skyBody.getLongitude());
			position.setDotX(aroundX + position.getOrbitRadius() * Math.cos(radians));
			position.setDotY(aroundY - position.getOrbitRadius() * Math.sin(radians));

			positionMap.put(skyBody.getName(), position);
		}

		placeLabels(bodyList, positionMap, width, skyChart.getHeight());
		return positionMap;
	}

	/**
	 * The bodies with the roots first, so a parent is always placed before its children.
	 */
	private List<SkyBody> orderedByParent(List<SkyBody> bodyList)
	{
		List<SkyBody> orderedList = new ArrayList<>();
		List<SkyBody> waitingList = new ArrayList<>(bodyList);

		while (!waitingList.isEmpty())
		{
			boolean placedOne = false;
			for (int index = 0; index < waitingList.size(); index++)
			{
				SkyBody skyBody = waitingList.get(index);
				SkyBody parent = skyBody.getParent();
				if (null == parent || orderedList.contains(parent))
				{
					orderedList.add(skyBody);
					waitingList.remove(index);
					placedOne = true;
					break;
				}
			}
			if (!placedOne)
			{
				// A cycle, or a parent that is not in the list. Draw what is left where it is.
				orderedList.addAll(waitingList);
				break;
			}
		}
		return orderedList;
	}

	private SkyBody makeBody(String name, SkyBody parent, double orbitRadius, double dotRadius,
			String colourClass, double longitude)
	{
		SkyBody skyBody = new SkyBody();
		skyBody.setName(name);
		skyBody.setParent(parent);
		skyBody.setOrbitFraction(orbitRadius / ORBIT_BASE);
		skyBody.setDotFraction(dotRadius / DOT_BASE);
		skyBody.setColourClass(colourClass);
		skyBody.setLongitude(longitude);
		return skyBody;
	}

	/**
	 * The caption's baseline, and the floor a label may not cross.
	 */
	public static double captionBaseline(double width)
	{
		double baseline = width + CAPTION_BASELINE;
		return baseline;
	}

	public static double captionStrip()
	{
		return CAPTION_STRIP;
	}

	/**
	 * Place every planet's label. Earth and the Moon take one label between them, because two
	 * labels ten pixels apart collide wherever each is put, and the pair's effective radius is
	 * the Moon's circle rather than Earth's dot.
	 */
	private void placeLabels(List<SkyBody> bodyList, Map<String, BodyPosition> positionMap,
			double width, double height)
	{
		SkyBody moon = findByName(bodyList, "Moon");
		BodyPosition moonPosition = positionMap.get("Moon");
		List<LabelBox> placedList = new ArrayList<>();

		for (SkyBody skyBody : bodyList)
		{
			String text = skyBody.getLabelText();
			if (null == text)
			{
				continue;
			}

			BodyPosition position = positionMap.get(skyBody.getName());
			boolean isEarth = EARTH_MOON_LABEL.equals(text);
			double clearRadius = isEarth ? moonPosition.getOrbitRadius() : position.getDotRadius();

			LabelBox chosen = choosePosition(text, position, clearRadius, bodyList, positionMap,
					placedList, skyBody, isEarth ? moon : null, width, height);
			placedList.add(chosen);
			position.setLabel(chosen.x, chosen.y, chosen.anchor);
		}
	}

	private SkyBody findByName(List<SkyBody> bodyList, String name)
	{
		for (SkyBody skyBody : bodyList)
		{
			if (name.equals(skyBody.getName()))
			{
				return skyBody;
			}
		}
		return null;
	}

	/**
	 * The first candidate that clears every dot, every label already placed, and the viewBox.
	 * When none does, the first one is used and the chart is drawn crowded rather than blank.
	 */
	private LabelBox choosePosition(String text, BodyPosition position, double clearRadius,
			List<SkyBody> bodyList, Map<String, BodyPosition> positionMap, List<LabelBox> placedList,
			SkyBody own, SkyBody ownMoon, double width, double height)
	{
		List<LabelBox> candidateList = makeCandidates(text, position.getDotX(), position.getDotY(), clearRadius);

		for (LabelBox candidate : candidateList)
		{
			if (!insideViewBox(candidate, width, height))
			{
				continue;
			}
			if (hitsAnyDot(candidate, bodyList, positionMap, own, ownMoon))
			{
				continue;
			}
			if (hitsAnyLabel(candidate, placedList))
			{
				continue;
			}
			return candidate;
		}

		return candidateList.get(0);
	}

	/**
	 * The eight places a label may go. The first four each overlap the dot on one axis, above
	 * and below sharing its x, right and left sharing its y. The last four slide above and
	 * below sideways while keeping some of that overlap, which is how a label escapes a crowded
	 * neighbour without going diagonal. All the offsets are in text units: they exist because
	 * the font is 12px and stays 12px whatever the canvas.
	 */
	private List<LabelBox> makeCandidates(String text, double dotX, double dotY, double clearRadius)
	{
		double above = dotY - (clearRadius + 5);
		double below = dotY + (clearRadius + 11);

		List<LabelBox> candidateList = new ArrayList<>();
		candidateList.add(new LabelBox(text, dotX, above, "middle"));
		candidateList.add(new LabelBox(text, dotX, below, "middle"));
		candidateList.add(new LabelBox(text, dotX + clearRadius + 3, dotY + 4, "start"));
		candidateList.add(new LabelBox(text, dotX - clearRadius - 3, dotY + 4, "end"));
		candidateList.add(new LabelBox(text, dotX - clearRadius, above, "start"));
		candidateList.add(new LabelBox(text, dotX + clearRadius, above, "end"));
		candidateList.add(new LabelBox(text, dotX - clearRadius, below, "start"));
		candidateList.add(new LabelBox(text, dotX + clearRadius, below, "end"));
		return candidateList;
	}

	private boolean insideViewBox(LabelBox candidate, double width, double height)
	{
		double floor = height - CAPTION_STRIP - LABEL_LINE_HEIGHT;
		boolean inside = candidate.left >= VIEW_BOX_MARGIN
				&& candidate.right <= width - VIEW_BOX_MARGIN
				&& candidate.top >= VIEW_BOX_MARGIN
				&& candidate.bottom <= floor;
		return inside;
	}

	private boolean hitsAnyDot(LabelBox candidate, List<SkyBody> bodyList,
			Map<String, BodyPosition> positionMap, SkyBody own, SkyBody ownMoon)
	{
		for (SkyBody other : bodyList)
		{
			if (other == own || other == ownMoon)
			{
				continue;
			}
			if (hitsDot(candidate, positionMap.get(other.getName())))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hitsDot(LabelBox candidate, BodyPosition other)
	{
		double nearestX = Math.min(Math.max(other.getDotX(), candidate.left), candidate.right);
		double nearestY = Math.min(Math.max(other.getDotY(), candidate.top), candidate.bottom);
		double distance = Math.hypot(nearestX - other.getDotX(), nearestY - other.getDotY());
		boolean hits = distance < other.getDotRadius();
		return hits;
	}

	/**
	 * Two labels clash either by overlapping outright, or by sharing a column and sitting
	 * within a line of each other. The second is the one a box test misses: the boxes never
	 * touch, and the two still read as one stacked block.
	 */
	private boolean hitsAnyLabel(LabelBox candidate, List<LabelBox> placedList)
	{
		for (LabelBox placed : placedList)
		{
			boolean sharesColumn = candidate.left < placed.right && placed.left < candidate.right;
			if (!sharesColumn)
			{
				continue;
			}

			double verticalGap = Math.max(candidate.top - placed.bottom, placed.top - candidate.bottom);
			if (verticalGap < LABEL_LINE_HEIGHT)
			{
				return true;
			}
		}
		return false;
	}
}
