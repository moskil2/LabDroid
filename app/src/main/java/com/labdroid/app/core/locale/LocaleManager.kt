package com.labdroid.app.core.locale

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val tag: String, val flagEmoji: String) {
    ENGLISH("en", "🇬🇧"),
    POLISH("pl", "🇵🇱"),
}

/**
 * Manual per-app language switching. MainActivity is a plain ComponentActivity (no
 * AppCompatActivity), so the AndroidX per-app-language APIs aren't reliably applied pre-API 33;
 * wrapping the base context in [attachBaseContext] with a locale-scoped Configuration works
 * uniformly across the app's minSdk (26) instead.
 */
object LocaleManager {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE_TAG = "language_tag"
    private val DEFAULT_LANGUAGE = AppLanguage.ENGLISH

    fun getLanguage(context: Context): AppLanguage {
        val tag = prefs(context).getString(KEY_LANGUAGE_TAG, null)
        return AppLanguage.entries.firstOrNull { it.tag == tag } ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        // commit() (not apply()) so the write lands on disk synchronously before restartProcess()
        // hard-kills the process — apply()'s async write would otherwise race the exit() call.
        prefs(context).edit().putString(KEY_LANGUAGE_TAG, language.tag).commit()
    }

    /**
     * Application.attachBaseContext only runs once at process start, so Hilt's
     * @ApplicationContext-derived resources (used by ViewModels/repositories for label strings)
     * would keep serving the old language after a plain Activity.recreate(). Restarting the whole
     * process guarantees every locale-scoped context picks up the newly saved language.
     */
    fun restartProcess(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    fun wrapContext(base: Context): Context {
        val locale = Locale(getLanguage(base).tag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return ContextWrapper(base.createConfigurationContext(configuration))
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
