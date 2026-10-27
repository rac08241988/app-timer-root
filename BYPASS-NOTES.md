# App Timer Root — Bypass Notes & Owner Escape Hatch

This tool is deliberately **not** malware and **not** anti-removal. A competent
owner (or anyone with physical access + recovery/ADB) can always end the lock.
That is by design.

## Intentional owner escape hatch (preferred)

While a lock is active, create this file as root:

```text
/data/adb/apptimerroot/UNLOCK_OVERRIDE
```

### Via ADB (device unlocked / ADB authorized)

```bash
adb shell su -c 'mkdir -p /data/adb/apptimerroot; touch /data/adb/apptimerroot/UNLOCK_OVERRIDE'
```

Within ~20 seconds the Magisk daemon (and the in-app service) will notice,
`pm enable` every locked package, delete the state file, and exit.

### Via recovery (TWRP / OrangeFox / etc.)

1. Boot into recovery.
2. Mount `/data`.
3. Create the empty file
   `/data/adb/apptimerroot/UNLOCK_OVERRIDE`
   (or simply delete `/data/adb/apptimerroot/state.json`).
4. Reboot. On next boot the daemon sees no active lock (or sees the override)
   and releases any still-disabled packages.

### Via Magisk Manager

Remove the **App Timer Root** module and reboot. `uninstall.sh` re-enables
any packages that were still frozen.

## Other remaining bypass methods (honest list)

| Method | Notes |
| --- | --- |
| Wait for the timer | Normal unlock path. |
| Owner escape hatch file | Documented above. Preferred. |
| Delete `state.json` | `/data/adb/apptimerroot/state.json` — next daemon tick / reboot releases. |
| Kill daemon + remove module | Magisk → Modules → remove App Timer Root → reboot. |
| `pm enable <pkg>` as root | Manually re-enable a package; the daemon will re-disable it ~20s later while the lock is still active, so also clear state / use the escape hatch. |
| ADB as root | Full control; clear state, enable packages, uninstall module/APK. |
| Recovery file manager | Edit or wipe `/data/adb/apptimerroot/`. |
| Factory reset / wipe data | Clears everything, including Magisk module data. |
| Flash stock / unroot | Removes Magisk; disabled packages may remain disabled until you `pm enable` them from recovery/ADB. |
| Uninstall the APK | Always allowed. The Magisk daemon alone can still enforce an *existing* lock until state is cleared or the module is removed — this is intentional so force-stopping / uninstalling the UI app does not casually end a lock the owner started. Use the escape hatch or remove the module to end it. |

## What this tool deliberately does NOT do

- Does **not** hide from the launcher or Settings.
- Does **not** resist its own uninstall.
- Does **not** auto-reinstall.
- Does **not** block recovery, fastboot, or ADB.
- Does **not** encrypt or hide the state file.
- Does **not** provide a casual in-UI “unlock early” button — use the
  documented owner escape hatch instead.

## Safety reminder

Only install this on a device **you own**. Leave BYPASS-NOTES.md accessible
(this file, or a printed copy) so you always know how to release a lock if you
forget the duration you set.
