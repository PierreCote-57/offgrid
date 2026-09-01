---
name: solar-system-chart
description: Draw a top-down solar system chart with one circle per planetary orbit, a dot marking each planet's true heliocentric position on a given date, and the Moon beside Earth. Use this whenever the user asks to see, draw, chart, or visualize the solar system, planetary positions, planetary alignments, "where the planets are right now", conjunctions, or an orrery-style picture — even if they don't say the words "solar system chart". Positions must be computed, never estimated. For rise and set times rather than a picture, use solar-system-rise-set instead.
---

# Solar system position chart

Produces a single inline SVG: the Sun at centre, eight concentric circles for the orbits
(spacing compressed, not to scale), one filled dot per planet at its actual heliocentric
ecliptic longitude for the requested date, and the Moon beside Earth in its true direction.

## Step 1 — compute the planet positions

Never guess longitudes. Run this script with the correct Julian Date. It uses the JPL
approximate orbital elements (valid 1800–2050) and solves Kepler's equation by Newton
iteration.

```python
import math

# a, e, I, L, longitude_of_perihelion, longitude_of_ascending_node, then rates per Julian century
els = {
 'Mercury':[0.38709927,0.20563593,7.00497902,252.25032350,77.45779628,48.33076593,
            0.00000037,0.00001906,-0.00594749,149472.67411175,0.16047689,-0.12534081],
 'Venus':  [0.72333566,0.00677672,3.39467605,181.97909950,131.60246718,76.67984255,
            0.00000390,-0.00004107,-0.00078890,58517.81538729,0.00268329,-0.27769418],
 'Earth':  [1.00000261,0.01671123,-0.00001531,100.46457166,102.93768193,0.0,
            0.00000562,-0.00004392,-0.01294668,35999.37244981,0.32327364,0.0],
 'Mars':   [1.52371034,0.09339410,1.84969142,-4.55343205,-23.94362959,49.55953891,
            0.00001847,0.00007882,-0.00813131,19140.30268499,0.44441088,-0.29257343],
 'Jupiter':[5.20288700,0.04838624,1.30439695,34.39644051,14.72847983,100.47390909,
            -0.00011607,-0.00013253,-0.00183714,3034.74612775,0.21252668,0.20469106],
 'Saturn': [9.53667594,0.05386179,2.48599187,49.95424423,92.59887831,113.66242448,
            -0.00125060,-0.00050991,0.00193609,1222.49362201,-0.41897216,-0.28867794],
 'Uranus': [19.18916464,0.04725744,0.77263783,313.23810451,170.95427630,74.01692503,
            -0.00196176,-0.00004397,-0.00242939,428.48202785,0.40805281,0.04240589],
 'Neptune':[30.06992276,0.00859048,1.77004347,-55.12002969,44.96476227,131.78422574,
            0.00026291,0.00005105,0.00035372,218.45945325,-0.32241464,-0.00508664],
}

JD = 2461284.5          # <-- set this: Julian Date at 00:00 UT of the target date
T = (JD - 2451545.0) / 36525.0

for name, v in els.items():
    a  = v[0] + v[6]*T
    e  = v[1] + v[7]*T
    I  = v[2] + v[8]*T
    L  = v[3] + v[9]*T
    lp = v[4] + v[10]*T
    ln = v[5] + v[11]*T
    w = lp - ln
    M = (L - lp) % 360
    if M > 180: M -= 360
    Mr = math.radians(M)
    E = Mr + e*math.sin(Mr)
    for _ in range(50):
        E -= (E - e*math.sin(E) - Mr) / (1 - e*math.cos(E))
    xp = a*(math.cos(E) - e)
    yp = a*math.sqrt(1 - e*e)*math.sin(E)
    wr, nr, Ir = math.radians(w), math.radians(ln), math.radians(I)
    x = (math.cos(wr)*math.cos(nr) - math.sin(wr)*math.sin(nr)*math.cos(Ir))*xp \
      + (-math.sin(wr)*math.cos(nr) - math.cos(wr)*math.sin(nr)*math.cos(Ir))*yp
    y = (math.cos(wr)*math.sin(nr) + math.sin(wr)*math.cos(nr)*math.cos(Ir))*xp \
      + (-math.sin(wr)*math.sin(nr) + math.cos(wr)*math.cos(nr)*math.cos(Ir))*yp
    lon = math.degrees(math.atan2(y, x)) % 360
    print(f"{name:8s} lon={lon:7.2f} r={math.hypot(x,y):7.3f}")
```

