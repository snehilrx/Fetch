package com.appspell.shaderview.gl.params

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.view.Surface
import androidx.annotation.DrawableRes
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

data class TextureOESParam(
    val surface: Surface,
    val surfaceTexture: SurfaceTexture,
    val updateSurface: AtomicBoolean = AtomicBoolean(false),
    val lock: ReentrantLock = ReentrantLock()
)

data class TextureParam(
    @DrawableRes val textureResourceId: Int? = null,
    val bitmap: Bitmap? = null,
    var textureId: Int? = null,
    val needToRecycleWhenUploaded: Boolean = true,
    val textureSlot: Int = GLES30.GL_TEXTURE0
)