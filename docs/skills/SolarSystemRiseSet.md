---
name: solar-system-rise-set
description: Compute a rise, transit and set table for the Sun, the Moon and the planets, for a given date and a given observer on Earth. Use this whenever the user asks when something rises or sets — sunrise, sunset, moonrise, moonset, "when can I see Jupiter tonight", "what time is it dark", twilight, or a stargazing plan for a place and a date. The observer's latitude, longitude and timezone come from the request; times must be computed, never estimated. For a picture of where the planets are, use solar-system-chart instead.
---

# Rise, transit and set times

Produces a table of local times for one observer on one date. Everything is computed from
orbital elements — never quote a remembered sunrise time.

## Step 1 — pin down the observer

Three things are needed, and all three come from the request:

| Input | Where it comes from | What a mistake costs |
|---|---|---|
| Latitude, longitude | The place named. State the coordinates used. | 0.01° is 24 seconds |
| IANA timezone | The place named, e.g. `America/Vancouver` | The wrong zone is a full hour |
| Date | The request; today if unstated | — |

The timezone is the input worth checking twice: coordinates good to a hundredth of a degree
are a sub-minute error, while a wrong zone — or a fixed UTC offset used across a DST boundary
— is off by an hour and looks entirely plausible. Use `zoneinfo`, never a hardcoded offset.

If the user names a place without coordinates, state the latitude and longitude being used so
they can correct them.

## Step 2 — compute

The method: for each body, sample its altitude once a minute across the local day and report
every crossing of that body's horizon altitude. Sampling rather than solving the hour-angle
equation means the Moon's own motion — 0.55° an hour against the stars — is handled for free,
along with circumpolar bodies, bodies that never rise, and days with two risings.

The horizon altitude differs per body and is not a detail:

- **Sun** −0.8333° — upper limb, refraction plus semidiameter.
- **Planets** −0.5667° — point sources, refraction only.
- **Moon** `0.7275 * parallax − 0.5667°`, where parallax is `asin(6378.14 / distance_km)`.
  The Moon's parallax is nearly a degree, so using the planet value puts moonrise minutes out.

```python
import math
from datetime import date, datetime, timedelta, timezone
from zoneinfo import ZoneInfo

# The JPL approximate elements from solar-system-chart, valid 1800-2050.
# a, e, I, L, longitude_of_perihelion, longitude_of_ascending_node, then rates per century
ELS = {
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
R = math.radians

def helio(name, T):
    """Heliocentric ecliptic x, y, z in AU. Unlike the chart, z is kept."""
    v = ELS[name]
    a  = v[0] + v[6]*T;  e  = v[1] + v[7]*T;  I  = v[2] + v[8]*T
    L  = v[3] + v[9]*T;  lp = v[4] + v[10]*T; ln = v[5] + v[11]*T
    w = lp - ln
    M = (L - lp) % 360
    if M > 180: M -= 360
    Mr = R(M)
    E = Mr + e*math.sin(Mr)
    for _ in range(50):
        E -= (E - e*math.sin(E) - Mr) / (1 - e*math.cos(E))
    xp = a*(math.cos(E) - e)
    yp = a*math.sqrt(1 - e*e)*math.sin(E)
    wr, nr, Ir = R(w), R(ln), R(I)
    cw, sw, cn, sn, cI, sI = (math.cos(wr), math.sin(wr), math.cos(nr),
                              math.sin(nr), math.cos(Ir), math.sin(Ir))
    x = (cw*cn - sw*sn*cI)*xp + (-sw*cn - cw*sn*cI)*yp
    y = (cw*sn + sw*cn*cI)*xp + (-sw*sn + cw*cn*cI)*yp
    z = (sw*sI)*xp + (cw*sI)*yp
    return x, y, z

def eq_from_ecl(x, y, z, T):
    """Ecliptic to equatorial: right ascension and declination in degrees."""
    eps = R(23.439291 - 0.0130042*T)
    ra  = math.degrees(math.atan2(y*math.cos(eps) - z*math.sin(eps), x)) % 360
    dec = math.degrees(math.asin((y*math.sin(eps) + z*math.cos(eps))
                                 / math.sqrt(x*x + y*y + z*z)))
    return ra, dec

def moon_ecl(T):
    """Truncated lunar series: geocentric longitude, latitude (deg) and distance (km)."""
    Lp = 218.3164477 + 481267.88123421*T
    D  = 297.8501921 + 445267.1114034 *T
    M  = 357.5291092 +  35999.0502909 *T
    Mp = 134.9633964 + 477198.8675055 *T
    F  =  93.2720950 + 483202.0175233 *T
    lon = (Lp + 6.289*math.sin(R(Mp)) + 1.274*math.sin(R(2*D - Mp))
              + 0.658*math.sin(R(2*D)) + 0.214*math.sin(R(2*Mp))
              - 0.186*math.sin(R(M))   - 0.114*math.sin(R(2*F))) % 360
    lat = (5.128*math.sin(R(F))      + 0.281*math.sin(R(Mp + F))
         - 0.278*math.sin(R(Mp - F)) + 0.173*math.sin(R(2*D - F)))
    dist = (385000.56 - 20905.355*math.cos(R(Mp)) - 3699.111*math.cos(R(2*D - Mp))
                      -  2955.968*math.cos(R(2*D)) -  569.925*math.cos(R(2*Mp)))
    return lon, lat, dist

def position(body, JD):
    """Geocentric right ascension, declination, and this body's horizon altitude."""
    T = (JD - 2451545.0) / 36525.0
    if body == 'Moon':
        lon, lat, dist = moon_ecl(T)
        clat = math.cos(R(lat))
        x, y, z = clat*math.cos(R(lon)), clat*math.sin(R(lon)), math.sin(R(lat))
        ra, dec = eq_from_ecl(x, y, z, T)
        parallax = math.degrees(math.asin(6378.14/dist))
        return ra, dec, 0.7275*parallax - 0.5667
    ex, ey, ez = helio('Earth', T)
    if body == 'Sun':
        x, y, z, h0 = -ex, -ey, -ez, -0.8333
    else:
        bx, by, bz = helio(body, T)
        x, y, z, h0 = bx - ex, by - ey, bz - ez, -0.5667
    ra, dec = eq_from_ecl(x, y, z, T)
    return ra, dec, h0

def gmst(JD):
    d = JD - 2451545.0
    T = d / 36525.0
    return (280.46061837 + 360.98564736629*d
            + 0.000387933*T*T - T*T*T/38710000) % 360

def altitude(body, JD, lat, lon):
    """Altitude above this body's horizon: positive means up."""
    ra, dec, h0 = position(body, JD)
    H = R((gmst(JD) + lon - ra) % 360)          # lon is east-positive
    h = math.degrees(math.asin(math.sin(R(lat))*math.sin(R(dec))
                             + math.cos(R(lat))*math.cos(R(dec))*math.cos(H)))
    return h - h0

def rise_set(body, day, lat, lon, tz):
    """Every horizon crossing during the local day, plus whether it starts up."""
    start = datetime(day.year, day.month, day.day, tzinfo=ZoneInfo(tz))
    jd0 = start.astimezone(timezone.utc).timestamp()/86400.0 + 2440587.5
    step = 1.0/1440.0
    prev = altitude(body, jd0, lat, lon)
    up_at_start = prev > 0
    events = []
    for i in range(1, 1441):
        cur = altitude(body, jd0 + i*step, lat, lon)
        if (prev <= 0 < cur) or (prev >= 0 > cur):
            frac = prev / (prev - cur)
            events.append(('rise' if cur > prev else 'set',
                           start + timedelta(days=(i - 1 + frac)*step)))
        prev = cur
    return events, up_at_start

def transit(body, day, lat, lon, tz):
    """Local time of greatest altitude, and that altitude in degrees."""
    start = datetime(day.year, day.month, day.day, tzinfo=ZoneInfo(tz))
    jd0 = start.astimezone(timezone.utc).timestamp()/86400.0 + 2440587.5
    best, when = -99.0, None
    for i in range(1441):
        jd = jd0 + i/1440.0
        ra, dec, h0 = position(body, jd)
        h = altitude(body, jd, lat, lon) + h0
        if h > best:
            best, when = h, start + timedelta(days=i/1440.0)
    return when, best
```

