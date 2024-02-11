package io.github.snehilrx.shinebar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.children
import com.appspell.shaderview.ShaderView
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import com.google.android.material.appbar.AppBarLayout
import io.github.snehilrx.R
import io.github.snehilrx.shinebar.Utils.getStatusBarHeight
import kotlin.math.abs


class Shinebar : FrameLayout, AppBarLayout.OnOffsetChangedListener {

    private lateinit var shaderView: ShaderView
    private var oProgress: Float = -1f
    private var progress: Float = 0f

    private lateinit var shaderParams: ShaderParams


    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary,
            typedValue,
            true
        )
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.Shinebar, defStyle, 0
        )


        shaderParams = ShaderParamsBuilder()
            .addColor(
                SHADER_PARAM_START_COLOR,
                a.getColor(R.styleable.Shinebar_startColor, Color.TRANSPARENT)
            )
            .addColor(
                SHADER_PARAM_END_COLOR,
                a.getColor(R.styleable.Shinebar_endColor, Color.TRANSPARENT)
            )
            .addFloat(SHADER_PARAM_SHADER_HEIGHT, getShaderHeight())
            .addFloat(SHADER_PARAM_TOP_MARGIN, getStatusBarHeight(resources))
            .addVec2f(SHADER_PARAM_VIEW_SIZE, floatArrayOf(0f, 0f))
            .addFloat(SHADER_PARAM_DISTANCE_SCROLLED, 0f)
            .addFloat(SHADER_PARAM_TOTAL_SCROLL, 0f)
            .build()

        a.recycle()
        setPadding(0, 0, 0, 0)
        shaderView = ShaderView(context, attrs, defStyle).apply {
            shaderParams = this@Shinebar.shaderParams
            fragmentShaderRawResId = R.raw.shader
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                    this.setMargins(0, 0, 0, 0)
                }
            debugMode = false
        }
        addView(shaderView)
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun getShaderHeight(): Float {
        return context.resources.getDimension(R.dimen.shaderHeight)
    }

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        return super.onApplyWindowInsets(insets)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shaderParams.updateValue(SHADER_PARAM_VIEW_SIZE, floatArrayOf(w.toFloat(), h.toFloat()))
        shaderView.requestRender()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
        redrawCurve(totalScrollRange, verticalOffset)
    }

    fun redrawCurve(totalScrollRange: Float, verticalOffset: Int) {
        progress = (-verticalOffset / totalScrollRange)
        if (oProgress == progress) return
        if (progress.isFinite()) {
            shaderParams.updateValue(SHADER_PARAM_TOTAL_SCROLL, totalScrollRange)
            shaderParams.updateValue(SHADER_PARAM_DISTANCE_SCROLLED, abs(verticalOffset).toFloat())
            shaderView.requestRender()
        }
        oProgress = progress
    }


    fun setStartColor(@ColorInt startColor: Int) {
        shaderParams.updateValue(
            SHADER_PARAM_START_COLOR, floatArrayOf(
                Color.red(startColor) / 255f,
                Color.green(startColor) / 255f,
                Color.blue(startColor) / 255f,
                Color.alpha(startColor) / 255f
            )
        )
    }

    fun setEndColor(@ColorInt endColor: Int) {
        shaderParams.updateValue(
            SHADER_PARAM_END_COLOR, floatArrayOf(
                Color.red(endColor) / 255f,
                Color.green(endColor) / 255f,
                Color.blue(endColor) / 255f,
                Color.alpha(endColor) / 255f
            )
        )
    }

    companion object {
        @JvmStatic
        private val SHADER_PARAM_START_COLOR = "uStartColor"

        @JvmStatic
        private val SHADER_PARAM_END_COLOR = "uEndColor"

        @JvmStatic
        private val SHADER_PARAM_DISTANCE_SCROLLED = "uDistanceY"

        @JvmStatic
        private val SHADER_PARAM_TOTAL_SCROLL = "uTotalScroll"

        @JvmStatic
        private val SHADER_PARAM_VIEW_SIZE = "uViewSize"

        @JvmStatic
        private val SHADER_PARAM_TOP_MARGIN = "uMarginTop"

        @JvmStatic
        private val SHADER_PARAM_SHADER_HEIGHT = "ushaderHeight"
    }
}