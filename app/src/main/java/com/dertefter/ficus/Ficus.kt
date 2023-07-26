package com.dertefter.ficus

import AppPreferences
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.mobile.ads.common.MobileAds
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


class Ficus : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: Ficus? = null

        fun getCookieJar(): CookieJar? {
            return instance!!.cookieJar
        }
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }
    var cookieJar: CookieJar? = null
    override fun onCreate() {
        super.onCreate()
        AppPreferences.setup(applicationContext)
        //val metrica = getString(R.string.metrica)
        //val config = YandexMetricaConfig.newConfigBuilder(metrica).build()
        //YandexMetrica.activate(applicationContext, config)
        //YandexMetrica.enableActivityAutoTracking(this)

        MobileAds.initialize(
            this
        ) { Log.e("Яндекс Реклама", "SDK инициализирована!") }
        cookieJar = object : CookieJar {
            public val cookieStore: HashMap<String, List<Cookie>> = HashMap()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host]
                return cookies ?: ArrayList()
            }
        }

        val intent = Intent(this, NewAppWidget::class.java)
        intent.action = "android.appwidget.action.APPWIDGET_UPDATE"
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(
            ComponentName(
                this,
                NewAppWidget::class.java
            )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}