# App Timer Root — Magisk module

Install via Magisk Manager → Modules → Install from storage.

On boot, `service.sh` starts a supervisor that keeps `lockd.sh` running while
`/data/adb/apptimerroot/state.json` exists. The daemon freezes the listed
packages with `pm disable-user` / `am force-stop` until the unlock epoch
elapses, or until the owner escape hatch is engaged
(`/data/adb/apptimerroot/UNLOCK_OVERRIDE`).

See INSTALL.md and BYPASS-NOTES.md in the project root.
