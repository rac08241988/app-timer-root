#!/system/bin/sh
# late_start service: launches the lock daemon once Android is up, and keeps a
# supervisor loop that restarts the daemon if it is killed while a lock is
# still active. This is what makes the timer survive reboot and force-stop.
MODDIR=${0%/*}
STATE_DIR=/data/adb/apptimerroot
STATE_FILE="$STATE_DIR/state.json"
PIDFILE="$STATE_DIR/lockd.pid"

# Wait for boot to complete.
while [ "$(getprop sys.boot_completed)" != "1" ]; do
  sleep 5
done

# Supervisor: as long as there is an active state file, keep the daemon alive.
(
  while true; do
    if [ -f "$STATE_FILE" ]; then
      if ! { [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE" 2>/dev/null)" 2>/dev/null; }; then
        sh "$MODDIR/lockd.sh" &
      fi
    fi
    sleep 15
  done
) &
