package com.dertefter.ficus

import AppPreferences
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit


class NewAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        //super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_timetable
            )
            lessons(null, context, views, appWidgetManager, appWidgetId)
            val intentSync = Intent(context, NewAppWidget::class.java)
            intentSync.action =
                AppWidgetManager.ACTION_APPWIDGET_UPDATE //You need to specify the action for the intent. Right now that intent is doing nothing for there is no action to be broadcasted.

            val pendingSync = PendingIntent.getBroadcast(
                context,
                0,
                intentSync,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            ) //You need to specify a proper flag for the intent. Or else the intent will become deleted.

            views.setOnClickPendingIntent(R.id.updateButton, pendingSync)
            views.setOnClickPendingIntent(R.id.retry_button, pendingSync)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context!!, NewAppWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }



    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun lessons(week_: Int?, context: Context, views: RemoteViews, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        views.setViewVisibility(R.id.progressBar, View.VISIBLE)
        views.setViewVisibility(R.id.updateButton, View.GONE)
        views.setViewVisibility(R.id.timetable, View.GONE)
        views.setViewVisibility(R.id.connection_error, View.GONE)
        views.setViewVisibility(R.id.no_group, View.GONE)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.e("widget", "lessons")
        var day1: ArrayList<ArrayList<String>> = ArrayList()
        var day2: ArrayList<ArrayList<String>> = ArrayList()
        var day3: ArrayList<ArrayList<String>> = ArrayList()
        var day4: ArrayList<ArrayList<String>> = ArrayList()
        var day5: ArrayList<ArrayList<String>> = ArrayList()
        var day6: ArrayList<ArrayList<String>> = ArrayList()
        var days = arrayOf(day1, day2, day3, day4, day5, day6)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)


        GlobalScope.launch(Dispatchers.Main) {
            try{
                var week_value = ""
                if (week_ != null){
                    week_value = week_.toString()
                }
                if (AppPreferences.group != null){
                    val response = service.getTimetableGuest(AppPreferences.group, week_value)
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val s = doc.body().select("div.schedule__table-body").first()
                        val weekLabel = doc.body().select("div.schedule__title").select("span.schedule__title-label").text()
                        val sessiaNow = weekLabel.contains("сессия")

                        val rows = s.select("> *")
                        val intent = Intent(context, AppWidgetService::class.java)
                        if (weekLabel.contains("неделя")){
                            val week = weekLabel.split(" ")[0].toInt()
                            views.setTextViewText(R.id.week, "Неделя $week")
                        }
                        for (i in 0..rows.size - 1) {
                            val cell = rows[i].select("div.schedule__table-cell")[1]
                            val lessons = cell.select("> *")
                            for (l in lessons){
                                val time = l.select("div.schedule__table-time").text()
                                val items = l.select("div.schedule__table-item")
                                for (t in items){
                                    var name = t.ownText().replace("·", "").replace(",", "")
                                    val type = t.select("span.schedule__table-typework").first().ownText()
                                    val aud = t.parent().parent().select("div.schedule__table-class").text()
                                    var person = ""
                                    for (p in t.select("a")){
                                        if (person == ""){
                                            person = p.text()
                                        }else{
                                            person = person + "\n" + p.text()
                                        }

                                    }

                                    if (name != ""){
                                        days[i].add(arrayListOf(time, name, type, aud, person))
                                    }
                                }
                            }




                        }
                        for (i in 0..days.size - 1){
                            var day = ""
                            for (j in days[i]){
                                day = day + j.toString().replace("[", "").replace("]", "") + ";"
                            }
                            val intentName = "day" + (i).toString()
                            intent.putExtra(intentName, day)
                        }
                        views.setViewVisibility(R.id.progressBar, View.GONE)
                        views.setViewVisibility(R.id.updateButton, View.VISIBLE)
                        views.setViewVisibility(R.id.timetable, View.VISIBLE)
                        views.setRemoteAdapter(R.id.week_lessons, intent)


                        //views.setScrollPosition(R.id.week_lessons, day - 1)
                        appWidgetManager.updateAppWidget(appWidgetId, views)



                    }
                    else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }else{
                    views.setViewVisibility(R.id.progressBar, View.GONE)
                    views.setViewVisibility(R.id.updateButton, View.GONE)
                    views.setViewVisibility(R.id.timetable, View.GONE)
                    views.setViewVisibility(R.id.no_group, View.VISIBLE)
                    views.setViewVisibility(R.id.connection_error, View.GONE)
                    val intent = Intent(context, Auth::class.java)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                    views.setOnClickPendingIntent(R.id.openApp, pendingIntent)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }


            } catch (e: Throwable){
                Log.e("ficus.widget.ok", e.stackTraceToString())
                views.setViewVisibility(R.id.progressBar, View.GONE)
                views.setViewVisibility(R.id.updateButton, View.GONE)
                views.setViewVisibility(R.id.timetable, View.GONE)
                views.setViewVisibility(R.id.no_group, View.GONE)
                views.setViewVisibility(R.id.connection_error, View.VISIBLE)
                appWidgetManager.updateAppWidget(appWidgetId, views)

            } catch (e: Exception){
                Log.e("ficus.widget.ok", e.stackTraceToString())
                views.setViewVisibility(R.id.progressBar, View.GONE)
                views.setViewVisibility(R.id.updateButton, View.GONE)
                views.setViewVisibility(R.id.timetable, View.GONE)
                views.setViewVisibility(R.id.no_group, View.GONE)
                views.setViewVisibility(R.id.connection_error, View.VISIBLE)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

        }
    }



}