## Step 3 — report

One table, local times, `HH:MM`. Bodies in order: Sun, Moon, then the planets outward.
Transit carries its altitude, which is what says whether the body is worth looking at.

Vancouver (49.2827, −123.1207), `America/Vancouver`, 1 September 2026:

| Body | Rises | Transit | Sets |
|---|---|---|---|
| Sun | 06:26 | 13:11 (49°) | 19:54 |
| Moon | 21:14 | 04:25 (58°) | 12:09 |
| Mercury | 06:49 | 13:31 (48°) | 20:11 |
| Venus | 10:45 | 15:50 (28°) | 20:53 |
| Mars | 01:23 | 09:28 (64°) | 17:31 |
| Jupiter | 04:05 | 11:33 (58°) | 19:00 |
| Saturn | 21:03 | 03:23 (44°) | 09:38 |
| Uranus | 22:50 | 06:43 (62°) | 14:32 |
| Neptune | 20:37 | 02:44 (41°) | 08:46 |

Rules for the table:

1. A body that sets before it rises is up at midnight — print the times in their column, not
   in clock order, and the table stays readable.
2. No crossing means one of two different things, and they must not be printed the same way:
   *always up* (circumpolar) or *never up* that day. `up_at_start` tells them apart.
3. Say the date, the place, the coordinates used and the timezone above the table. The
   coordinates are what the user checks when a time looks wrong.
4. Do not add a "best viewing" verdict unless asked. Bodies close to the Sun in the table are
   the user's to interpret.

## Validating a change

Sunrise and sunset for a well-known city are the check that catches almost every error —
a sign flip on longitude, a broken sidereal time, a timezone applied backwards. Vancouver
(49.2827, −123.1207, `America/Vancouver`) on 2026-09-01 gives sunrise 06:26 and sunset 19:54.
The Sun's transit is a second, independent check: its altitude must come out at
`90 - latitude + declination`, and its time must be within a few minutes of local solar noon.
Run both after any edit to the position code.

## Variations worth supporting

- **Twilight**: the same sampling with the Sun's horizon altitude set to −6° (civil), −12°
  (nautical) or −18° (astronomical) instead of −0.8333°.
- **A range of dates**: one row per day for a single body, to show a season's drift.
- **Moon phase**: the elongation between the Moon's and the Sun's geocentric longitudes,
  which both come out of `position` already.
