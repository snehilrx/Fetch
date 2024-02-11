package com.appspell.shaderview.gl.render

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import com.appspell.shaderview.gl.params.ShaderParams
import com.appspell.shaderview.gl.shader.GLShader
import com.appspell.shaderview.gl.view.GLTextureView
import com.appspell.shaderview.log.LibLog
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "GLQuadRender"

private const val FLOAT_SIZE_BYTES = 4
private const val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

private const val UNKNOWN_ATTRIBUTE = -1

interface GLQuadRender : GLTextureView.Renderer {

    var shader: GLShader

    var listener: ShaderViewListener?

    interface ShaderViewListener {
        fun onSurfaceCreated()

        fun onDrawFrame(shaderParams: ShaderParams)
    }
}

/**
 * Render full-screen quad render
 */
internal class GLQuadRenderImpl(
    override var shader: GLShader
) : GLQuadRender {

    companion object {
        const val VERTEX_SHADER_IN_POSITION = "inPosition"
        const val VERTEX_SHADER_IN_TEXTURE_COORD = "inTextureCoord"

        const val VERTEX_SHADER_UNIFORM_MATRIX_MVP = "uMVPMatrix"
        const val VERTEX_SHADER_UNIFORM_MATRIX_STM = "uSTMatrix"
    }

    override var listener: GLQuadRender.ShaderViewListener? = null

    private val quadVertices: FloatBuffer
    private val matrixMVP = FloatArray(16)
    private val matrixSTM = FloatArray(16)

    // shader vertex attributes
    private var inPositionHandle = UNKNOWN_ATTRIBUTE
    private var inTextureHandle = UNKNOWN_ATTRIBUTE

    init {
        // set array of Quad vertices
        val quadVerticesData = floatArrayOf(
            // [x,y,z, U,V]
            -1.0f, -1.0f, 0f, 0f, 1f,
            1.0f, -1.0f, 0f, 1f, 1f,
            -1.0f, 1.0f, 0f, 0f, 0f,
            1.0f, 1.0f, 0f, 1f, 0f
        )

        quadVertices = ByteBuffer
            .allocateDirect(quadVerticesData.size * FLOAT_SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVerticesData).position(0)
            }

        // initialize matrix
        Matrix.setIdentityM(matrixSTM, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        listener?.onSurfaceCreated()
        if (!shader.isReady) {
            return
        }

        // built-in parameters
        shader.params = shader.params
            .newBuilder()
            .addMat4f(VERTEX_SHADER_UNIFORM_MATRIX_MVP)
            .addMat4f(VERTEX_SHADER_UNIFORM_MATRIX_STM)
            .build()

        // as far as we set the new
        shader.bindParams(null)

        // set attributes (input for Vertex Shader)
        inPositionHandle = glGetAttribLocation(VERTEX_SHADER_IN_POSITION)
        inTextureHandle = glGetAttribLocation(VERTEX_SHADER_IN_TEXTURE_COORD)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!shader.isReady) {
            return
        }

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(shader.program)
        checkGlError("glUseProgram")

        // shader input (built-in attributes)
        setAttribute(inPositionHandle, VERTEX_SHADER_IN_POSITION, 3, TRIANGLE_VERTICES_DATA_POS_OFFSET)
        setAttribute(inTextureHandle, VERTEX_SHADER_IN_TEXTURE_COORD, 2, TRIANGLE_VERTICES_DATA_UV_OFFSET)

        // build-in uniforms
        Matrix.setIdentityM(matrixMVP, 0)
        shader.params.updateValue(VERTEX_SHADER_UNIFORM_MATRIX_MVP, matrixMVP)
        shader.params.updateValue(VERTEX_SHADER_UNIFORM_MATRIX_STM, matrixSTM)

        // callback if we need to update some custom parameters
        listener?.onDrawFrame(shaderParams = shader.params)

        // send params to the shader
        shader.onDrawFrame()

        // activate blending for textures
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glEnable(GLES20.GL_BLEND)

        // draw scene
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")

        GLES30.glFinish()
    }

    /**
     * get location of some input attribute for shader
     */
    private fun glGetAttribLocation(attrName: String): Int {
        val attrLocation = GLES30.glGetAttribLocation(shader.program, attrName)
        checkGlError("glGetAttribLocation $attrName")
        return attrLocation
    }

    /**
     * set values for attributes of input vertices
     */
    private fun setAttribute(attrLocation: Int, attrName: String, size: Int, offset: Int) {
        if (attrLocation == UNKNOWN_ATTRIBUTE) {
            // skip it if undefined
            return
        }
        quadVertices.position(offset)
        GLES30.glVertexAttribPointer(
            attrLocation,
            size,
            GLES30.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            quadVertices
        )
        checkGlError("glVertexAttribPointer $attrName")
        GLES30.glEnableVertexAttribArray(attrLocation)
        checkGlError("glEnableVertexAttribArray $attrName")
    }

    private fun checkGlError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            LibLog.e(TAG, "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }
}