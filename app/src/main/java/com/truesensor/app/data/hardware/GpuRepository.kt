package com.truesensor.app.data.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class GpuSnapshot(
    val renderer: String,
    val vendor: String,
    val glVersion: String,
    val vulkanSupported: Boolean,
)

class GpuRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getGpuSnapshot(): GpuSnapshot {
        val strings = queryGlStrings()
        val vulkanSupported = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        return GpuSnapshot(
            renderer = strings?.renderer ?: "Unknown",
            vendor = strings?.vendor ?: "Unknown",
            glVersion = strings?.version ?: "Unknown",
            vulkanSupported = vulkanSupported,
        )
    }

    private data class GlStrings(val renderer: String, val vendor: String, val version: String)

    private fun queryGlStrings(): GlStrings? {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return null

        val versionOut = IntArray(2)
        if (!EGL14.eglInitialize(display, versionOut, 0, versionOut, 1)) return null

        try {
            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            val configOk = EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)
            val config = configs[0]
            if (!configOk || numConfigs[0] == 0 || config == null) return null

            val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val eglContext = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
            if (eglContext == EGL14.EGL_NO_CONTEXT) return null

            try {
                val pbufferAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
                val pbuffer = EGL14.eglCreatePbufferSurface(display, config, pbufferAttribs, 0)
                if (pbuffer == EGL14.EGL_NO_SURFACE) return null

                try {
                    if (!EGL14.eglMakeCurrent(display, pbuffer, pbuffer, eglContext)) return null
                    return GlStrings(
                        renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown",
                        vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown",
                        version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown",
                    )
                } finally {
                    EGL14.eglMakeCurrent(
                        display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT,
                    )
                    EGL14.eglDestroySurface(display, pbuffer)
                }
            } finally {
                EGL14.eglDestroyContext(display, eglContext)
            }
        } finally {
            EGL14.eglTerminate(display)
        }
    }
}
