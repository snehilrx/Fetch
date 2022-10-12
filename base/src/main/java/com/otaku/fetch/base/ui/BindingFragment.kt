package com.otaku.fetch.base.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.otaku.fetch.base.TAG
import com.otaku.fetch.base.databinding.AppbarImageBinding
import com.otaku.fetch.base.databinding.AppbarShineBinding
import com.otaku.fetch.bindings.ImageViewBindings
import java.lang.ref.WeakReference

open class BindingFragment<T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    Fragment() {

    private var mStatusBarHeight: Int = 0
    lateinit var weakReference: WeakReference<T>
    val binding: T get() = weakReference.get() ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        weakReference =
            WeakReference(DataBindingUtil.inflate(inflater, layoutRes, container, false))
        mStatusBarHeight = getStatusBarHeight()
        onBind(binding, savedInstanceState)
        return binding.root
    }

    protected fun AppBarLayout.getAppBarBehavior() =
        (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior

    protected fun initAppbar(
        binding: AppbarShineBinding?,
        navController: NavController
    ) {
        if (binding == null) return
        initAppbar(
            binding.shineView,
            binding.toolbar,
            binding.collapsingToolbar,
            binding.appbarLayout,
            navController
        )
    }

    protected fun initAppbar(
        shineView: ShineView?,
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        appbarLayout: AppBarLayout?,
        navController: NavController?,
        showBackInHomeFragment: Boolean = false
    ) {
        initShineView(shineView, appbarLayout)
        initAppbar(
            toolbar,
            collapsingToolbar,
            navController,
            showBackInHomeFragment
        )
    }

    protected fun initShineView(
        shineView: ShineView?,
        appbarLayout: AppBarLayout?
    ) {
        shineView?.statusbarHeight = mStatusBarHeight.toFloat()
        appbarLayout?.addOnOffsetChangedListener(shineView)
    }

    protected fun initAppbar(
        imageView: ImageView?,
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        navController: NavController?,
        imageUrl: String? = null,
        showBackInHomeFragment: Boolean = false
    ) {
        if (imageView != null) {
            setAppbarBackground(imageView, imageUrl)
        }
        initAppbar(
            toolbar,
            collapsingToolbar,
            navController,
            showBackInHomeFragment
        )
    }

    protected fun setAppbarBackground(imageView: ImageView, image: String?) {
        if (image.isNullOrEmpty()) return
        ImageViewBindings.imageUrl(
            imageView, image
        )
    }

    protected fun initAppbar(
        binding: AppbarImageBinding,
        navController: NavController,
        imageUrl: String? = null
    ) {
        initAppbar(
            binding.appbarImageView,
            binding.toolbar,
            binding.collapsingToolbar,
            navController,
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
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        navController: NavController?,
        showBackInHomeFragment: Boolean
    ) {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        if (toolbar != null) {
            setupToolbar(toolbar)
            if (collapsingToolbar != null && navController != null) {
                if (showBackInHomeFragment) {
                    NavigationUI.setupWithNavController(
                        collapsingToolbar,
                        toolbar,
                        navController,
                        AppBarConfiguration(setOf(), null) {
                            if(!navController.popBackStack()){
                                activity?.finish()
                            }
                            return@AppBarConfiguration  true
                        }
                    )
                } else {
                    NavigationUI.setupWithNavController(collapsingToolbar, toolbar, navController)
                }
            }
        }
    }

    protected open fun onBind(binding: T, savedInstanceState: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        if (this::weakReference.isInitialized) {
            weakReference.clear()
        }
    }

    val bindingActivity: BindingActivity<*> get() = activity as BindingActivity<*>
}

/**
 * Set the adapter and call [clearReference] extension function in one call.
 * Use this extension if the current Fragment is going to be REPLACED. (When using fragmentTransaction.add is not necessary) the back stack.
 */
fun <VH : RecyclerView.ViewHolder> RecyclerView.setNullableAdapter(
    adapter: RecyclerView.Adapter<VH>
) {
    this.adapter = adapter
    this.clearReference()
}

/**
 * Remove the adapter after the view has been detached from window in order to prevent memory leaks.
 */
internal fun RecyclerView.clearReference() {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {

        }

        override fun onViewDetachedFromWindow(v: View) {
            this@clearReference.adapter = null
        }
    })
}