# App Timer Root — Install Instructions

Owner-controlled focus/timer tool for **rooted** Android devices with Magisk.
It freezes selected apps (`pm disable-user` / `am force-stop`) for a duration
you choose. It does **not** hide itself, resist uninstall, or auto-reinstall.

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

## Building from source (optional)

The project at `AppTimerRoot/` is a complete Android Studio / Gradle project.

```bash
cd AppTimerRoot
# generates the throwaway keystore if missing:
keytool -genkeypair -v -keystore apptimerroot-release.jks \
  -alias apptimerroot -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass apptimerroot -keypass apptimerroot \
  -dname "CN=App Timer Root, OU=Personal, O=Owner, L=NA, S=NA, C=US"
./gradlew :app:assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

Default signing passwords (throwaway keystore shipped for convenience):
`storePassword=apptimerroot`, `keyAlias=apptimerroot`, `keyPassword=apptimerroot`.
Replace the keystore before any real distribution.

## Uninstall / disable

- Magisk → Modules → remove **App Timer Root**, then reboot. The module's
  `uninstall.sh` re-enables any still-frozen packages.
- Uninstall the APK normally from Settings → Apps. Nothing prevents this.
