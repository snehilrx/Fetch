package com.otaku.fetch.tinker


import android.util.Log
import com.tencent.tinker.entry.ApplicationLike
import com.tencent.tinker.lib.listener.DefaultPatchListener
import com.tencent.tinker.lib.listener.PatchListener
import com.tencent.tinker.lib.patch.AbstractPatch
import com.tencent.tinker.lib.patch.UpgradePatch
import com.tencent.tinker.lib.reporter.DefaultLoadReporter
import com.tencent.tinker.lib.reporter.DefaultPatchReporter
import com.tencent.tinker.lib.reporter.LoadReporter
import com.tencent.tinker.lib.reporter.PatchReporter
import com.tencent.tinker.lib.tinker.TinkerInstaller
import com.tencent.tinker.lib.util.TinkerLog
import com.tencent.tinker.lib.util.UpgradePatchRetry

/**
 * Created by zhangshaowen on 16/7/3.
 */
object TinkerManager {
    private const val TAG = "Tinker.TinkerManager"
    var tinkerApplicationLike: ApplicationLike? = null
    private var uncaughtExceptionHandler: SampleUncaughtExceptionHandler? = null
    private var isInstalled = false
    fun initFastCrashProtect() {
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = SampleUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        }
    }

    fun setUpgradeRetryEnable(enable: Boolean) {
        UpgradePatchRetry.getInstance(tinkerApplicationLike!!.application).setRetryEnable(enable)
    }

    /**
     * all use default class, simply Tinker install method
     */
    fun sampleInstallTinker(appLike: ApplicationLike?) {
        if (isInstalled) {
            Log.w(TAG, "install tinker, but has installed, ignore")
            return
        }
        TinkerInstaller.install(appLike)
        isInstalled = true
    }

    /**
     * you can specify all class you want.
     * sometimes, you can only install tinker in some process you want!
     *
     * @param appLike
     */
    fun installTinker(appLike: ApplicationLike) {
        if (isInstalled) {
            Log.w(TAG, "install tinker, but has installed, ignore")
            return
        }
        //or you can just use DefaultLoadReporter
        val loadReporter: LoadReporter = DefaultLoadReporter(appLike.application)
        //or you can just use DefaultPatchReporter
        val patchReporter: PatchReporter = DefaultPatchReporter(appLike.application)
        //or you can just use DefaultPatchListener
        val patchListener: PatchListener = DefaultPatchListener(appLike.application)
        //you can set your own upgrade patch if you need
        val upgradePatchProcessor: AbstractPatch = UpgradePatch()
        TinkerInstaller.install(
            appLike,
            loadReporter, patchReporter, patchListener,
            FetchTinkerService::class.java, upgradePatchProcessor
        )
        isInstalled = true
    }
}