# Desktop Head Unit and real-car sideload

Spike package `com.deeeelay.aaradar.spike`. Build the debug APK first:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
cd spikes/android-auto-radar
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Debug builds accept any Android Auto host (`HostValidator.ALLOW_ALL_HOSTS_VALIDATOR`) so the Desktop Head Unit can connect. A release build uses `androidx.car.app.R.array.hosts_allowlist_sample`.

The session always shows the latest reflectivity frame and its scan time. It does not loop. The product rule "loop only when parked" is not implemented here.

The default observer is downtown Denver (39.7392, -104.9903), so the nearest WSR-88D is KFTG. There is no phone location UI in this spike.

## 1. Phone prerequisites

Use a phone on Android 9 (API 28) or higher. For Android 10+, sign in to the Play Store and update Android Auto before the first DHU connection.

1. Enable Android developer options and USB debugging (Settings → About phone → tap Build number 7 times, then Settings → System → Developer options → USB debugging).
2. Install the current Android Auto app from Play if it is not already installed.
3. Turn on Android Auto developer mode: open Android Auto settings, find the version label (About, or the app header), and tap it 10 times until the phone says developer mode is enabled.
4. Open the Android Auto overflow menu (three dots) → Developer settings, and enable **Unknown sources**. Without this, a sideloaded app never shows up in the car or DHU launcher.
5. In Android Auto settings, confirm **Add new cars to Android Auto** is enabled (Settings → Previously connected cars).

## 2. Install the Desktop Head Unit

On the computer, Android Studio → SDK Manager → SDK Tools tab → check **Android Auto Desktop Head Unit Emulator** → Apply.

The binary lands here:

```text
$ANDROID_HOME/extras/google/auto/desktop-head-unit
```

On Linux or macOS:

```bash
chmod +x "$ANDROID_HOME/extras/google/auto/desktop-head-unit"
```

Leave `libusb` in that same directory. DHU 2.x on Linux needs glibc 2.32 or newer (`ldd --version`) and:

```bash
sudo apt-get install libc++1 libc++abi1
```

Source: [Test using the Desktop Head Unit](https://developer.android.com/training/cars/testing/dhu).

## 3. Sideload the APK and start the head unit server

```bash
adb install -r spikes/android-auto-radar/app/build/outputs/apk/debug/app-debug.apk
```

On the phone, Android Auto → overflow menu → **Start head unit server**. A foreground notification says the server is running. Leave the phone unlocked.

## 4. ADB tunnel and DHU

```bash
adb forward tcp:5277 tcp:5277
cd "$ANDROID_HOME/extras/google/auto"
./desktop-head-unit
```

Accept the Android Auto prompts on the phone the first time. In the DHU launcher, open **Radar Spike**. The template title is Reflectivity. The map area is the `SurfaceCallback` drawing KFTG N0B (or N0Q if N0B is missing) plus the scan time. The pane repeats the scan time.

DHU 2.x can also attach over USB accessory mode instead of the forward:

```bash
./desktop-head-unit --usb
```

The adb-forward path above is the one this spike expects.

## 5. Real car

1. Keep Android Auto developer mode and **Unknown sources** on.
2. `adb install -r` the same debug APK onto the phone.
3. Connect the phone to the car with the usual Android Auto cable or wireless session.
4. Open the Android Auto app list in the car and launch Radar Spike.

This is not a Play listing and there is no release track. Production head units can still hide a sideloaded weather app even with unknown sources; the DHU is the run that should be treated as the real test. If the car host rejects the debug signature, install the debug APK again after `adb uninstall com.deeeelay.aaradar.spike`.

## What this VM could not run

The cloud build host has no phone, no Android emulator image, and no DHU binary. `assembleDebug` is the build check. The frame you would see on the surface is the PNG from `:radar-core:renderProof` (`docs/local-nexrad-reflectivity.png`), drawn by `RadarCompositor`, which `RadarScreen` also calls before `Surface.lockCanvas`.
