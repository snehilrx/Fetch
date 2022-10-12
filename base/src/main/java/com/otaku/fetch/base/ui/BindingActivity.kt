package com.otaku.fetch.base.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.appbar.AppBarLayout
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.AppbarImageBinding
import com.otaku.fetch.bindings.ImageViewBindings
import java.lang.ref.WeakReference

open class BindingActivity<T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    AppCompatActivity() {

    private var mStatusBarHeight: Int = 0
    lateinit var weakReference: WeakReference<T>
    val binding: T get() = weakReference.get() ?: throw IllegalStateException("Binding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weakReference =
            WeakReference(DataBindingUtil.setContentView(this, layoutRes))
        mStatusBarHeight = getStatusBarHeight()
        onBind(binding, savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item);
    }

    fun showBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun initShineView(shineView: ShineView, appbarLayout: AppBarLayout) {
        shineView.statusbarHeight = mStatusBarHeight.toFloat()
        appbarLayout.addOnOffsetChangedListener(shineView)
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

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.layoutParams = toolbar.layoutParams?.apply {
            height += mStatusBarHeight
        }
        toolbar.apply {
            setPadding(paddingLeft, paddingTop + mStatusBarHeight, paddingRight, paddingBottom)
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            try {
                result = resources.getDimensionPixelSize(resourceId)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "getStatusBarHeight: unable to calculate statusbar size, resources must be an issue",
                    e
                )
            }
        }
        return result
    }

    private fun initAppbar(
        toolbar: Toolbar?
    ) {
        setSupportActionBar(toolbar)
        if (toolbar != null) {
            setupToolbar(toolbar)
        }
    }

    fun setTransparentStatusBar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = Color.TRANSPARENT
    }

    protected open fun onBind(binding: T, savedInstanceState: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        if (this::weakReference.isInitialized) weakReference.clear()
    }
}