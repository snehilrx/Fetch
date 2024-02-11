package com.appspell.shaderview.gl.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLExt
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import androidx.annotation.CallSuper
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock


private const val TAG = "GLTextureView"

open class GLTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), View.OnLayoutChangeListener {

    companion object {
        /**
         * The renderer only renders
         * when the surface is created, or when [.requestRender] is called.
         *
         * @see .getRenderMode
         * @see .setRenderMode
         * @see .requestRender
         */
        const val RENDERMODE_WHEN_DIRTY = 0

        /**
         * The renderer is called
         * continuously to re-render the scene.
         *
         * @see .getRenderMode
         * @see .setRenderMode
         */
        const val RENDERMODE_CONTINUOUSLY = 1

        /**
         * Check glError() after every GL call and throw an exception if glError indicates
         * that an error has occurred. This can be used to help track down which OpenGL ES call
         * is causing an error.
         *
         * @see .getDebugFlags
         *
         * @see .setDebugFlags
         */
        const val DEBUG_CHECK_GL_ERROR = 1

        /**
         * Log GL calls to the system log at "verbose" level with tag "GLTextureView".
         *
         * @see .getDebugFlags
         *
         * @see .setDebugFlags
         */
        const val DEBUG_LOG_GL_CALLS = 2
    }

    internal var enableLogPauseResume = false
    internal var enableLogSurface = false
    internal var enableLogEgl = false

    private val sGLThreadManager = GLThreadManager()
    private val threadLock = ReentrantLock()
    private val threadLockCondition = threadLock.newCondition()

    private val mGLThread: GLThread = GLThread(WeakReference(this))

    private var mRenderer: Renderer? = null
    private var mDetached = false
    private var mEGLConfigChooser: EGLConfigChooser? = null
    private var mEGLContextFactory: EGLContextFactory? = null
    private var mEGLWindowSurfaceFactory: EGLWindowSurfaceFactory? = null
    private var mGLWrapper: GLWrapper? = null
    private var mDebugFlags = 0
    private var mEGLContextClientVersion = 0
    private var mPreserveEGLContextOnPause = false

