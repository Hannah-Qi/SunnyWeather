package com.example.sunnyweather

import android.app.Application
import android.content.Context

//获取全部context
class SunnyWeatherApplication : Application() {
    companion object{
        const val TOKEN = "RQWJlrB7J1B0bqnE"
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}