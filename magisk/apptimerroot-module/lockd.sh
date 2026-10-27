#!/system/bin/sh
# App Timer Root lock daemon.
# Reads the shared state file written by the app and re-applies the freeze on
# the owner-selected packages until the unlock timestamp elapses. Honors the
# owner escape hatch. This is transparent, owner-controlled enforcement only.

STATE_DIR=/data/adb/apptimerroot
STATE_FILE="$STATE_DIR/state.json"
ESCAPE_FILE="$STATE_DIR/UNLOCK_OVERRIDE"
LOG="$STATE_DIR/lockd.log"
PIDFILE="$STATE_DIR/lockd.pid"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*" >> "$LOG"; }

# crude JSON helpers (no jq on stock Android)
json_num() {  # json_num <key> <file>
  grep -o "\"$1\"[[:space:]]*:[[:space:]]*[0-9]*" "$2" 2>/dev/null | grep -o '[0-9]*$' | head -n1
}
json_pkgs() { # list packages inside "locked_packages":[ ... ]
  sed -n 's/.*"locked_packages"[[:space:]]*:[[:space:]]*\[\([^]]*\)\].*/\1/p' "$1" 2>/dev/null \
    | tr ',' '\n' | sed 's/[" ]//g' | grep -v '^$'
}

now_ms() { echo $(( $(date +%s) * 1000 )); }

# single-instance guard
if [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE" 2>/dev/null)" 2>/dev/null; then
  exit 0
fi
echo $$ > "$PIDFILE"

mkdir -p "$STATE_DIR"
log "lockd started pid=$$"

while true; do
  if [ ! -f "$STATE_FILE" ]; then
    log "no state file; exiting"
    break
  fi

  # Owner escape hatch: presence of override file releases everything.
  if [ -f "$ESCAPE_FILE" ]; then
    log "escape hatch engaged -> releasing"
    for p in $(json_pkgs "$STATE_FILE"); do
      pm enable "$p" 2>/dev/null
    done
    rm -f "$STATE_FILE" "$ESCAPE_FILE"
    break
  fi

  UNLOCK=$(json_num unlock_epoch_ms "$STATE_FILE")
  NOW=$(now_ms)
  [ -z "$UNLOCK" ] && UNLOCK=0

  if [ "$NOW" -ge "$UNLOCK" ]; then
    log "timer elapsed -> releasing"
    for p in $(json_pkgs "$STATE_FILE"); do
      pm enable "$p" 2>/dev/null
    done
    rm -f "$STATE_FILE"
    break
  fi

  # Re-apply freeze.
  for p in $(json_pkgs "$STATE_FILE"); do
    am force-stop "$p" 2>/dev/null
    pm disable-user --user 0 "$p" 2>/dev/null
  done

  sleep 20
done

rm -f "$PIDFILE"
log "lockd exited"