Julian Date for 00:00 UT on any Gregorian date:

```python
from datetime import date
JD = date(YYYY, M, D).toordinal() + 1721424.5
```

## Step 1b — compute the Moon's direction

The JPL planetary elements do not cover the Moon. Its geocentric ecliptic longitude comes
from a truncated lunar series, good to about 0.3° — far finer than a pixel at this scale.

```python
T = (JD - 2451545.0) / 36525.0
Lp = 218.3164477 + 481267.88123421 * T   # mean longitude
D  = 297.8501921 + 445267.1114034  * T   # mean elongation
M  = 357.5291092 +  35999.0502909  * T   # Sun's mean anomaly
Mp = 134.9633964 + 477198.8675055  * T   # Moon's mean anomaly
F  =  93.2720950 + 483202.0175233  * T   # argument of latitude
r = math.radians
moonLon = (Lp + 6.289 * math.sin(r(Mp))
              + 1.274 * math.sin(r(2*D - Mp))
              + 0.658 * math.sin(r(2*D))
              + 0.214 * math.sin(r(2*Mp))
              - 0.186 * math.sin(r(M))
              - 0.114 * math.sin(r(2*F))) % 360
```

## Step 2 — map longitude to screen coordinates

The view is from ecliptic north, so planets travel counter-clockwise and SVG's downward
y-axis must be inverted.

```
x = cx + R * cos(lon)
y = cy - R * sin(lon)
```

Fixed orbit radii, evenly compressed so the inner planets stay legible (this is why the
drawing is not to scale):

| Body | R (px) | Dot radius | Colour class |
|---|---|---|---|
| Sun | 0 | 9 | c-sun |
| Mercury | 28 | 4.5 | c-coral |
| Venus | 42 | 4.5 | c-coral |
| Earth | 56 | 4.5 | c-coral |
| Moon | Earth + 10 | 2.5 | c-moon |
| Mars | 70 | 4.5 | c-coral |
| Jupiter | 92 | 5.5 | c-amber |
| Saturn | 108 | 5.5 | c-amber |
| Uranus | 124 | 5 | c-teal |
| Neptune | 140 | 5 | c-teal |

Rocky planets share one ramp, gas giants another, ice giants a third. The Sun gets its own
bright yellow; the Moon is white with a `--border-strong` outline, which is what makes it
read as the Moon without being labelled.

The Moon is drawn 10px from Earth's dot along `moonLon` — the direction is real, the 10px
is not, the same contract as the orbit spacing:

```
moonX = earthX + 10 * cos(moonLon)
moonY = earthY - 10 * sin(moonLon)
```

Draw the Moon's orbit too — a circle of R=10 centred on Earth's dot, in the same stroke as
the planetary orbits. It is what makes the white dot read as orbiting rather than adjacent.

## Step 3 — draw

Canvas: `<svg width="100%" viewBox="0 0 380 360" role="img">` with `<title>` and `<desc>`,
centre at cx=190, cy=175. Neptune's orbit at R=140 puts the outermost edge at x=330 and
y=35..315, inside the safe area.

Rules that keep it from breaking:

1. Orbit circles are `fill="none" stroke="var(--border-strong)" stroke-width="0.5"` — no
   `<style>` block, no hardcoded colours.
