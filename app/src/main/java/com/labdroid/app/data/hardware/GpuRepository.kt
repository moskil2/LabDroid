package com.labdroid.app.data.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class GpuSnapshot(
    val renderer: String,
    val vendor: String,
    val glVersion: String,
    val glslVersion: String,
    val eglVersion: String,
    val glExtensionsCount: Int,
    val maxTextureSize: Int?,
    val maxViewportDims: String?,
    val vulkanSupported: Boolean,
    val vulkanVersion: String?,
    val vulkanHardwareLevel: Int?,
    val vulkanComputeSupported: Boolean,
)

class GpuRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getGpuSnapshot(): GpuSnapshot {
        val strings = queryGlStrings()
        val vulkanSupported = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        val vulkanVersionFeature = context.packageManager.systemAvailableFeatures
            .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }
        val vulkanLevelFeature = context.packageManager.systemAvailableFeatures
            .firstOrNull { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL }
        val vulkanComputeSupported = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_COMPUTE)
        val unknown = context.getString(R.string.cameras_unknown)
        return GpuSnapshot(
            renderer = strings?.renderer ?: unknown,
            vendor = strings?.vendor ?: unknown,
            glVersion = strings?.version ?: unknown,
            glslVersion = strings?.glslVersion ?: unknown,
            eglVersion = strings?.eglVersion ?: unknown,
            glExtensionsCount = strings?.extensionsCount ?: 0,
            maxTextureSize = strings?.maxTextureSize,
            maxViewportDims = strings?.maxViewportDims,
            vulkanSupported = vulkanSupported,
            vulkanVersion = vulkanVersionFeature?.version?.takeIf { it > 0 }?.let { decodeVulkanVersion(it) },
            vulkanHardwareLevel = vulkanLevelFeature?.version,
            vulkanComputeSupported = vulkanComputeSupported,
        )
    }

    private fun decodeVulkanVersion(encoded: Int): String {
        val major = encoded shr 22
        val minor = (encoded shr 12) and 0x3ff
        val patch = encoded and 0xfff
        return "$major.$minor.$patch"
    }

    private data class GlStrings(
        val renderer: String,
        val vendor: String,
        val version: String,
        val glslVersion: String,
        val eglVersion: String,
        val extensionsCount: Int,
        val maxTextureSize: Int?,
        val maxViewportDims: String?,
    )

    private fun queryGlStrings(): GlStrings? {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return null

        val versionOut = IntArray(2)
        if (!EGL14.eglInitialize(display, versionOut, 0, versionOut, 1)) return null
        val eglVersion = "${versionOut[0]}.${versionOut[1]}"

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
                    val extensionsCount = (GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: "")
                        .split(" ")
                        .count { it.isNotBlank() }
                    val unknown = context.getString(R.string.cameras_unknown)
                    val maxTextureSizeOut = IntArray(1)
                    GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSizeOut, 0)
                    val maxViewportDimsOut = IntArray(2)
                    GLES20.glGetIntegerv(GLES20.GL_MAX_VIEWPORT_DIMS, maxViewportDimsOut, 0)
                    return GlStrings(
                        renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: unknown,
                        vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: unknown,
                        version = GLES20.glGetString(GLES20.GL_VERSION) ?: unknown,
                        glslVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION) ?: unknown,
                        eglVersion = eglVersion,
                        extensionsCount = extensionsCount,
                        maxTextureSize = maxTextureSizeOut[0].takeIf { it > 0 },
                        maxViewportDims = "${maxViewportDimsOut[0]} × ${maxViewportDimsOut[1]}"
                            .takeIf { maxViewportDimsOut[0] > 0 },
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
