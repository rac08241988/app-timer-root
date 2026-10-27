# App Timer Root

> ⚠️ **For personal self-control only.** Install this only on a device **you own**, to limit your **own** app use. Using it to lock, monitor, or restrict someone else's device — or any device without that person's knowledge and consent — is abusive, may be illegal, and is not a supported use of this project.

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
Magisk module and either uninstall then reinstall or re-enable/unfreeze the app in app settings. Details: [BYPASS-NOTES.md](BYPASS-NOTES.md).

## License / intent

**For personal self-control only.** Install only on a device you own, to manage
your own focus. Not for controlling or monitoring anyone else's device.
