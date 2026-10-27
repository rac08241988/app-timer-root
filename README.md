# App Timer Root

Owner-controlled Android focus/timer tool for **rooted** devices with Magisk.

Pick apps → set a duration → Start Lock. The app freezes those packages with
root (`am force-stop` + `pm disable-user`). A Magisk late-start daemon
re-applies the freeze across reboot / force-stop until the unlock epoch
elapses. **No early-unlock button in the UI** — use the documented owner
escape hatch instead.

## Contents

| Path | What |
| --- | --- |
| `AppTimerRoot/` | Complete Kotlin Android Studio / Gradle project (`com.apptimerroot.lock`) |
| `magisk/apptimerroot-module/` | Magisk module sources (`module.prop`, `service.sh`, `lockd.sh`, …) |
| `INSTALL.md` | Install steps for the APK + Magisk module |
| `BYPASS-NOTES.md` | Remaining bypasses **and the owner escape hatch** |

The signed release APK is **not** in this repo (binary upload isn't supported
via the tooling used to publish). Build it locally, or grab
`AppTimerRoot-release.apk` from the local deliverable folder if you have it.

## Quick install

1. install the module via Magisk → Modules → Install
    then reboot.
2. install the APK (`./gradlew :app:assembleRelease` inside
   `AppTimerRoot/`), grant `su`.
3. Open **App Timer Root**, check apps, enter minutes, tap **Start Lock**.

Full steps: [INSTALL.md](INSTALL.md).

## Owner escape hatch (required safety)

While a lock is active, create this file as root:

```text
/data/adb/apptimerroot/UNLOCK_OVERRIDE
```

Via ADB:

```bash
adb shell su -c 'mkdir -p /data/adb/apptimerroot; touch /data/adb/apptimerroot/UNLOCK_OVERRIDE'
```

Or delete `/data/adb/apptimerroot/state.json` from recovery, or remove the
Magisk module. Details: [BYPASS-NOTES.md](BYPASS-NOTES.md).

## Build the APK

```bash
cd AppTimerRoot
# generate a throwaway keystore if you don't have one (see INSTALL.md)
./gradlew :app:assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

Requires Android SDK (compileSdk 34) and JDK 17+.

## License / intent

Personal owner-controlled focus tool. Install only on a device you own.
