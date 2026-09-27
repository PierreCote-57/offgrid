---
name: horizons-ephemeris
description: Fetch a full year of JPL Horizons ephemeris files for the sky page — one CSV per body under {folder.local}/ephemeris/<year>/. Use when Pierre names a year to fetch, refresh or complete, e.g. "get 2027", "we need 2025 too", "redo 2026 as a full year".
---

# Horizons ephemeris files for one year

The input is a year. The output is nine CSV files, one per body that has an ephemeris, in the
form `HorizonsEphemeris` reads.

## What a year's files hold

One row a day at 00:00 UT, from `<year-1>-12-31` to `<year+1>-01-01` — the year plus one day at
each end, so the first and last local day of the year fall between two rows and interpolate.
That is 367 rows for a common year and 368 for a leap year, under the header line.

A file is `<folder.local>/ephemeris/<year>/<stem>.csv`. `folder.local` is set in
`src/main/resources/application-dev.yaml`; the stem is the body's name in lower case, which is
`HorizonsBody.getFileStem()`.

Horizons answers for the years -2000 to +2999.

## Step 1 — the bodies

Read the enum constants in
`src/main/java/com/lc/offgrid/common/misc/astronomy/planet/HorizonsBody.java`. The second
argument of each is the `COMMAND` identifier. A body whose identifier is null — Earth, because
the ephemeris is geocentric — has no file and is skipped.

Never copy the identifiers into this file: the enum is where they live.

## Step 2 — the query

One call per body, to `https://ssd.jpl.nasa.gov/api/horizons.api`, with:

    format=text
    COMMAND='<the body's identifier>'
    OBJ_DATA='NO'
    MAKE_EPHEM='YES'
    EPHEM_TYPE='OBSERVER'
    CENTER='500@399'
    START_TIME='<year-1>-12-31'
    STOP_TIME='<year+1>-01-01'
    STEP_SIZE='1 d'
    QUANTITIES='1,9,20,23,24,29'
    ANG_FORMAT='DEG'
    CSV_FORMAT='YES'

`CENTER='500@399'` is the geocentre, so the rows serve any observer. The quantities and the
columns they produce are documented in `HorizonsRow`.

**No more than one call every 15 seconds.** It is Pierre's pacing and it is not negotiable.
Nine bodies is a little over two minutes.

## Step 3 — run it as one background script

A foreground `sleep` is blocked in this harness, so the whole year is fetched by a single Bash
command run in the background, which sleeps between calls. Fill in the folder, the year and the
body list, then run it with `run_in_background: true` and read its output when it finishes.

```bash
LOCAL="<folder.local>"
YEAR=<year>
BODY_LIST="<stem:identifier pairs, built from HorizonsBody in step 1>"

OUT="$LOCAL/ephemeris/$YEAR"
mkdir -p "$OUT"
FIRST=1
for ENTRY in $BODY_LIST; do
    STEM="${ENTRY%%:*}"
    COMMAND_ID="${ENTRY##*:}"
    if [ $FIRST -eq 0 ]; then sleep 15; fi
    FIRST=0
    RAW="$OUT/$STEM.raw"
    curl -sS -G "https://ssd.jpl.nasa.gov/api/horizons.api" \
        --data-urlencode "format=text" \
        --data-urlencode "COMMAND='$COMMAND_ID'" \
        --data-urlencode "OBJ_DATA='NO'" \
        --data-urlencode "MAKE_EPHEM='YES'" \
        --data-urlencode "EPHEM_TYPE='OBSERVER'" \
        --data-urlencode "CENTER='500@399'" \
        --data-urlencode "START_TIME='$((YEAR-1))-12-31'" \
        --data-urlencode "STOP_TIME='$((YEAR+1))-01-01'" \
        --data-urlencode "STEP_SIZE='1 d'" \
        --data-urlencode "QUANTITIES='1,9,20,23,24,29'" \
        --data-urlencode "ANG_FORMAT='DEG'" \
        --data-urlencode "CSV_FORMAT='YES'" > "$RAW"
    awk '/^\$\$SOE/ { inside = 1; next }
         /^\$\$EOE/ { inside = 0; next }
         inside     { print; next }
         /^ Date__\(UT\)__HR:MN/ { print }' "$RAW" > "$OUT/$STEM.csv"
    rm "$RAW"
    echo "$STEM rows=$(( $(wc -l < "$OUT/$STEM.csv") - 1 )) first=$(sed -n '2p' "$OUT/$STEM.csv" | cut -c1-18) last=$(tail -1 "$OUT/$STEM.csv" | cut -c1-18)"
done
```

The trim keeps the column header and the rows between `$$SOE` and `$$EOE` and drops everything
else. `HorizonsEphemeris` skips the header and blank lines and nothing else, so any preamble
left in a file is parsed as a row and throws.

## Step 4 — check every file before saying it is done

The script's own line per body is the check: the row count is 367, or 368 in a leap year, and
the first and last rows are `<year-1>-Dec-31 00:00` and `<year+1>-Jan-01 00:00`. A body whose
count is 0 or whose rows are missing got an error page instead of an ephemeris — Horizons
answers 200 with the error in the text — so refetch that body alone.

State the year, the nine counts and the range, and stop.
