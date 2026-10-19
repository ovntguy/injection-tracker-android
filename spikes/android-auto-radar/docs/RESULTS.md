# Spike results

Recorded 2026-10-19 from `:radar-core:renderProof` and `:app:assembleDebug`. Machine-readable copy: `proof-metadata.json`.

## A — Local NEXRAD Level 3 on the Auto surface

**PASS (build + same-renderer PNG). DHU not executed.**

- Product: N0B, code 153, super-res base reflectivity. 720 radials × 1840 bins × 0.25 km. Scan `2026-10-19T16:54:47Z`.
- Source: `https://unidata-nexrad-level3.s3.amazonaws.com/FTG_N0B_2026_10_19_16_54_47` (public Unidata bucket, no credentials). tgftp `DS.00n0b` only listed a handful of sites and returned 403 for KFTG `sn.last`. N0Q `DS.p94r0/SI.{site}/sn.last` on tgftp is the fallback in `NexradClient`.
- Nearest site to the default Denver point is KFTG (Front Range Airport). Decoder is original packet-16 code, not wX.
- One OSM tile, `https://tile.openstreetmap.org/5/6/12.png`, is drawn under the sweep. Attribution is on the PNG.
- Proof: `local-nexrad-reflectivity.png` (207992 echo pixels). `RadarScreen` calls the same `RadarCompositor.compose`, then `Surface.lockCanvas` / `drawText` for the scan time. The pane row shows that time too.
- Blocker: no phone, emulator image, or Desktop Head Unit in this environment, so the surface was not driven inside DHU. Steps are in `../SETUP.md`.

## B — National MRMS composite reflectivity

**PASS (real GetMap PNG).**

- Layer: `conus_cref_qcd` (CONUS MRMS quality-controlled composite reflectivity), from the CREF QCD link on https://opengeo.ncep.noaa.gov/geoserver/www/.
- GetCapabilities: `https://opengeo.ncep.noaa.gov/geoserver/conus/conus_cref_qcd/ows?service=WMS&version=1.3.0&request=GetCapabilities`
- TIME steps advertised at fetch: **60** (explicit ISO timestamps, about two hours at a two-minute step). Default/latest used for GetMap: `2026-10-19T16:56:41Z`. The count moves as NOAA rolls the dimension.
- URL template:

```text
https://opengeo.ncep.noaa.gov/geoserver/conus/conus_cref_qcd/ows?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=conus_cref_qcd&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE&CRS=EPSG:3857&BBOX={minx},{miny},{maxx},{maxy}&WIDTH={width}&HEIGHT={height}&TIME={iso8601}
```

- Proof: `national-mrms-reflectivity.png` (the GetMap body, 44136 bytes, PNG signature checked). Shown as this file rather than on the DHU surface.

## C — Manifest and sideload notes

**PASS (manifest). DHU procedure written, not run.**

Merged debug manifest contains:

- `androidx.car.app.category.WEATHER` on `RadarCarAppService`
- `androidx.car.app.MAP_TEMPLATES`
- `androidx.car.app.ACCESS_SURFACE`
- `androidx.car.app.minCarApiLevel` = `7`
- template host metadata `com.google.android.gms.car.application` → `@xml/automotive_app_desc`

`SETUP.md` has developer mode, unknown sources, SDK Manager DHU install, `adb forward tcp:5277 tcp:5277`, start head unit server, `desktop-head-unit`, and real-car sideload.

## Not done

Loop UI, phone settings, Play, Level 2, RIDGE2, velocity, live car location (Denver is hard-coded as the observer).
