package com.otaku.fetch.base

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.otaku.fetch.base.ui.BindingActivity
import com.otaku.fetch.base.utils.UiUtils

val Any.TAG: String
    get() = this::class.java.name

val Fragment.isLandscape: Boolean
    get() {
        return this.context?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

fun BindingActivity<*>.askNotificationPermission(): Boolean {
    return when {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED -> {
            true
        }

        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
            UiUtils.showNotificationInfo(this, getString(R.string.notifications_rational)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            false
        }

        else -> {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            false
        }
    }
}