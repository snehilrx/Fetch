package com.appspell.shaderview.gl.shader

import android.content.Context
import androidx.annotation.RawRes
import com.appspell.shaderview.ext.getRawTextFile
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.log.LibLog

class ShaderBuilder internal constructor(private var shader: GLShader) {

    fun create(
        context: Context,
        @RawRes vertexShaderRawResId: Int,
        @RawRes fragmentShaderRawResId: Int
    ): ShaderBuilder {
        val vsh = context.resources.getRawTextFile(vertexShaderRawResId)
        val fsh = context.resources.getRawTextFile(fragmentShaderRawResId)
        if (!shader.createProgram(vsh, fsh)) {
            LibLog.e(TAG, "shader program wasn't created")
        }
        return this
    }

    fun params(shaderParams: ShaderParams): ShaderBuilder {
        shader.params = shaderParams
        return this
    }

    fun build() = shader
}