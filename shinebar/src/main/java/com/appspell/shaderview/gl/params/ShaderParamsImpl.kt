package com.appspell.shaderview.gl.params

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.view.Surface
import com.appspell.shaderview.ext.createExternalTexture
import com.appspell.shaderview.ext.loadBitmapForTexture
import com.appspell.shaderview.ext.toGlTexture
import kotlin.concurrent.withLock

class ShaderParamsImpl : ShaderParams {

    private val map = HashMap<String, Param>()

    override fun updateParam(paramName: String, param: Param) {
        map[paramName] = param
    }

    override fun updateValue(paramName: String, value: Float) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: Int) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: Boolean) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: FloatArray) {
        map[paramName]?.value = value
    }

    override fun updateValue(paramName: String, value: IntArray) {
        map[paramName]?.value = value
    }

    override fun updateValue2D(paramName: String, value: Bitmap?, needToRecycleWhenUploaded: Boolean) {
        map[paramName]?.value = (map[paramName]?.value as? TextureParam)?.copy(
            bitmap = value,
            needToRecycleWhenUploaded = needToRecycleWhenUploaded
        )
    }

    override fun updateValue2D(paramName: String, res: Int) {
        map[paramName]?.value = (map[paramName]?.value as? TextureParam)?.copy(
            textureResourceId = res,
            needToRecycleWhenUploaded = true
        )
    }

    /**
     * Usually it returns uniform shader ID of particaluar parameter (if initialized)
     */
    override fun getParamShaderLocation(paramName: String): Int? = map[paramName]?.location

    override fun getParamValue(paramName: String): Any? = map[paramName]?.value

    private fun updateUniformLocation(paramName: String, shaderProgram: Int) {
        map[paramName]?.apply {
            location = GLES30.glGetUniformLocation(shaderProgram, paramName)
        }
    }

    override fun release() {
        for (key in map.keys) {
            map[key]?.apply {
                when (valeType) {
                    Param.ValueType.SAMPLER_OES -> {
                        (value as? TextureOESParam)?.apply {
                            lock.withLock {
                                surfaceTexture.release()
                                surface.release()
                            }
                        }
                        value = null
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    override fun bindParams(shaderProgram: Int, resources: Resources?) {
        for (key in map.keys) {
            updateUniformLocation(key, shaderProgram)
            resources?.also { bindTextures(key, resources) }
        }
    }

    override fun newBuilder() = ShaderParamsBuilder(this)

    override fun pushValuesToProgram() {
        for (key in map.keys) {
            val param = map[key]
            if (param == null || param.location == UNKNOWN_LOCATION || param.value == null) {
                continue
            }
            when (param.valeType) {
                Param.ValueType.FLOAT -> GLES30.glUniform1f(param.location, param.value as Float)
                Param.ValueType.INT -> GLES30.glUniform1i(param.location, param.value as Int)
                Param.ValueType.BOOL -> GLES30.glUniform1i(param.location, if (param.value as Boolean) 1 else 0)
                Param.ValueType.FLOAT_VEC2 -> GLES30.glUniform2fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.FLOAT_VEC3 -> GLES30.glUniform3fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.FLOAT_VEC4 -> GLES30.glUniform4fv(param.location, 1, (param.value as FloatArray), 0)
                Param.ValueType.INT_VEC2 -> GLES30.glUniform2iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.INT_VEC3 -> GLES30.glUniform3iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.INT_VEC4 -> GLES30.glUniform4iv(param.location, 1, (param.value as IntArray), 0)
                Param.ValueType.MAT3 -> GLES30.glUniformMatrix3fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT4 -> GLES30.glUniformMatrix4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.MAT3x4 -> GLES30.glUniformMatrix3x4fv(
                    param.location,
                    1,
                    false,
                    (param.value as FloatArray),
                    0
                )
                Param.ValueType.SAMPLER_2D -> {
                    (param.value as? TextureParam)?.apply {
                        GLES30.glUniform1i(param.location, textureSlot.convertTextureSlotToIndex())
                        GLES30.glActiveTexture(textureSlot)
                        textureId?.also { GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it) }
                    }
                }
                Param.ValueType.SAMPLER_OES -> {
                    // update texture (as far as we stored SurfaceTexture to value in updateParams() method)
                    (param.value as? TextureOESParam)?.apply {
                        lock.withLock {
                            if (updateSurface.get()) {
                                surfaceTexture.updateTexImage()
                                updateSurface.set(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindTextures(paramName: String, resources: Resources) {
        map[paramName]?.apply {
            when (valeType) {
                // We have a different flow for Textures.
                // At first, we get a bitmap from params and when OpenGL context is ready we convert it to Texture
                Param.ValueType.SAMPLER_2D -> {
                    // if it is a Bitmap let's upload it to the GPU
                    (value as? TextureParam)?.let { textureParam ->
                        // create Bitmap
                        val bitmap = textureParam.bitmap ?: textureParam.textureResourceId?.let {
                            resources.loadBitmapForTexture(it)
                        }

                        // upload bitmap to GPU
                        bitmap?.toGlTexture(
                            needToRecycle = textureParam.needToRecycleWhenUploaded,
                            textureSlot = textureParam.textureSlot
                        )
                    }.also { textureId ->
                        value = (value as? TextureParam)?.copy(
                            textureId = textureId
                        ) ?: value
                    }
                }
                // create Surface for External Texture
                Param.ValueType.SAMPLER_OES -> {
                    if (value == null) {
                        // if it's not initialized
                        location = createExternalTexture()
                        val surfaceTexture = SurfaceTexture(location)
                        value = TextureOESParam(
                            surfaceTexture = surfaceTexture,
                            surface = Surface(surfaceTexture)
                        ).apply {
                            surfaceTexture.setOnFrameAvailableListener {
                                lock.withLock {
                                    updateSurface.set(true)
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Do Nothing for the other types
                }
            }
        }
    }

    private fun Int.convertTextureSlotToIndex(): Int =
        when (this) {
            GLES30.GL_TEXTURE0 -> 0
            GLES30.GL_TEXTURE1 -> 1
            GLES30.GL_TEXTURE2 -> 2
            GLES30.GL_TEXTURE3 -> 3
            GLES30.GL_TEXTURE4 -> 4
            GLES30.GL_TEXTURE5 -> 5
            GLES30.GL_TEXTURE6 -> 6
            GLES30.GL_TEXTURE7 -> 7
            GLES30.GL_TEXTURE8 -> 8
            GLES30.GL_TEXTURE9 -> 9
            GLES30.GL_TEXTURE10 -> 10
            GLES30.GL_TEXTURE11 -> 11
            GLES30.GL_TEXTURE12 -> 12
            GLES30.GL_TEXTURE13 -> 13
            GLES30.GL_TEXTURE14 -> 14
            GLES30.GL_TEXTURE15 -> 15
            GLES30.GL_TEXTURE16 -> 16
            GLES30.GL_TEXTURE17 -> 17
            GLES30.GL_TEXTURE18 -> 18
            GLES30.GL_TEXTURE19 -> 19
            GLES30.GL_TEXTURE20 -> 20
            GLES30.GL_TEXTURE21 -> 21
            GLES30.GL_TEXTURE22 -> 22
            GLES30.GL_TEXTURE23 -> 23
            GLES30.GL_TEXTURE24 -> 24
            GLES30.GL_TEXTURE25 -> 25
            GLES30.GL_TEXTURE26 -> 26
            GLES30.GL_TEXTURE27 -> 27
            GLES30.GL_TEXTURE28 -> 28
            GLES30.GL_TEXTURE29 -> 29
            GLES30.GL_TEXTURE30 -> 30
            GLES30.GL_TEXTURE31 -> 31
            else -> 0
        }
}