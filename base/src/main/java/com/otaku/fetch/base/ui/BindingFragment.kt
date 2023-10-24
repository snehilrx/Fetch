@file:Suppress("MemberVisibilityCanBePrivate", "MemberVisibilityCanBePrivate")

package com.otaku.fetch.base.ui

import android.os.Bundle
import android.util.TypedValue
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
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.otaku.fetch.base.databinding.AppbarImageBinding
import com.otaku.fetch.base.databinding.AppbarShineBinding
import com.otaku.fetch.base.utils.UiUtils.statusBarHeight
import com.otaku.fetch.bindings.ImageViewBindings
import io.github.snehilrx.shinebar.Shinebar
import java.lang.ref.WeakReference


@Suppress("MemberVisibilityCanBePrivate")
open class BindingFragment<T : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    Fragment() {

    private lateinit var weakReference: WeakReference<T>
    protected val binding: T
        get() = weakReference.get() ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        weakReference =
            WeakReference(DataBindingUtil.inflate(inflater, layoutRes, container, false))
        onBind(binding, savedInstanceState)
        return binding.root
    }

    protected fun AppBarLayout.getAppBarBehavior() =
        (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior

    protected fun initAppbar(
        binding: AppbarShineBinding?,
        navController: NavController,
        hideBackButton: Boolean = false
    ) {
        if (binding == null) return
        initAppbar(
            binding.shinebar,
            binding.toolbar,
            binding.collapsingToolbar,
            navController,
            hideBackButton
        )
    }

    protected fun initAppbar(
        shinebar: Shinebar?,
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        navController: NavController?,
        hideBackButton: Boolean = false
    ) {
        setupShineBar(shinebar)
        initAppbar(
            toolbar,
            collapsingToolbar,
            navController,
            hideBackButton
        )
    }

    fun setupShineBar(shinebar: Shinebar?) {
        val start = TypedValue()
        val end = TypedValue()
        context?.theme?.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary,
            start,
            true
        )
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorAccent, end, true)
        shinebar?.apply {
            setStartColor(start.data)
            setEndColor(end.data)
        }
    }

    private fun initAppbar(
        imageView: ImageView?,
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        navController: NavController?,
        imageUrl: String? = null,
        hideBackButton: Boolean = false
    ) {
        if (imageView != null) {
            setAppbarBackground(imageView, imageUrl)
        }
        initAppbar(
            toolbar,
            collapsingToolbar,
            navController,
            hideBackButton
        )
    }

    private fun setAppbarBackground(imageView: ImageView, image: String?) {
        if (image.isNullOrEmpty()) return
        ImageViewBindings.imageUrl(
            imageView, image
        )
    }

    protected fun initAppbar(
        binding: AppbarImageBinding,
        navController: NavController,
        hideBackButton: Boolean,
        imageUrl: String? = null,
    ) {
        initAppbar(
            binding.appbarImageView,
            binding.toolbar,
            binding.collapsingToolbar,
            navController,
            imageUrl,
            hideBackButton
        )
    }

    private fun setupToolbar(toolbar: Toolbar) {
        statusBarHeight {
            toolbar.apply {
                if (paddingTop == 0) {
                    setPadding(paddingLeft, paddingTop + it, paddingRight, paddingBottom)
                    toolbar.layoutParams = toolbar.layoutParams?.apply {
                        height += it
                    }
                }
            }
        }
    }

    private fun initAppbar(
        toolbar: Toolbar?,
        collapsingToolbar: CollapsingToolbarLayout?,
        navController: NavController?,
        hideBackButton: Boolean
    ) {
        bindingActivity.setSupportActionBar(toolbar)
        if (toolbar != null) {
            setupToolbar(toolbar)
            if (collapsingToolbar != null && navController != null) {
                val idToRemove = if (hideBackButton) {
                    navController.currentDestination?.id
                } else {
                    null
                }

                NavigationUI.setupWithNavController(
                    collapsingToolbar,
                    toolbar,
                    navController,
                    bindingActivity.createConfiguration(
                        navController,
                        setOfNotNull(
                            navController.graph.startDestinationId,
                            idToRemove
                        )
                    )
                )
            }
        }
    }

    protected open fun onBind(binding: T, savedInstanceState: Bundle?) {
        // no-op̊
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).setSupportActionBar(null)
        if (this::weakReference.isInitialized) {
            weakReference.clear()
        }
    }

    val bindingActivity: BindingActivity<*> get() = activity as BindingActivity<*>
}