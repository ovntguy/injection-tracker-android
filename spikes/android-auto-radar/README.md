# Android Auto radar spike

Self-contained feasibility spike for a sideload-only Android Auto weather radar. It does not change the Injection Tracker app at the repo root.

Package: `com.deeeelay.aaradar.spike`.

The phone activity is only a launcher note. The radar UI is an Android Auto `CarAppService` (`MapWithContentTemplate`, Car API 7) that draws the latest reflectivity frame and its scan time on the surface. Looping while parked is intentionally not built.

## What was verified

| Check | Result |
| --- | --- |
| `:radar-core:test` | Passes offline (synthetic Level 3 packet, nearest site, WMS time parse, compositor) |
| `:app:assembleDebug` | Passes |
| `:radar-core:renderProof` | Writes the PNGs under `docs/` from the same `RadarCompositor` the car surface uses |

See `docs/RESULTS.md` and `SETUP.md`.

## Decoder

Original Level 3 decoder for digital radial packet 16 (N0B product 153 and N0Q product 94), including the bzip2 block NOAA puts after the product description. wX (`joshuatee.wx.radar`, GPL-3.0) was not copied: its decode path is bound to Android `Context` and OpenGL buffers (`NexradDecodeEightBit`, `NexradLevel3`, `OglRadarBuffers`). This spike stays under the repository MIT license.

## Build

```bash
export ANDROID_HOME="$HOME/Android/Sdk"   # or the SDK you installed
./gradlew :radar-core:test :app:assembleDebug
./gradlew :radar-core:renderProof         # network; rewrites docs/*.png
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

The car session picks the nearest WSR-88D to Birmingham, Alabama (33.5186, -86.8104), which is KBMX (Alabaster). That observer is a demo hard-code, not live GPS. Houston resolves to KHGX in the unit test.
