package com.labdroid.app

import android.app.Application
import android.content.Context
import com.labdroid.app.core.locale.LocaleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LabDroidApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(base))
    }
}
