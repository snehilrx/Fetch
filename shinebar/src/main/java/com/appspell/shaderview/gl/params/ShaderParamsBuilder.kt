package com.appspell.shaderview.gl.params

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES30
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class ShaderParamsBuilder {

    private val result: ShaderParams

    constructor() {
        this.result = ShaderParamsImpl()
    }

    internal constructor(result: ShaderParams) {
        this.result = result
    }

    fun addFloat(paramName: String, value: Float? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.FLOAT, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addInt(paramName: String, value: Int? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.INT, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addBool(paramName: String, value: Boolean? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.BOOL, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addVec2f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.FLOAT_VEC2, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    /**
     * Pass color form android resources to shader as vec4
     */
    fun addColor(paramName: String, @ColorRes colorRes: Int, resources: Resources): ShaderParamsBuilder {
        val color = ResourcesCompat.getColor(resources, colorRes, null)
        addColor(paramName, color)
        return this
    }

    /**
     * Pass color integer as color to shader as vec4
     */
    fun addColor(paramName: String, @ColorInt color: Int): ShaderParamsBuilder {
        addVec4f(
            paramName,
            floatArrayOf(
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f,
                Color.alpha(color) / 255f
            )
        )
        return this
    }

    fun addVec3f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.FLOAT_VEC3, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addVec4f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.FLOAT_VEC4, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addVec2i(paramName: String, value: IntArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.INT_VEC2, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addVec3i(paramName: String, value: IntArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.INT_VEC3, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addVec4i(paramName: String, value: IntArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.INT_VEC4, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addMat3f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.MAT3, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addMat4f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.MAT4, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun addMat3x4f(paramName: String, value: FloatArray? = null): ShaderParamsBuilder {
        val param = Param(valeType = Param.ValueType.MAT3x4, value = value)
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    /**
     * Set 2d texture
     * set texture to GL_TEXTURE0 slot by default
     */
    fun addTexture2D(
        paramName: String,
        bitmap: Bitmap? = null,
        textureSlot: Int = GLES30.GL_TEXTURE0
    ): ShaderParamsBuilder {
        val param = Param(
            valeType = Param.ValueType.SAMPLER_2D,
            value = TextureParam(
                bitmap = bitmap,
                textureSlot = textureSlot,
                needToRecycleWhenUploaded = false
            )
        )
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    /**
     * Set 2d texture
     * set texture to GL_TEXTURE0 slot by default
     */
    fun addTexture2D(
        paramName: String,
        @DrawableRes textureResourceId: Int,
        textureSlot: Int = GLES30.GL_TEXTURE0
    ): ShaderParamsBuilder {
        val param = Param(
            valeType = Param.ValueType.SAMPLER_2D,
            value = TextureParam(
                textureResourceId = textureResourceId,
                textureSlot = textureSlot,
                needToRecycleWhenUploaded = true
            )
        )
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    /**
     * Use external texture. Usually for video stream
     * Currently we support only one instance of such texture per shader
     *
     * more info: https://www.khronos.org/registry/OpenGL/extensions/OES/OES_EGL_image_external.txt
     */
    fun addTextureOES(paramName: String): ShaderParamsBuilder {
        val param = Param(
            valeType = Param.ValueType.SAMPLER_OES
        )
        result.updateParam(paramName = paramName, param = param)
        return this
    }

    fun build() = result
}