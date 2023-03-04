package com.otaku.fetch.tinker

import android.content.Context
import com.otaku.fetch.tinker.TinkerManager.tinkerApplicationLike
import com.tencent.tinker.lib.tinker.TinkerApplicationHelper
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals
import android.os.SystemClock
import android.util.Log
import com.tencent.tinker.loader.shareutil.ShareConstants

class SampleUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private val ueh: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler() as Thread.UncaughtExceptionHandler

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        Log.e(TAG, "uncaughtException:" + ex.message)
        tinkerFastCrashProtect()
        tinkerPreVerifiedCrashHandler(ex)
        ueh.uncaughtException(thread, ex)
    }

    /**
     * Such as Xposed, if it try to load some class before we load from patch files.
     * With dalvik, it will crash with "Class ref in pre-verified class resolved to unexpected implementation".
     * With art, it may crash at some times. But we can't know the actual crash type.
     * If it use Xposed, we can just clean patch or mention user to uninstall it.
     */
    private fun tinkerPreVerifiedCrashHandler(ex: Throwable) {
        val applicationLike = tinkerApplicationLike
        if (applicationLike == null || applicationLike.application == null) {
            Log.w(TAG, "applicationlike is null")
            return
        }
        if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
            Log.w(TAG, "tinker is not loaded")
            return
        }
        var throwable: Throwable? = ex
        var isXposed = false
        while (throwable != null) {
            if (!isXposed) {
                isXposed = isXposedExists(throwable)
            }

            // xposed?
            if (isXposed) {
                val isCausedByXposed =
                    throwable is IllegalAccessError && throwable.message!!.contains(
                        DALVIK_XPOSED_CRASH
                    )
                if (isCausedByXposed) {
                    Log.e(TAG, "have xposed: just clean tinker")
                    //kill all other process to ensure that all process's code is the same.
                    ShareTinkerInternals.killAllOtherProcess(applicationLike.application)
                    TinkerApplicationHelper.cleanPatch(applicationLike)
                    ShareTinkerInternals.setTinkerDisableWithSharedPreferences(applicationLike.application)
                    return
                }
            }
            throwable = throwable.cause
        }
    }

    /**
     * if tinker is load, and it crash more than MAX_CRASH_COUNT, then we just clean patch.
     */
    private fun tinkerFastCrashProtect(): Boolean {
        val applicationLike = tinkerApplicationLike
        if (applicationLike == null || applicationLike.application == null) {
            return false
        }
        if (!TinkerApplicationHelper.isTinkerLoadSuccess(applicationLike)) {
            return false
        }
        val elapsedTime =
            SystemClock.elapsedRealtime() - applicationLike.applicationStartElapsedTime
        //this process may not install tinker, so we use TinkerApplicationHelper api
        if (elapsedTime < QUICK_CRASH_ELAPSE) {
            val currentVersion = TinkerApplicationHelper.getCurrentVersion(applicationLike)
            if (ShareTinkerInternals.isNullOrNil(currentVersion)) {
                return false
            }
            val sp = applicationLike.application.getSharedPreferences(
                ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG,
                Context.MODE_PRIVATE
            )
            val fastCrashCount = sp.getInt(currentVersion, 0) + 1
            if (fastCrashCount >= MAX_CRASH_COUNT) {
                TinkerApplicationHelper.cleanPatch(applicationLike)
                Log.e(
                    TAG,
                    "tinker has fast crash more than %d, we just clean patch! $fastCrashCount"
                )
                return true
            } else {
                sp.edit().putInt(currentVersion, fastCrashCount).apply()
                Log.e(TAG, "tinker has fast crash %d times")
            }
        }
        return false
    }

    companion object {
        private const val TAG = "Tinker.SampleUncaughtExHandler"
        private const val QUICK_CRASH_ELAPSE = (10 * 1000).toLong()
        const val MAX_CRASH_COUNT = 3
        private const val DALVIK_XPOSED_CRASH =
            "Class ref in pre-verified class resolved to unexpected implementation"

        fun isXposedExists(thr: Throwable): Boolean {
            val stackTraces = thr.stackTrace
            for (stackTrace in stackTraces) {
                val clazzName = stackTrace.className
                if (clazzName != null && clazzName.contains("de.robv.android.xposed.XposedBridge")) {
                    return true
                }
            }
            return false
        }
    }
}