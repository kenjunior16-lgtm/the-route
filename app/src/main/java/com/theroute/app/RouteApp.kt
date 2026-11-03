package com.theroute.app

import android.app.Application
import org.osmdroid.config.Configuration

class RouteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = "TheRoute/1.0"
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
    }
}
