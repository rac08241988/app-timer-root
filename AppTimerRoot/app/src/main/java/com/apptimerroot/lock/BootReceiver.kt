package com.apptimerroot.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * On boot, if a lock is still active, restart the in-app watchdog service.
 * (The Magisk daemon also handles boot independently; this is belt-and-braces
 * so the user-visible notification returns too.)
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val state = LockState.fromJson(RootUtils.readState()) ?: return
        if (!state.active) return
        val svc = Intent(context, LockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc)
        } else {
            context.startService(svc)
        }
    }
}