    @Throws(Throwable::class)
    protected fun finalize() {
        mGLThread.requestExitAndWait()
    }

    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                surfaceCreated()
                surfaceChanged(width, height)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                surfaceChanged(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                surfaceDestroyed()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // no op
            }
        }
    }

    /**
     * Set the glWrapper. If the glWrapper is not null, its
     * [GLWrapper.wrap] method is called
     * whenever a surface is created. A GLWrapper can be used to wrap
     * the GL object that's passed to the renderer. Wrapping a GL
     * object enables examining and modifying the behavior of the
     * GL calls made by the renderer.
     *
     *
     * Wrapping is typically used for debugging purposes.
     *
     *
     * The default value is null.
     * @param glWrapper the new GLWrapper
     */
    fun setGLWrapper(glWrapper: GLWrapper?) {
        mGLWrapper = glWrapper
    }

    /**
     * Set the debug flags to a new value. The value is
     * constructed by OR-together zero or more
     * of the DEBUG_CHECK_* constants. The debug flags take effect
     * whenever a surface is created. The default value is zero.
     * @param debugFlags the new debug flags
     * @see .DEBUG_CHECK_GL_ERROR
     *
     * @see .DEBUG_LOG_GL_CALLS
     */
    fun setDebugFlags(debugFlags: Int) {
        mDebugFlags = debugFlags
    }

    /**
     * Get the current value of the debug flags.
     * @return the current value of the debug flags.
     */
    fun getDebugFlags(): Int {
        return mDebugFlags
    }

    /**
     * Control whether the EGL context is preserved when the GLTextureView is paused and
     * resumed.
     *
     *
     * If set to true, then the EGL context may be preserved when the GLTextureView is paused.
     *
     *
     * Prior to API level 11, whether the EGL context is actually preserved or not
     * depends upon whether the Android device can support an arbitrary number of
     * EGL contexts or not. Devices that can only support a limited number of EGL
     * contexts must release the EGL context in order to allow multiple applications
     * to share the GPU.
     *
     *
     * If set to false, the EGL context will be released when the GLTextureView is paused,
     * and recreated when the GLTextureView is resumed.
     *
     *
     *
     * The default is false.
     *
     * @param preserveOnPause preserve the EGL context when paused
     */
    fun setPreserveEGLContextOnPause(preserveOnPause: Boolean) {
        mPreserveEGLContextOnPause = preserveOnPause
    }

    /**
     * @return true if the EGL context will be preserved when paused
     */
    fun getPreserveEGLContextOnPause(): Boolean {
        return mPreserveEGLContextOnPause
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that
     * will call the renderer, which in turn causes the rendering to start.
     *
     * This method should be called once and only once in the life-cycle of
     * a GLTextureView.
     *
     * The following GLTextureView methods can only be called *before*
     * setRenderer is called:
     *
     *  * [.setEGLConfigChooser]
     *  * [.setEGLConfigChooser]
     *  * [.setEGLConfigChooser]
     *
     *
     *
     * The following GLTextureView methods can only be called *after*
     * setRenderer is called:
     *
     *  * [.getRenderMode]
     *  * [.onPause]
     *  * [.onResume]
     *  * [.queueEvent]
     *  * [.requestRender]
     *  * [.setRenderMode]
     *
     *
     * @param renderer the renderer to use to perform OpenGL drawing.
     */
    fun setRenderer(renderer: Renderer?) {
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = SimpleEGLConfigChooser(true)
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = DefaultContextFactory()
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = DefaultWindowSurfaceFactory()
        }
        mRenderer = renderer
        mGLThread.start()
    }

    /**
     * Install a custom EGLContextFactory.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    fun setEGLContextFactory(factory: EGLContextFactory?) {
        mEGLContextFactory = factory
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    fun setEGLWindowSurfaceFactory(factory: EGLWindowSurfaceFactory?) {
        mEGLWindowSurfaceFactory = factory
    }

    /**
     * Install a custom EGLConfigChooser.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     * @param configChooser
     */
    fun setEGLConfigChooser(configChooser: EGLConfigChooser?) {
        mEGLConfigChooser = configChooser
    }

    /**
     * Install a config chooser which will choose a config
     * as close to 16-bit RGB as possible, with or without an optional depth
     * buffer as close to 16-bits as possible.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     *
     * @param needDepth
     */
    fun setEGLConfigChooser(needDepth: Boolean) {
        setEGLConfigChooser(SimpleEGLConfigChooser(needDepth))
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified depthSize and stencilSize,
     * and exactly the specified redSize, greenSize, blueSize and alphaSize.
     *
     * If this method is
     * called, it must be called before [.setRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an RGB_888 surface with a depth buffer depth of
     * at least 16 bits.
     *
     */
    fun setEGLConfigChooser(
        redSize: Int,
        greenSize: Int,
        blueSize: Int,
        alphaSize: Int,
        depthSize: Int,
        stencilSize: Int
    ) {
        setEGLConfigChooser(
            ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize)
        )
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser
     * which EGLContext client version to pick.
     *
     * Use this method to create an OpenGL ES 2.0-compatible context.
     * Example:
     * <pre class="prettyprint">
     * public MyView(Context context) {
     * super(context);
     * setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     * setRenderer(new MyRenderer());
     * }
    </pre> *
     *
     * Note: Activities which require OpenGL ES 2.0 should indicate this by
     * setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's
     * AndroidManifest.xml file.
     *
     * If this method is called, it must be called before [.setRenderer]
     * is called.
     *
     * This method only affects the behavior of the default EGLContexFactory and the
     * default EGLConfigChooser. If
     * [.setEGLContextFactory] has been called, then the supplied
     * EGLContextFactory is responsible for creating an OpenGL ES 2.0-compatible context.
     * If
     * [.setEGLConfigChooser] has been called, then the supplied
     * EGLConfigChooser is responsible for choosing an OpenGL ES 2.0-compatible config.
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    fun setEGLContextClientVersion(version: Int) {
        mEGLContextClientVersion = version
    }

    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when [.requestRender] is called. Defaults to RENDERMODE_CONTINUOUSLY.
     *
     *
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     *
     *
     * This method can only be called after [.setRenderer]
     *
     * @param renderMode one of the RENDERMODE_X constants
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    fun setRenderMode(renderMode: Int) {
        mGLThread.renderMode = renderMode
    }

    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     * @return the current rendering mode.
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    fun getRenderMode(): Int {
        return mGLThread.renderMode
    }

    /**
     * Request that the renderer render a frame.
     * This method is typically used when the render mode has been set to
     * [.RENDERMODE_WHEN_DIRTY], so that frames are only rendered on demand.
     * May be called
     * from any thread. Must not be called before a renderer has been set.
     */
    fun requestRender() {
        mGLThread.requestRender()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    fun surfaceCreated() {
        mGLThread.surfaceCreated()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    open fun surfaceDestroyed() {
        // Surface will be destroyed when we return
        mGLThread.surfaceDestroyed()
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    fun surfaceChanged(w: Int, h: Int) {
        mGLThread.onWindowResize(w, h)
    }

    /**
     * This method is part of the SurfaceHolder.Callback2 interface, and is
     * not normally called or subclassed by clients of GLTextureView.
     */
    fun surfaceRedrawNeededAsync(finishDrawing: Runnable) {
        mGLThread.requestRenderAndNotify(finishDrawing)
    }

    /**
     * Pause the rendering thread, optionally tearing down the EGL context
     * depending upon the value of [.setPreserveEGLContextOnPause].
     *
     * This method should be called when it is no longer desirable for the
     * GLTextureView to continue rendering, such as in response to
     * [Activity.onStop][android.app.Activity.onStop].
     *
     * Must not be called before a renderer has been set.
     */
    @CallSuper
    fun onPause() {
        mGLThread.onPause()
    }

    /**
     * Resumes the rendering thread, re-creating the OpenGL context if necessary. It
     * is the counterpart to [.onPause].
     *
     * This method should typically be called in
     * [Activity.onStart][android.app.Activity.onStart].
     *
     * Must not be called before a renderer has been set.
     */
    @CallSuper
    fun onResume() {
        mGLThread.onResume()
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     * @param r the runnable to be run on the GL rendering thread.
     */
    fun queueEvent(r: Runnable?) {
        mGLThread.queueEvent(r)
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLTextureView.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mDetached && mRenderer != null) {
            var renderMode = RENDERMODE_CONTINUOUSLY
            mGLThread.renderMode?.let { renderMode = it }
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread.renderMode = renderMode
            }
            mGLThread.start()
        }
        mDetached = false
    }

    override fun onDetachedFromWindow() {
        mGLThread.weakReference.clear()
        mGLThread.requestExitAndWait()
        mDetached = true
        surfaceTextureListener = null
        super.onDetachedFromWindow()
    }

    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    /**
     * An interface used to wrap a GL interface.
     *
     * Typically
     * used for implementing debugging and tracing on top of the default
     * GL interface. You would typically use this by creating your own class
     * that implemented all the GL methods by delegating to another GL instance.
     * Then you could add your own behavior before or after calling the
     * delegate. All the GLWrapper would do was instantiate and return the
     * wrapper GL instance:
     * <pre class="prettyprint">
     * class MyGLWrapper implements GLWrapper {
     * GL wrap(GL gl) {
     * return new MyGLImplementation(gl);
     * }
     * static class MyGLImplementation implements GL,GL10,GL11,... {
     * ...
     * }
     * }
    </pre> *
     * @see .setGLWrapper
     */
    interface GLWrapper {
        /**
         * Wraps a gl interface in another gl interface.
         * @param gl a GL interface that is to be wrapped.
         * @return either the input argument or another GL object that wraps the input argument.
         */
        fun wrap(gl: GL?): GL?
    }

    /**
     * A generic renderer interface.
     *
     *
     * The renderer is responsible for making OpenGL calls to render a frame.
     *
     *
     * GLTextureView clients typically create their own classes that implement
     * this interface, and then call [GLTextureView.setRenderer] to
     * register the renderer with the GLTextureView.
     *
     *
     *
     * <div class="special reference">
     * <h3>Developer Guides</h3>
    </div> *
     * For more information about how to use OpenGL, read the
     * [OpenGL]({@docRoot}guide/topics/graphics/opengl.html) developer guide.
     *
     *
     * <h3>Threading</h3>
     * The renderer will be called on a separate thread, so that rendering
     * performance is decoupled from the UI thread. Clients typically need to
     * communicate with the renderer from the UI thread, because that's where
     * input events are received. Clients can communicate using any of the
     * standard Java techniques for cross-thread communication, or they can
     * use the [GLTextureView.queueEvent] convenience method.
     *
     *
     * <h3>EGL Context Lost</h3>
     * There are situations where the EGL rendering context will be lost. This
     * typically happens when device wakes up after going to sleep. When
     * the EGL context is lost, all OpenGL resources (such as textures) that are
     * associated with that context will be automatically deleted. In order to
     * keep rendering correctly, a renderer must recreate any lost resources
     * that it still needs. The [.onSurfaceCreated] method
     * is a convenient place to do this.
     *
     *
     * @see .setRenderer
     */
    interface Renderer {
        /**
         * Called when the surface is created or recreated.
         *
         *
         * Called when the rendering thread
         * starts and whenever the EGL context is lost. The EGL context will typically
         * be lost when the Android device awakes after going to sleep.
         *
         *
         * Since this method is called at the beginning of rendering, as well as
         * every time the EGL context is lost, this method is a convenient place to put
         * code to create resources that need to be created when the rendering
         * starts, and that need to be recreated when the EGL context is lost.
         * Textures are an example of a resource that you might want to create
         * here.
         *
         *
         * Note that when the EGL context is lost, all OpenGL resources associated
         * with that context will be automatically deleted. You do not need to call
         * the corresponding "glDelete" methods such as glDeleteTextures to
         * manually delete these lost resources.
         *
         *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         * @param config the EGLConfig of the created surface. Can be used
         * to create matching pbuffers.
         */
        fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

        /**
         * Called when the surface changed size.
         *
         *
         * Called after the surface is created and whenever
         * the OpenGL ES surface size changes.
         *
         *
         * Typically you will set your viewport here. If your camera
         * is fixed then you could also set your projection matrix here:
         * <pre class="prettyprint">
         * void onSurfaceChanged(GL10 gl, int width, int height) {
         * gl.glViewport(0, 0, width, height);
         * // for a fixed camera, set the projection too
         * float ratio = (float) width / height;
         * gl.glMatrixMode(GL10.GL_PROJECTION);
         * gl.glLoadIdentity();
         * gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
         * }
        </pre> *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         * @param width
         * @param height
         */
        fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

        /**
         * Called to draw the current frame.
         *
         *
         * This method is responsible for drawing the current frame.
         *
         *
         * The implementation of this method typically looks like this:
         * <pre class="prettyprint">
         * void onDrawFrame(GL10 gl) {
         * gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         * //... other gl calls to render the scene ...
         * }
        </pre> *
         * @param gl the GL interface. Use `instanceof` to
         * test if the interface supports GL11 or higher interfaces.
         */
        fun onDrawFrame(gl: GL10?)
    }

    /**
     * An interface for customizing the eglCreateContext and eglDestroyContext calls.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLContextFactory]
     */
    interface EGLContextFactory {
        fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext?
        fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?)
    }

    private inner class DefaultContextFactory : EGLContextFactory {

        override fun createContext(
            egl: EGL10?,
            display: EGLDisplay?,
            eglConfig: EGLConfig?
        ): EGLContext? {
            val attribList = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                EGL10.EGL_NONE
            )
            return egl?.eglCreateContext(
                display, eglConfig, EGL10.EGL_NO_CONTEXT,
                if (mEGLContextClientVersion != 0) attribList else null
            )
        }

        override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
            // no op
        }
    }

    /**
     * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLWindowSurfaceFactory]
     */
    interface EGLWindowSurfaceFactory {
        /**
         * @return null if the surface cannot be constructed.
         */
        fun createWindowSurface(
            egl: EGL10?, display: EGLDisplay?, config: EGLConfig?,
            nativeWindow: Any?
        ): EGLSurface?

        fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?)
    }

    private inner class DefaultWindowSurfaceFactory : EGLWindowSurfaceFactory {
        override fun createWindowSurface(
            egl: EGL10?, display: EGLDisplay?, config: EGLConfig?, nativeWindow: Any?
        ): EGLSurface? {
            var result: EGLSurface? = null
            try {
                result = egl?.eglCreateWindowSurface(display, config, nativeWindow, null)
            } catch (_: IllegalArgumentException) {
                // ignored
            }
            return result
        }

        override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
            egl?.eglDestroySurface(display, surface)
        }
    }

    /**
     * An interface for choosing an EGLConfig configuration from a list of
     * potential configurations.
     *
     *
     * This interface must be implemented by clients wishing to call
     * [GLTextureView.setEGLConfigChooser]
     */
    interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * [EGL10.eglChooseConfig] and iterating through the results. Please consult the
         * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
         * @param egl the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig?
    }

    private abstract inner class BaseConfigChooser(configSpec: IntArray) :
        EGLConfigChooser {
        override fun chooseConfig(egl: EGL10?, display: EGLDisplay?): EGLConfig? {
            val numConfig = IntArray(1)
            require(
                egl?.eglChooseConfig(
                    display, mConfigSpec, null, 0,
                    numConfig
                ) == true
            ) { "eglChooseConfig failed" }
            val numConfigs = numConfig[0]
            require(numConfigs > 0) { "No configs match configSpec" }
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            require(
                egl?.eglChooseConfig(
                    display, mConfigSpec, configs, numConfigs,
                    numConfig
                ) == true
            ) { "eglChooseConfig#2 failed" }
            return chooseConfig(egl, display, configs)
                ?: throw IllegalArgumentException("No config chosen")
        }

        abstract fun chooseConfig(
            egl: EGL10?, display: EGLDisplay?,
            configs: Array<EGLConfig?>?
        ): EGLConfig?

        protected var mConfigSpec: IntArray
        private fun filterConfigSpec(configSpec: IntArray): IntArray {
            if (mEGLContextClientVersion != 2 && mEGLContextClientVersion != 3) {
                return configSpec
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            val len = configSpec.size
            val newConfigSpec = IntArray(len + 2)
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
            if (mEGLContextClientVersion == 2) {
                newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT /* EGL_OPENGL_ES2_BIT */
            } else {
                newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR /* EGL_OPENGL_ES3_BIT_KHR */
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE
            return newConfigSpec
        }

        init {
            mConfigSpec = filterConfigSpec(configSpec)
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    private open inner class ComponentSizeChooser(
        redSize: Int, greenSize: Int, blueSize: Int,
        alphaSize: Int, depthSize: Int, stencilSize: Int
    ) :
        BaseConfigChooser(
            intArrayOf(
                EGL10.EGL_RED_SIZE, redSize,
                EGL10.EGL_GREEN_SIZE, greenSize,
                EGL10.EGL_BLUE_SIZE, blueSize,
                EGL10.EGL_ALPHA_SIZE, alphaSize,
                EGL10.EGL_DEPTH_SIZE, depthSize,
                EGL10.EGL_STENCIL_SIZE, stencilSize,
                EGL10.EGL_NONE
            )
        ) {

        override fun chooseConfig(
            egl: EGL10?,
            display: EGLDisplay?,
            configs: Array<EGLConfig?>?
        ): EGLConfig? {
            if (configs == null) {
                return null
            }
            for (config in configs) {
                val d = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_DEPTH_SIZE
                )
                val s = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_STENCIL_SIZE
                )
                if (d >= mDepthSize && s >= mStencilSize) {
                    val r = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_RED_SIZE
                    )
                    val g = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_GREEN_SIZE
                    )
                    val b = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_BLUE_SIZE
                    )
                    val a = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_ALPHA_SIZE
                    )
                    if (r == mRedSize && g == mGreenSize
                        && b == mBlueSize && a == mAlphaSize
                    ) {
                        return config
                    }
                }
            }
            return null
        }

        private fun findConfigAttrib(
            egl: EGL10?, display: EGLDisplay?,
            config: EGLConfig?, attribute: Int
        ): Int {
            return if (egl?.eglGetConfigAttrib(display, config, attribute, mValue) == true) {
                mValue[0]
            } else 0
        }

        private val mValue: IntArray = IntArray(1)

        // Subclasses can adjust these values:
        protected var mRedSize: Int
        protected var mGreenSize: Int
        protected var mBlueSize: Int
        protected var mAlphaSize: Int
        protected var mDepthSize: Int
        protected var mStencilSize: Int

        init {
            mRedSize = redSize
            mGreenSize = greenSize
            mBlueSize = blueSize
            mAlphaSize = alphaSize
            mDepthSize = depthSize
            mStencilSize = stencilSize
        }
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     *
     */
    private inner class SimpleEGLConfigChooser(withDepthBuffer: Boolean) :
        ComponentSizeChooser(8, 8, 8, 0, if (withDepthBuffer) 16 else 0, 0)

    /**
     * An EGL helper class.
     */
    private inner class EglHelper(private val mGLTextureViewWeakRef: WeakReference<GLTextureView>) {

        var mEgl: EGL10? = null
        var mEglDisplay: EGLDisplay? = null
        var mEglSurface: EGLSurface? = null
        var mEglConfig: EGLConfig? = null

        var mEglContext: EGLContext? = null

        /**
         * Initialize EGL for a given configuration spec.
         */
        fun start() {
            mEgl = EGLContext.getEGL() as EGL10
            mEglDisplay = mEgl?.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }

            val version = IntArray(2)
            if (mEgl?.eglInitialize(mEglDisplay, version) == false) {
                throw RuntimeException("eglInitialize failed")
            }
            val view = mGLTextureViewWeakRef.get()
            if (view == null) {
                mEglConfig = null
                mEglContext = null
            } else {
                mEglConfig = view.mEGLConfigChooser?.chooseConfig(mEgl, mEglDisplay)

                mEglContext =
                    view.mEGLContextFactory?.createContext(mEgl, mEglDisplay, mEglConfig)
            }
            if (mEglContext == null || mEglContext === EGL10.EGL_NO_CONTEXT) {
                mEglContext = null
            }
            mEglSurface = null
        }

        /**
         * Create an egl surface for the current SurfaceHolder surface. If a surface
         * already exists, destroy it before creating the new surface.
         *
         * @return true if the surface was created successfully.
         */
        fun createSurface(): Boolean {
            /*
             * Check preconditions.
             */if (mEgl == null) {
                throw RuntimeException("egl not initialized")
            }
            if (mEglDisplay == null) {
                throw RuntimeException("eglDisplay not initialized")
            }
            if (mEglConfig == null) {
                throw RuntimeException("mEglConfig not initialized")
            }

            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp()

            /*
             * Create an EGL surface we can render into.
             */
            val view = mGLTextureViewWeakRef.get()
            mEglSurface = view?.mEGLWindowSurfaceFactory?.createWindowSurface(
                mEgl,
                mEglDisplay, mEglConfig, view.surfaceTexture
            )
            if (mEglSurface == null || mEglSurface === EGL10.EGL_NO_SURFACE) {
                return false
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */if (mEgl?.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext) == false) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                return false
            }
            return true
        }

        /**
         * Create a GL object for the current EGL context.
         * @return
         */
        fun createGL(): GL? {
            var gl = mEglContext?.gl
            val view = mGLTextureViewWeakRef.get()
            if (view?.mGLWrapper != null) {
                gl = view.mGLWrapper?.wrap(gl)
            }
            return gl
        }

        /**
         * Display the current render surface.
         * @return the EGL error code from eglSwapBuffers.
         */
        fun swap(): Int {
            return if (mEgl?.eglSwapBuffers(mEglDisplay, mEglSurface) == false) {
                mEgl!!.eglGetError()
            } else EGL10.EGL_SUCCESS
        }

        fun destroySurface() {
            destroySurfaceImp()
        }

        private fun destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface !== EGL10.EGL_NO_SURFACE) {
                mEgl!!.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT
                )
                val view = mGLTextureViewWeakRef.get()
                view?.mEGLWindowSurfaceFactory?.destroySurface(mEgl, mEglDisplay, mEglSurface)
                mEglSurface = null
            }
        }

        fun finish() {
            if (mEglContext != null) {
                val view = mGLTextureViewWeakRef.get()
                view?.mEGLContextFactory?.destroyContext(mEgl, mEglDisplay, mEglContext)
                mEglContext = null
            }
            if (mEglDisplay != null) {
                mEgl!!.eglTerminate(mEglDisplay)
                mEglDisplay = null
            }
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     *
     * All potentially blocking synchronization is done through the
     * sGLThreadManager object. This avoids multiple-lock ordering issues.
     *
     */
    private inner class GLThread(val weakReference: WeakReference<GLTextureView>) :
        Thread() {

        override fun run() {
            name = "GLThread $id"
            try {
                guardedRun()
            } catch (e: InterruptedException) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this)
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglSurfaceLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false
                mEglHelper.destroySurface()
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private fun stopEglContextLocked() {
            if (mHaveEglContext) {
                mEglHelper.finish()
                mHaveEglContext = false
                sGLThreadManager.releaseEglContextLocked(this)
            }
        }

        @Throws(InterruptedException::class)
        private fun guardedRun() {
            mEglHelper = EglHelper(weakReference)
            mHaveEglContext = false
            mHaveEglSurface = false
            mWantRenderNotification = false
            try {
                var gl: GL10? = null
                var createEglContext = false
                var createEglSurface = false
                var createGlInterface = false
                var lostEglContext = false
                var sizeChanged = false
                var wantRenderNotification = false
                var doRenderNotification = false
                var askedToReleaseEglContext = false
                var w = 0
                var h = 0
                var event: Runnable? = null
                var finishDrawingRunnable: Runnable? = null
                while (true) {
                    threadLock.withLock {
                        while (true) {
                            if (mShouldExit) {
                                return
                            }
                            if (mEventQueue.isNotEmpty()) {
                                event = mEventQueue.removeAt(0)
                                break
                            }

                            // Update the pause state.
                            var pausing = false
                            if (mPaused != mRequestPaused) {
                                pausing = mRequestPaused
                                mPaused = mRequestPaused
                                threadLockCondition.signalAll()
                            }

                            // Do we need to give up the EGL context?
                            if (mShouldReleaseEglContext) {
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                mShouldReleaseEglContext = false
                                askedToReleaseEglContext = true
                            }

                            // Have we lost the EGL context?
                            if (lostEglContext) {
                                stopEglSurfaceLocked()
                                stopEglContextLocked()
                                lostEglContext = false
                            }

                            // When pausing, release the EGL surface:
                            if (pausing && mHaveEglSurface) {
                                stopEglSurfaceLocked()
                            }

                            // When pausing, optionally release the EGL Context:
                            if (pausing && mHaveEglContext) {
                                val view = weakReference.get()
                                val preserveEglContextOnPause =
                                    view?.mPreserveEGLContextOnPause ?: false
                                if (!preserveEglContextOnPause) {
                                    stopEglContextLocked()
                                }
                            }

                            // Have we lost the SurfaceView surface?
                            if (!mHasSurface && !mWaitingForSurface) {
                                if (mHaveEglSurface) {
                                    stopEglSurfaceLocked()
                                }
                                mWaitingForSurface = true
                                mSurfaceIsBad = false
                                threadLockCondition.signalAll()
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                mWaitingForSurface = false
                                threadLockCondition.signalAll()
                            }
                            if (doRenderNotification) {

                                mWantRenderNotification = false
                                doRenderNotification = false
                                mRenderComplete = true
                                threadLockCondition.signalAll()
                            }
                            if (mFinishDrawingRunnable != null) {
                                finishDrawingRunnable = mFinishDrawingRunnable
                                mFinishDrawingRunnable = null
                            }

                            // Ready to draw?
                            if (readyToDraw()) {

                                // If we don't have an EGL context, try to acquire one.
                                if (!mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = false
                                    } else {
                                        try {
                                            mEglHelper.start()
                                        } catch (t: RuntimeException) {
                                            sGLThreadManager.releaseEglContextLocked(this)
                                            throw t
                                        }
                                        mHaveEglContext = true
                                        createEglContext = true
                                        threadLockCondition.signalAll()
                                    }
                                }
                                if (mHaveEglContext && !mHaveEglSurface) {
                                    mHaveEglSurface = true
                                    createEglSurface = true
                                    createGlInterface = true
                                    sizeChanged = true
                                }
                                if (mHaveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true
                                        w = mWidth
                                        h = mHeight
                                        mWantRenderNotification = true

                                        // Destroy and recreate the EGL surface.
                                        createEglSurface = true
                                        mSizeChanged = false
                                    }
                                    mRequestRender = false
                                    threadLockCondition.signalAll()
                                    if (mWantRenderNotification) {
                                        wantRenderNotification = true
                                    }
                                    break
                                }
                            } else {
                                if (finishDrawingRunnable != null) {
                                    finishDrawingRunnable?.run()
                                    finishDrawingRunnable = null
                                }
                            }
                            // By design, this is the only place in a GLThread thread where we wait().

                            threadLockCondition.await()
                        }
                    } // end of synchronized(sGLThreadManager)
                    if (event != null) {
                        event?.run()
                        event = null
                        continue
                    }
                    if (createEglSurface) {
                        if (mEglHelper.createSurface()) {
                            threadLock.withLock {
                                mFinishedCreatingEglSurface = true
                                threadLockCondition.signalAll()
                            }
                        } else {
                            threadLock.withLock {
                                mFinishedCreatingEglSurface = true
                                mSurfaceIsBad = true
                                threadLockCondition.signalAll()
                            }
                            continue
                        }
                        createEglSurface = false
                    }
                    if (createGlInterface) {
                        gl = mEglHelper.createGL() as GL10
                        createGlInterface = false
                    }
                    if (createEglContext) {
                        val view = weakReference.get()
                        if (view != null) {
                            view.mRenderer?.onSurfaceCreated(gl, mEglHelper.mEglConfig)
                        }
                        createEglContext = false
                    }
                    if (sizeChanged) {
                        val view = weakReference.get()
                        if (view != null) {
                            view.mRenderer?.onSurfaceChanged(gl, w, h)
                        }
                        sizeChanged = false
                    }
                    run {
                        val view = weakReference.get()
                        if (view != null) {
                            view.mRenderer?.onDrawFrame(gl)
                            if (finishDrawingRunnable != null) {
                                finishDrawingRunnable?.run()
                                finishDrawingRunnable = null
                            }
                        }
                    }
                    when (val swapError = mEglHelper.swap()) {
                        EGL10.EGL_SUCCESS -> {
                            // no op
                        }

                        EGL11.EGL_CONTEXT_LOST -> {
                            lostEglContext = true
                        }

                        else -> {
                            // Other errors typically mean that the current surface is bad,
                            // probably because the SurfaceView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            threadLock.withLock {
                                mSurfaceIsBad = true
                                threadLockCondition.signalAll()
                            }
                        }
                    }
                    if (wantRenderNotification) {
                        doRenderNotification = true
                        wantRenderNotification = false
                    }
                }
            } finally {
                /*
                 * clean-up everything...
                 */
                threadLock.withLock {
                    stopEglSurfaceLocked()
                    stopEglContextLocked()
                }
            }
        }

        fun ableToDraw(): Boolean {
            return mHaveEglContext && mHaveEglSurface && readyToDraw()
        }

        private fun readyToDraw(): Boolean {
            return (!mPaused && mHasSurface && !mSurfaceIsBad
                    && mWidth > 0 && mHeight > 0
                    && (mRequestRender || mRenderMode == RENDERMODE_CONTINUOUSLY))
        }

        var renderMode: Int
            get() {
                threadLock.withLock { return mRenderMode }
            }
            set(renderMode) {
                require((renderMode in RENDERMODE_WHEN_DIRTY..RENDERMODE_CONTINUOUSLY)) { "renderMode" }
                threadLock.withLock {
                    mRenderMode = renderMode
                    threadLockCondition.signalAll()
                }
            }

        fun requestRender() {
            threadLock.withLock {
                mRequestRender = true
                threadLockCondition.signalAll()
            }
        }

        fun requestRenderAndNotify(finishDrawing: Runnable?) {
            threadLock.withLock {

                // If we are already on the GL thread, this means a client callback
                // has caused reentrancy, for example via updating the SurfaceView parameters.
                // We will return to the client rendering code, so here we don't need to
                // do anything.
                if (currentThread() === this) {
                    return
                }
                mWantRenderNotification = true
                mRequestRender = true
                mRenderComplete = false
                mFinishDrawingRunnable = finishDrawing
                threadLockCondition.signalAll()
            }
        }

        fun surfaceCreated() {
            threadLock.withLock {
                mHasSurface = true
                mFinishedCreatingEglSurface = false
                threadLockCondition.signalAll()
                while ((mWaitingForSurface
                            && !mFinishedCreatingEglSurface
                            && !mExited)
                ) {
                    try {
                        threadLockCondition.await()
                    } catch (e: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun surfaceDestroyed() {
            threadLock.withLock {
                mHasSurface = false
                threadLockCondition.signalAll()
                while ((!mWaitingForSurface) && (!mExited)) {
                    try {
                        threadLockCondition.await()
                    } catch (e: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onPause() {
            threadLock.withLock {
                mRequestPaused = true
                threadLockCondition.signalAll()
                while ((!mExited) && (!mPaused)) {
                    try {
                        threadLockCondition.await()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onResume() {
            threadLock.withLock {
                mRequestPaused = false
                mRequestRender = true
                mRenderComplete = false
                threadLockCondition.signalAll()
                while ((!mExited) && mPaused && (!mRenderComplete)) {
                    try {
                        threadLockCondition.await()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun onWindowResize(w: Int, h: Int) {
            threadLock.withLock {
                mWidth = w
                mHeight = h
                mSizeChanged = true
                mRequestRender = true
                mRenderComplete = false

                // If we are already on the GL thread, this means a client callback
                // has caused reentrancy, for example via updating the SurfaceView parameters.
                // We need to process the size change eventually though and update our EGLSurface.
                // So we set the parameters and return so they can be processed on our
                // next iteration.
                if (currentThread() === this) {
                    return
                }
                threadLockCondition.signalAll()

                // Wait for thread to react to resize and render a frame
                while ((!mExited && !mPaused && !mRenderComplete
                            && ableToDraw())
                ) {
                    try {
                        threadLockCondition.await()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            threadLock.withLock {
                mShouldExit = true
                threadLockCondition.signalAll()
                while (!mExited) {
                    try {
                        threadLockCondition.await()
                    } catch (ex: InterruptedException) {
                        currentThread().interrupt()
                    }
                }
            }
        }

        fun requestReleaseEglContextLocked() {
            mShouldReleaseEglContext = true
            threadLockCondition.signalAll()
        }

        /**
         * Queue an "event" to be run on the GL rendering thread.
         * @param r the runnable to be run on the GL rendering thread.
         */
        fun queueEvent(r: Runnable?) {
            requireNotNull(r) { "r must not be null" }
            threadLock.withLock {
                mEventQueue.add(r)
                threadLockCondition.signalAll()
            }
        }

        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private var mShouldExit = false
        var mExited = false
        private var mRequestPaused = false
        private var mPaused = false
        private var mHasSurface = false
        private var mSurfaceIsBad = false
        private var mWaitingForSurface = false
        private var mHaveEglContext = false
        private var mHaveEglSurface = false
        private var mFinishedCreatingEglSurface = false
        private var mShouldReleaseEglContext = false
        private var mWidth = 0
        private var mHeight = 0
        private var mRenderMode: Int
        private var mRequestRender = true
        private var mWantRenderNotification: Boolean
        private var mRenderComplete = false
        private val mEventQueue = ArrayList<Runnable>()
        private var mSizeChanged = true
        private var mFinishDrawingRunnable: Runnable? = null

        // End of member variables protected by the sGLThreadManager monitor.

        private lateinit var mEglHelper: EglHelper

        init {
            mRenderMode = RENDERMODE_CONTINUOUSLY
            mWantRenderNotification = false
        }
    }

    private inner class GLThreadManager {
        @Synchronized
        fun threadExiting(thread: GLThread) {
            threadLock.withLock {
                thread.mExited = true
                threadLockCondition.signalAll()
            }
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        fun releaseEglContextLocked(thread: GLThread?) {
            threadLock.withLock {
                threadLockCondition.signalAll()
            }
        }
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        surfaceChanged(right - left, bottom - top)
    }
}


private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098