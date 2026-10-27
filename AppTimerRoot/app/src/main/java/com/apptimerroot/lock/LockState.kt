package com.apptimerroot.lock

import org.json.JSONArray
import org.json.JSONObject

/**
 * The shared lock state. Serialized to /data/adb/apptimerroot/state.json so the
 * Magisk boot daemon and the app agree on what is locked and until when.
 */
data class LockState(
    val lockedPackages: List<String>,
    val unlockEpochMs: Long,
    val startedEpochMs: Long
) {
    val active: Boolean get() = System.currentTimeMillis() < unlockEpochMs

    fun remainingMs(): Long = (unlockEpochMs - System.currentTimeMillis()).coerceAtLeast(0)

    fun toJson(): String = JSONObject().apply {
        put("version", 1)
        put("unlock_epoch_ms", unlockEpochMs)
        put("started_epoch_ms", startedEpochMs)
        put("locked_packages", JSONArray(lockedPackages))
    }.toString()

    companion object {
        fun fromJson(s: String?): LockState? {
            if (s.isNullOrBlank()) return null
            return try {
                val o = JSONObject(s)
                val arr = o.optJSONArray("locked_packages") ?: JSONArray()
                val pkgs = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) pkgs.add(arr.getString(i))
                LockState(
                    lockedPackages = pkgs,
                    unlockEpochMs = o.optLong("unlock_epoch_ms", 0L),
                    startedEpochMs = o.optLong("started_epoch_ms", 0L)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
