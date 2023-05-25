package com.otaku.fetch.base.ui

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.otaku.fetch.base.R
import com.otaku.fetch.base.databinding.AppbarImageBinding
import com.otaku.fetch.base.settings.Settings
import com.otaku.fetch.base.settings.dataStore
import com.otaku.fetch.base.utils.UiUtils
import com.otaku.fetch.bindings.ImageViewBindings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


/**
 * Base activity class that initializes binding
 *
 * @param layoutRes layout xml associated with the binding
 * @author snehil
 * */
open class BindingActivity<T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    AppCompatActivity() {

    lateinit var weakReference: WeakReference<T>
    val binding: T get() = weakReference.get() ?: throw IllegalStateException("Binding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weakReference =
            WeakReference(DataBindingUtil.setContentView(this, layoutRes))
        onBind(binding, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        weakReference =
            WeakReference(DataBindingUtil.setContentView(this, layoutRes))
        onBind(binding, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        window.decorView.consumeBottomInsets(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun AppBarLayout.getAppBarBehavior() =
        (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior

    protected fun initAppbar(
        imageView: ImageView?,
        toolbar: Toolbar?,
        imageUrl: String? = null
    ) {
        if (imageView != null) {
            setAppbarBackground(imageView, imageUrl)
        }
        initAppbar(toolbar)
    }

    private fun setAppbarBackground(imageView: ImageView, image: String?) {
        if (image.isNullOrEmpty()) return
        ImageViewBindings.imageUrl(
            imageView, image
        )
    }

    protected fun initAppbar(
        binding: AppbarImageBinding,
        imageUrl: String? = null
    ) {
        initAppbar(
            binding.appbarImageView,
            binding.toolbar,
            imageUrl
        )
    }

    private fun initAppbar(
        toolbar: Toolbar?
    ) {
        setSupportActionBar(toolbar)
    }

    fun setTransparentStatusBar() {
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            val isNightMode = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            controller.isAppearanceLightStatusBars =
                !isNightMode
            controller.isAppearanceLightNavigationBars =
                !isNightMode
        }
    }

    val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            UiUtils.showNotificationInfo(
                this,
                getString(R.string.notifications_updates_unavailable)
            )
        }
        lifecycleScope.launch {
            dataStore.data.collectLatest {
                if (it[Settings.PREF_DEFAULTS_SET] != isGranted) {
                    dataStore.edit { pref ->
                        pref[Settings.NOTIFICATION_ENABLED] = isGranted
                    }
                }
            }
        }
    }

    protected open fun onBind(binding: T, savedInstanceState: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        setSupportActionBar(null)
        if (this::weakReference.isInitialized) weakReference.clear()
    }
}

fun View.consumeBottomInsets(view: View) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val systemWindowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val bottomMargin = systemWindowInsets.bottom

        val layoutParams = view.layoutParams as MarginLayoutParams
        if (layoutParams.bottomMargin == 0) {
            layoutParams.bottomMargin += (bottomMargin / Resources.getSystem().displayMetrics.density).toInt()
        }
        insets
    }
}
