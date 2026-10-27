# App Timer Root — Install Instructions

Owner-controlled Android focus/timer tool for **rooted** devices with Magisk.

Pick apps → set a duration → Start Lock. The app freezes those packages with
root (`am force-stop` + `pm disable-user`). A Magisk late-start daemon
re-applies the freeze across reboot / force-stop until the unlock epoch
elapses. **No early-unlock button in the UI** — use the documented owner
escape hatch instead. It does **not** hide itself, resist uninstall, or
auto-reinstall.

## Prerequisites

- Android 8.0+ (API 26+), preferably Magisk-rooted.
- Magisk Manager installed (v20.4+ recommended; v24+ ideal).
- ADB optional (needed only for the ADB escape hatch).

## 1. Install the Magisk module

1. Copy `apptimerroot-magisk-module.zip` to the phone.
2. Open **Magisk** → **Modules** → **Install from storage**.
3. Select `apptimerroot-magisk-module.zip` and confirm.
4. **Reboot**.

After reboot the module's late-start service is armed. It will not freeze
anything until you start a lock from the app.

## 2. Install the APK

1. Copy `AppTimerRoot-release.apk` to the phone.
2. Install it (Files → tap the APK, or `adb install -r AppTimerRoot-release.apk`).
3. Grant any permission prompts (notifications / all-packages query).
4. Open **App Timer Root**. It should report root available; if not, check Magisk
   and grant the `su` prompt for `com.apptimerroot.lock`.

## 3. Use it

1. Check the apps you want locked.
2. Enter a duration in minutes.
3. Tap **Start Lock**.
4. The selected apps are force-stopped and disabled. A persistent notification
   shows while the in-app watchdog is running. The Magisk daemon also keeps the
   freeze applied across reboot / force-stop of the app.

There is **no early-unlock button** in the UI. The timer must elapse, or you
use the **owner escape hatch** (see BYPASS-NOTES.md).

## Uninstall / disable

- Magisk → Modules → remove **App Timer Root**, then reboot. The module's
  `uninstall.sh` re-enables any still-frozen packages.
- Uninstall the APK normally from Settings → Apps. Nothing prevents this.
