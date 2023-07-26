package com.dertefter.ficus

import android.content.Intent
import android.util.Log
import android.widget.RemoteViewsService


class AppWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        //get java.util.ArrayList from intent
        //val day1 = intent.getStringArrayListExtra("day1")
        //Log.e("wseevice", day1.toString())
        return MyFactory(applicationContext, intent)
    }
}