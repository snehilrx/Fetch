package com.appspell.shaderview.gl.shader

import android.content.res.Resources
import com.appspell.shaderview.gl.params.ShaderParams

const val UNKNOWN_PROGRAM = 0
const val TAG = "GLShader"

interface GLShader {

    val isReady: Boolean

    var params: ShaderParams

    var program: Int

    fun createProgram(vertexSource: String, fragmentSource: String): Boolean
    fun bindParams(resources: Resources? = null)

    fun onDrawFrame()
    fun release()
    fun newBuilder(): ShaderBuilder
}