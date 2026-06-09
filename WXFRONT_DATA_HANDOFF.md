# WeatherFront (wxfront.com) Data Pipeline — Handoff Brief

This document fully describes how to fetch and decode weather **model/forecast**
data from the WeatherFront CDN. It was reverse-engineered from the deployed web
app's bundled JavaScript. Everything needed to download and decode a field is
here. Paste this whole file into a new chat as context.

---

## 1. Where the data lives

Base CDN: `https://cdn.wxfront.com`

**Model/forecast field URL pattern:**
```
https://cdn.wxfront.com/data2/{MODEL}/{RUN}/{FORECAST_HOUR}/{VARIABLE}.bin.zstd
```
- `MODEL` — e.g. `HRRR`, `GFS`, `NAM`, `RAP`, `NAMNESTCONUS`, `ECMWFIFS`, …
- `RUN` — model run id, format `MMDDYY_HHZ` (e.g. `060826_18Z` = Jun 8 2026, 18 UTC).
  RTMA/satellite use `MMDDYY_HHMMZ`.
- `FORECAST_HOUR` — integer forecast hour that exists for that run (e.g. `48`).
- `VARIABLE` — field code, e.g. `2mTMP`, `2mDPT`, `10mWIND`, `REFC`.

Example:
```
https://cdn.wxfront.com/data2/HRRR/060826_18Z/48/2mTMP.bin.zstd
```

## 2. Which runs/hours exist (the index)

A status manifest lists the latest run and available forecast hours per model.
(Found via the app; exact path should be confirmed in the Network tab — it
returns JSON shaped like below.)

```json
{
  "latest": {
    "HRRR": {"latest_run":"060826_02Z","latest_run_hours_complete":3,"latest_run_hours_expected":18},
    "GFS":  {"latest_run":"060726_18Z","latest_run_hours_complete":384,"latest_run_hours_expected":384}
  },
  "all": {
    "HRRR": {"060826_00Z":[48,48], "060826_02Z":[3,18]}
  }
}
```
- `latest[MODEL].latest_run` → newest run id (use as `RUN`).
- `all[MODEL][RUN]` = `[hours_complete, hours_expected]` → which forecast hours
  are ready for that run.

## 3. Required HTTP headers (IMPORTANT)

The CDN is behind Cloudflare and **rejects requests without the app's Referer/
Origin** (returns `403 Forbidden`, body = the text "Forbidden"). A bare request
fails; you must send:

```
Referer: https://app.weatherfront.com/
Origin:  https://app.weatherfront.com
User-Agent: Mozilla/5.0
```
The server's `access-control-allow-origin: https://app.weatherfront.com` +
`vary: Origin` is enforced server-side, not just browser CORS. Files are public
once these headers are present (no auth/token/cookie needed). Cache: 4h.

## 4. File format (the container)

A downloaded `.bin.zstd` file is a custom container:

```
[ 4 bytes ]  big-endian uint32  = JSON header length N
[ N bytes ]  UTF-8 JSON header
[ rest    ]  zstd-compressed body (magic 28 b5 2f fd)
```

Header JSON example:
```json
{"product_name":"2mTMP","min_value":220,"max_value":330,"units":"K","map":"linear"}
```

The zstd body decompresses to a **raw grid of 1 byte per cell**, row-major.
Map each byte to a physical value (for `"map":"linear"`):
```
value = min_value + (byte / 255) * (max_value - min_value)   # in header "units"
```

### Verified example
`HRRR/060826_18Z/48/2mTMP.bin.zstd`:
- compressed size ≈ 650,744 bytes
- header length = 92
- decompressed = 1,906,200 bytes = **1059 rows × 1800 cols** (HRRR CONUS,
  native 1799 cols padded to 1800)
- header: min 220K, max 330K, linear
- decoded temps spanned ~17°F..102°F across CONUS — confirmed sane.

## 5. Reference decoder (Python)

```python
#!/usr/bin/env python3
# pip install requests zstandard
import struct, json, requests, zstandard

H = {"Referer":"https://app.weatherfront.com/",
     "Origin":"https://app.weatherfront.com","User-Agent":"Mozilla/5.0"}

def fetch_field(model, run, fhr, var):
    url = f"https://cdn.wxfront.com/data2/{model}/{run}/{fhr}/{var}.bin.zstd"
    r = requests.get(url, headers=H); r.raise_for_status()
    b = r.content
    n   = struct.unpack(">I", b[:4])[0]
    hdr = json.loads(b[4:4+n])
    raw = zstandard.ZstdDecompressor().decompress(b[4+n:])   # bytes, 1/cell
    return hdr, raw

def to_value(byte, hdr):
    mn, mx = hdr["min_value"], hdr["max_value"]
    return mn + (byte/255)*(mx-mn)   # units = hdr["units"]

if __name__ == "__main__":
    hdr, raw = fetch_field("HRRR", "060826_18Z", "48", "2mTMP")
    COLS, ROWS = 1800, 1059
    print(hdr, "cells:", len(raw))
    r, c = ROWS//2, COLS//2
    print("center:", round(to_value(raw[r*COLS+c], hdr), 1), hdr["units"])
```

## 6. Grid sizes per model (cells = rows × cols)

`len(raw)` tells you total cells; factor into rows×cols. Known:

| Model              | cols × rows  | cells     |
|--------------------|--------------|-----------|
| HRRR / NAMNESTCONUS| 1800 × 1059  | 1,906,200 |
| GFS (0.25°)        | 1440 × 721   | 1,038,240 |
| RAP (13 km)        | 451 × 337    | ~152,000  |
| NAM                | (confirm)    | —         |

Note: not every model exposes every variable/hour — a 403/404 means that
combo doesn't exist; skip it.

## 7. Open / not-yet-extracted

These still need the bundle chunk `wwaLayerManager-CzRIDqOS.js`:

- **Grid → lat/lon projection.** HRRR/NAM/RAP are Lambert Conformal Conic; GFS
  is a regular lat/lon grid. Exact projection params per model are in a table
  named `Sa[...]` in `wwaLayerManager-CzRIDqOS.js`. Needed to place cells on a
  map. (The decode in `App-W9DSsgFm.js` reads `g*cols + (col+1)`, i.e. there is
  a 1-column offset/pad — account for this when indexing geographically.)
- **Live radar (single-site NEXRAD + MRMS mosaic)** — same container format,
  different path/product, plus live "ScanSync" updates over an AWS AppSync
  WebSocket (`https://events.wxfront.com/event`, channels like
  `/radar/{SITE}/{PRODUCT}`). Radar single-site is radial data indexed
  `azimuth*numGates + gate`; MRMS is a tiled grid. Grab one radar request URL
  from the Network tab to confirm the exact path.

## 8. Variable codes seen in the bundle (partial)

`2mTMP` (2m temp), `2mDPT` (dewpoint), `2mRH`, `APPT` (feels-like),
`10mWIND`, `10mGUST`, `925mbWIND`/`850mbWIND`/`500mbWIND`/`300mbWIND`,
`REFC`/`MAXREF` (future/sim radar), `PTYPE` (precip type),
`APCP1H-sum`/`APCP24H-sum` (QPF), `ASNOW*-sum` (snowfall), `SBCAPE`/`MUCAPE`,
`SBCIN`, `HLCY1km`/`HLCY3km` (helicity), `6kmSHR`, `STPSBC`, `PWAT`, etc.

---
*Source: deployed weatherfront-web@0.1.0 bundle (proprietary). For personal/
educational reverse-engineering of a public web build.*
