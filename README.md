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

## Quick install

1. install the module via Magisk → Modules → Install
    then reboot.
2. install the APK
3. Open **App Timer Root**, check apps, enter minutes, tap **Start Lock**.

Full steps: [INSTALL.md](INSTALL.md).

## Owner escape hatch (required safety)

Remove the
Magisk module or either reinstall or re-enable/unfreeze the app in app settings. Details: [BYPASS-NOTES.md](BYPASS-NOTES.md).

## License / intent

Personal owner-controlled focus tool. Install only on a device you own.