2. Dots use the ramp classes directly on the `<circle>` so dark mode works automatically.
3. Every `<text>` gets `class="ts"` (12px). **A label always overlaps its dot on one axis** —
   above or below it (sharing x), or beside it (sharing y). Never offset diagonally. The four
   positions, which all leave a 2px gap between the dot's edge and the text box:

   ```
   above   x = dotX,         y = dotY - (r + 5),   text-anchor="middle"
   below   x = dotX,         y = dotY + (r + 11),  text-anchor="middle"
   right   x = dotX + r + 3, y = dotY + 4,         text-anchor="start"
   left    x = dotX - r - 3, y = dotY + 4,         text-anchor="end"
   ```

   The `+4` on left and right puts the 12px baseline on the dot's centre line.
4. Above and below can also slide horizontally while keeping the x overlap: left-justified to
   the dot's left edge (`text-anchor="start"` at `dotX - r`), centred, or right-justified to
   its right edge (`text-anchor="end"` at `dotX + r`). Sliding is how a label escapes a
   crowded neighbour without going diagonal and without shrinking the text.
5. Work the positions in the order above, below, right, left, taking the first that clears
   every dot, every label already placed, and the viewBox — then **look at the result and
   override where it reads badly**. Two labels can pass a box test and still be visually
   stacked, and the fix is usually rule 4's slide or the opposite side of the dot. A dot near
   the right edge (x > 290) fails *right* on the viewBox test and falls through to *left*,
   which keeps the y overlap; near the left edge it falls through to *right*.
6. Earth and the Moon take a single label reading `Earth+Moon`. Two labels 10px apart collide
   no matter which position each one takes. The pair's effective radius for rule 3 is the
   Moon's orbit, not Earth's dot — clear the label to R=10, or it lands inside that circle.
7. Every orbit carries one arrowhead showing the direction of travel, counter-clockwise in
   this view — the Moon's circle included, since it goes the same way. At longitude `a` on a
   circle of radius `R`, the counter-clockwise screen tangent is `(-sin a, -cos a)` and the
   outward radial is `(cos a, -sin a)`; build a triangle 4.5px long and 5.2px across,
   `fill="var(--border-strong)"`.
   - The eight planetary arrowheads all sit at one longitude so they line up radially. Pick
     it per date as the midpoint of the widest gap between planets, so none lands on a dot.
   - The Moon's is scaled to its smaller circle — 3.5px long, 4px across — and sits opposite
     the Moon itself, at `moonLon + 180`.
8. Caption at y=340: the date plus "not to scale".

## Step 4 — report the numbers

After the chart, state the computed heliocentric ecliptic longitudes in degrees, one line,
so the user can check the work. Do not restate what the picture already shows.

## Delivering it as a file

When the chart is handed over as a file rather than rendered inline, it goes in
`<repo>/_preview/`, named for the date. A standalone file has no host supplying the design
tokens, so wrap the SVG in a page that defines them — the SVG markup itself stays exactly as
step 3 specifies, classes and `var(--border-strong)` only:

```css
:root {
  --bg: #FAF9F5; --fg: #1F1E1C; --border-strong: #8C877D;
  --coral: #C8553D; --amber: #C08A2E; --teal: #2F7E76;
  --sun: #F5C400; --moon: #FFFFFF;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #1A1917; --fg: #EDEBE6; --border-strong: #6E6A62;
    --coral: #E8836A; --amber: #E0B457; --teal: #57B0A6;
    --sun: #FFD21E; --moon: #FFFFFF;
  }
}
.c-coral { fill: var(--coral); }
.c-amber { fill: var(--amber); }
.c-teal  { fill: var(--teal); }
.c-sun   { fill: var(--sun); }
.c-moon  { fill: var(--moon); stroke: var(--border-strong); stroke-width: 0.75; }
.ts      { font-size: 12px; fill: var(--fg); }
```

## Variations worth supporting

- **Inner planets only**: drop Jupiter through Neptune and re-space at R = 45, 75, 105, 135.
  Positions are far more legible.
- **Two dates compared**: draw both sets of dots, second set hollow (`fill="none"` plus the
  ramp stroke), and say which is which in prose.
- **Interactive**: HTML with a date input, re-running the position math in JS, is a reasonable
  upgrade when the user wants to scrub through time. The Kepler solve ports to JS unchanged.
