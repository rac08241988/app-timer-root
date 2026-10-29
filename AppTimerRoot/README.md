# App Timer Root

Owner-controlled Android focus/timer app + Magisk module.

- **App**: pick installed apps, set a duration, tap Start Lock. Uses root
  (`su`) to `am force-stop` + `pm disable-user` the selection. Persists the
  lock set and unlock epoch to `/data/adb/apptimerroot/state.json`.
- **Grid view** (v1.3): toggle the app picker between the list and a compact
  6-column tile grid (selected apps show a checkmark). Your choice is remembered.
- **Credit**: a small "Made by Rattlehead" footer with a link to more of my software.
- **Magisk module**: late-start supervisor + `lockd.sh` re-apply the freeze
  across reboot / process death until the unlock epoch, or until the owner
  escape hatch is engaged.

See `../INSTALL.md` and `../BYPASS-NOTES.md`.

applicationId / package: `com.apptimerroot.lock`
