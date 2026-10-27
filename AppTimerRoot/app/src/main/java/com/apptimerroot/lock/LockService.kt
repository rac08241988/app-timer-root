package com.apptimerroot.lock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * In-app foreground watchdog. While the timer is active it periodically
 * re-applies the freeze (defence against an app being re-enabled). When the
 * timer elapses, or the owner escape hatch is engaged, it releases all apps
 * and stops.
 *
 * This complements the Magisk daemon: the Magisk daemon survives force-stop
 * and reboot; this service gives prompt in-session enforcement and a
 * user-visible notification (no hiding).
 */
class LockService : Service() {

    @Volatile private var running = false
    private val channelId = "apptimerroot_lock"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification("App Timer Root is active"))
        if (!running) {
            running = true
            thread { loop() }
        }
        return START_STICKY
    }

    private fun loop() {
        while (running) {
            val state = LockState.fromJson(RootUtils.readState())
            if (state == null) { stopNow(); break }

            // Owner escape hatch: a root/recovery-placed override file releases the lock.
            if (RootUtils.escapeEngaged()) {
                RootUtils.unfreezeAll(state.lockedPackages)
                RootUtils.clearState()
                stopNow(); break
            }

            if (!state.active) {
                // Timer elapsed -> release.
                RootUtils.unfreezeAll(state.lockedPackages)
                RootUtils.clearState()
                stopNow(); break
            }

            // Re-apply freeze defensively.
            RootUtils.freezeAll(state.lockedPackages)
            try { Thread.sleep(TimeUnit.SECONDS.toMillis(30)) } catch (e: InterruptedException) {}
        }
    }

    private fun stopNow() {
        running = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                channelId, "App Timer Root",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, channelId)
        else
            @Suppress("DEPRECATION") Notification.Builder(this)
        return b.setContentTitle("App Timer Root")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }
}
