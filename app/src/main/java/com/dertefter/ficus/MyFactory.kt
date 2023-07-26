package com.dertefter.ficus

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.RemoteViewsService.RemoteViewsFactory


class MyFactory(private val mContext: Context, val intent: Intent?) :
    RemoteViewsFactory {
    private lateinit var days: Array<String?>
    private var day0: String? = null
    private var day1: String? = null
    private var day2: String? = null
    private var day3: String? = null
    private var day4: String? = null
    private var day5: String? = null
    val positions = arrayOf(false, false, false, false, false, false)
    override fun onCreate() {
        day0 = intent?.getStringExtra("day0")
        day1 = intent?.getStringExtra("day1")
        day2 = intent?.getStringExtra("day2")
        day3 = intent?.getStringExtra("day3")
        day4 = intent?.getStringExtra("day4")
        day5 = intent?.getStringExtra("day5")
        days = arrayOf(day0, day1, day2, day3, day4, day5)
    }
    override fun onDataSetChanged() {

    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return days.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.item_timetable_widget_day)
        rv.removeAllViews(R.id.day_les)
        var day_name = ""
        if (position == 0){
            day_name = "Понедельник"
        }else if (position == 1){
            day_name = "Вторник"
        }else if (position == 2) {
            day_name = "Среда"
        }else if (position == 3) {
            day_name = "Четверг"
        }else if (position == 4) {
            day_name = "Пятница"
        }else if (position == 5) {
            day_name = "Суббота"
        }
        rv.setTextViewText(R.id.day, day_name)

        if (days[position] != null) {
            Log.e("widget", "position: $position")
            Log.e("widget", days[position]!!.toString())
            val lessons = days[position]!!.split(";")
            Log.e("widget", lessons.toString())
            if (positions[position]){
                return rv
            }
            for (l in lessons){
                val lesson_info = l.split(",")
                if (lesson_info.size > 2){
                    val lessonItem = RemoteViews(mContext.packageName, R.layout.item_timetable_widget_lesson)
                    lessonItem.setTextViewText(R.id.time, lesson_info[0])
                    lessonItem.setTextViewText(R.id.lesson, lesson_info[1])
                    lessonItem.setTextViewText(R.id.type, lesson_info[2])
                    lessonItem.setTextViewText(R.id.person, lesson_info[4])
                    lessonItem.setTextViewText(R.id.aud, lesson_info[3])
                    if (lesson_info[3] == ""){
                        lessonItem.setViewVisibility(R.id.aud, View.GONE)
                        lessonItem.setViewVisibility(R.id.im2, View.GONE)
                    }
                    if (lesson_info[4] == ""){
                        lessonItem.setViewVisibility(R.id.person, View.GONE)
                        lessonItem.setViewVisibility(R.id.im3, View.GONE)
                    }
                    if (lesson_info[2].replace(" ", "").replace(" ", "").replace(" ", "") == ""){
                        lessonItem.setViewVisibility(R.id.type, View.GONE)
                    }
                    if (lesson_info[0] == ""){
                        lessonItem.setViewVisibility(R.id.time, View.GONE)
                        lessonItem.setViewVisibility(R.id.im1, View.GONE)
                    }
                    lessonItem.setImageViewResource(R.id.im1, R.drawable.schedule)
                    lessonItem.setImageViewResource(R.id.im2, R.drawable.campus)
                    lessonItem.setImageViewResource(R.id.im3, R.drawable.group)
                    rv.addView(R.id.day_les, lessonItem)

                }

            }

        }
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()

    }

    override fun hasStableIds(): Boolean {
        return false
    }





}