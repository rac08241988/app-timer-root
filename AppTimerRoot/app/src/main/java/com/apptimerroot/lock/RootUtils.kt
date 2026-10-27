package com.apptimerroot.lock

import android.util.Log
import java.io.DataOutputStream

/**
 * Thin wrapper around `su` for the root operations this tool needs.
 *
 * This app is an OWNER-CONTROLLED focus/timer tool. Everything here is
 * transparent: it freezes (pm disable) or force-stops apps the owner picked,
 * and re-enables them when the timer elapses. There is no stealth, no
 * uninstall-resistance, and there is a documented owner escape hatch
 * (see BYPASS-NOTES.md and ESCAPE_HATCH handling below).
 */
object RootUtils {

    private const val TAG = "AppTimerRoot"

    /** Persistent state shared with the Magisk boot daemon. */
    const val MODULE_DIR = "/data/adb/apptimerroot"
    const val STATE_FILE = "$MODULE_DIR/state.json"
    /** If this file exists, the owner escape hatch is engaged -> clear the lock. */
    const val ESCAPE_FILE = "$MODULE_DIR/UNLOCK_OVERRIDE"

    data class RootResult(val exitCode: Int, val out: String, val err: String) {
        val ok: Boolean get() = exitCode == 0
    }

    /** Returns true if we can obtain a root shell. */
    fun isRootAvailable(): Boolean {
        return try {
            val r = runAsRoot("id -u")
            r.ok && r.out.trim() == "0"
        } catch (e: Exception) {
            Log.w(TAG, "root check failed", e)
            false
        }
    }

    /** Run a block of shell as root via `su -c`. */
    fun runAsRoot(script: String): RootResult {
        val proc = ProcessBuilder("su")
            .redirectErrorStream(false)
            .start()
        DataOutputStream(proc.outputStream).use { os ->
            os.writeBytes(script)
            os.writeBytes("\n")
            os.writeBytes("exit\n")
            os.flush()
        }
        val out = proc.inputStream.bufferedReader().readText()
        val err = proc.errorStream.bufferedReader().readText()
        val code = proc.waitFor()
        Log.d(TAG, "su script exit=$code")
        return RootResult(code, out, err)
    }

    /** Freeze a single package: force-stop then pm disable. */
    fun freezePackage(pkg: String): RootResult =
        runAsRoot("am force-stop $pkg; pm disable-user --user 0 $pkg")

    /** Unfreeze a single package. */
    fun unfreezePackage(pkg: String): RootResult =
        runAsRoot("pm enable $pkg")

    /** Apply the freeze to a whole set. */
    fun freezeAll(pkgs: Collection<String>): RootResult {
        if (pkgs.isEmpty()) return RootResult(0, "", "")
        val sb = StringBuilder()
        for (p in pkgs) {
            sb.append("am force-stop ").append(p).append("; ")
            sb.append("pm disable-user --user 0 ").append(p).append("; ")
        }
        return runAsRoot(sb.toString())
    }

    /** Release a whole set. */
    fun unfreezeAll(pkgs: Collection<String>): RootResult {
        if (pkgs.isEmpty()) return RootResult(0, "", "")
        val sb = StringBuilder()
        for (p in pkgs) sb.append("pm enable ").append(p).append("; ")
        return runAsRoot(sb.toString())
    }

    /**
     * Write the shared state JSON that the Magisk daemon reads on boot.
     * We write it through root so it lands on the module's persistent path.
     */
    fun writeState(json: String): RootResult {
        // base64 to avoid any quoting problems in the heredoc
        val b64 = android.util.Base64.encodeToString(
            json.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP
        )
        val script = buildString {
            append("mkdir -p $MODULE_DIR; ")
            append("echo $b64 | base64 -d > $STATE_FILE; ")
            append("chmod 600 $STATE_FILE; ")
            append("rm -f $ESCAPE_FILE; ")   // starting a new lock clears any stale override
            append("cat $STATE_FILE")
        }
        return runAsRoot(script)
    }

    /** Read the shared state JSON (may be empty if none). */
    fun readState(): String {
        val r = runAsRoot("cat $STATE_FILE 2>/dev/null")
        return if (r.ok) r.out else ""
    }

    /** True if the owner escape-hatch file is present. */
    fun escapeEngaged(): Boolean =
        runAsRoot("test -f $ESCAPE_FILE && echo yes || echo no").out.trim() == "yes"

    /** Clear all persistent state (used after unlock). */
    fun clearState(): RootResult =
        runAsRoot("rm -f $STATE_FILE $ESCAPE_FILE")
}
