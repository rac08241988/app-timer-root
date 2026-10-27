#!/system/bin/sh
# Magisk module install hook (runs once at flash time).
ui_print "- App Timer Root module"
ui_print "- Owner escape hatch: touch /data/adb/apptimerroot/UNLOCK_OVERRIDE"
mkdir -p /data/adb/apptimerroot
chmod 700 /data/adb/apptimerroot
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/service.sh"       0 0 0755
set_perm "$MODPATH/post-fs-data.sh"  0 0 0755
set_perm "$MODPATH/lockd.sh"         0 0 0755
set_perm "$MODPATH/uninstall.sh"     0 0 0755
ui_print "- Done. Install the App Timer Root APK, then reboot."
