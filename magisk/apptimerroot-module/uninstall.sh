#!/system/bin/sh
# Called by Magisk when the module is removed. Re-enable any locked apps so
# removing the module cleanly releases the lock (no lingering disabled apps).
STATE_DIR=/data/adb/apptimerroot
STATE_FILE="$STATE_DIR/state.json"
if [ -f "$STATE_FILE" ]; then
  for p in $(sed -n 's/.*"locked_packages"[[:space:]]*:[[:space:]]*\[\([^]]*\)\].*/\1/p' "$STATE_FILE" | tr ',' '\n' | sed 's/[" ]//g' | grep -v '^$'); do
    pm enable "$p" 2>/dev/null
  done
fi
rm -rf "$STATE_DIR"
