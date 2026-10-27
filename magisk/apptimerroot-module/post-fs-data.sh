#!/system/bin/sh
# Runs early at boot (before most apps). Ensure state dir exists.
MODDIR=${0%/*}
mkdir -p /data/adb/apptimerroot
chmod 700 /data/adb/apptimerroot
