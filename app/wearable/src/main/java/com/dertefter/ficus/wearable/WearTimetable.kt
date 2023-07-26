package com.dertefter.ficus.wearable

import AppPreferences
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.widget.NestedScrollView
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.lang.Math.abs
import java.util.*

class WearTimetable : Activity() {
    var scrollView: ScrollView? = null
    var linearLayout: LinearLayout? = null
    var loading: ProgressBar? = null
    val screen_top = 0
    var screen_bottom = 0

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_timetable)
        loading = findViewById(R.id.loadingTimeTable)
        linearLayout = findViewById(R.id.timeTableLinearLayout)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screen_bottom = displayMetrics.heightPixels
        scrollView = findViewById(R.id.timeTableScrollView)
        val scrollBounds = Rect()
        scrollView?.getHitRect(scrollBounds)
        val screenHeight = resources.displayMetrics.heightPixels
        scrollView?.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                redraw()
             }

        val calendar = Calendar.getInstance()
        var today: Int = calendar.get(Calendar.DAY_OF_WEEK)
        today -= 1
        if (today < 1) {
            today = 1
        }
        lessons(null)
    }

    fun redraw(){
        for (i in 0 until  linearLayout?.childCount!!) {
            val child = linearLayout?.get(i)
            val location = IntArray(2)
            child?.getLocationInWindow(location)
            val y = location[1]
            val has_animation = child?.hasTransientState()
            if (has_animation == false){
                if (y  + 130 >= screen_bottom || y < screen_top) {
                    ObjectAnimator.ofFloat(child, "alpha", 0.8f).start()
                    ObjectAnimator.ofFloat(child, "scaleX", 0.8f).start()
                    ObjectAnimator.ofFloat(child, "scaleY", 0.8f).start()
                } else {
                    ObjectAnimator.ofFloat(child, "alpha", 1f).start()
                    ObjectAnimator.ofFloat(child, "scaleX", 1f).start()
                    ObjectAnimator.ofFloat(child, "scaleY", 1f).start()
                }
            }

        }
    }

    fun lessons(week_: Int?) {
        val mInflater = LayoutInflater.from(this@WearTimetable)
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()

        val url1 = "https://ciu.nstu.ru/student_study/timetable/timetable_lessons/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url1)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.Study()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        linearLayout?.removeAllViews()
                        ObjectAnimator.ofFloat(linearLayout, "alpha", 0f, 1f).start()
                        var week = 0
                        var is_today = false
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val s = doc.body().select("div.schedule__table-body").first()
                        val weekLabel = doc.body().select("div.schedule__title").select("span.schedule__title-label").text()
                        val sessiaNow = weekLabel.contains("сессия")
                        if (sessiaNow){
                            val item = mInflater.inflate(R.layout.sessia_alert, null, false)
                            linearLayout?.addView(item)
                            return@withContext
                        }
                        val rows = s.select("> *")
                        if (week_ == null){
                            if (weekLabel.contains("каникулы", true)){
                                Toast.makeText(this@WearTimetable, "Сейчас каникулы", Toast.LENGTH_SHORT).show()
                                return@withContext
                            }else{
                                week = weekLabel.split(" ")[0].toInt()
                            }
                            for (i in 0..rows.size - 1) {

                                val day_name = rows[i].select("div.schedule__table-day").text()
                                if (rows[i].select("div.schedule__table-day").hasAttr("data-today") && rows[i].select("div.schedule__table-day").attr("data-today") == "true") {
                                    is_today = true
                                }else{
                                    is_today = false
                                }
                                var day_item = mInflater.inflate(R.layout.day_name, null, false)
                                if (is_today){
                                    day_item = mInflater.inflate(R.layout.day_name_today, null, false)
                                }else{
                                    day_item = mInflater.inflate(R.layout.day_name, null, false)
                                }
                                if(day_name == "пн"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Понедельник"
                                }else if(day_name == "вт"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Вторник"
                                }else if(day_name == "ср"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Среда"
                                }else if(day_name == "чт"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Четверг"
                                }else if(day_name == "пт"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Пятница"
                                }else if(day_name == "сб"){
                                    day_item.findViewById<TextView>(R.id.day_name).text = "Суббота"
                                }
                                linearLayout?.addView(day_item)
                                if(is_today){
                                    scrollView?.post(Runnable { scrollView?.smoothScrollTo(0, day_item.top) })
                                }
                                val time = rows[i].select("div.schedule__table-time").text().toString()
                                val cell = rows[i].select("div.schedule__table-cell")[1]
                                val lessons = cell.select("> *")
                                for (l in lessons){
                                    val time = l.select("div.schedule__table-time").text()
                                    val items = l.select("div.schedule__table-item")
                                    for (t in items){
                                        var current = true
                                        var name = t.ownText().replace("·", "").replace(",", "")
                                        if (t.select("span.schedule__table-label").hasAttr("data-week")){
                                            if (t.select("span.schedule__table-label").attr("data-week") != "current"){
                                                current = false
                                            }

                                        }

                                        val type = t.select("span.schedule__table-typework").first().ownText()
                                        val aud = t.parent().parent().select("div.schedule__table-class").text()
                                        var person = ""
                                        for (p in t.select("a")){
                                            person = person + p.text() + "\n"
                                        }
                                        if (person != ""){
                                            person = person.substring(0, person.length - 1)
                                        }


                                        if (name != "" && current){
                                            var lessonCard = mInflater.inflate(R.layout.item, null, false)
                                            if (is_today){
                                                lessonCard = mInflater.inflate(R.layout.item_today, null, false)
                                            }
                                            lessonCard.findViewById<TextView>(R.id.time).text = time
                                            lessonCard.findViewById<TextView>(R.id.lesson).text = name
                                            lessonCard.findViewById<TextView>(R.id.aud).text = aud
                                            if (person == ""){
                                                (lessonCard.findViewById<TextView>(R.id.person).parent as LinearLayout).visibility = View.GONE
                                            }else{
                                                lessonCard.findViewById<TextView>(R.id.person).text = person
                                            }
                                            if (type == ""){
                                                lessonCard.findViewById<TextView>(R.id.type).visibility = View.GONE
                                            }else{
                                                lessonCard.findViewById<TextView>(R.id.type).text = type
                                            }
                                            if (aud == ""){
                                                (lessonCard.findViewById<TextView>(R.id.aud).parent as LinearLayout).visibility = View.GONE
                                            }
                                            else{
                                                lessonCard.findViewById<TextView>(R.id.aud).text = aud

                                            }

                                            linearLayout?.addView(lessonCard)
                                            ObjectAnimator.ofFloat(lessonCard, "alpha", 0f, 1f).apply {
                                                duration = 260
                                                start()
                                            }
                                        }
                                    }
                                }


                            }

                        } else {
                            val week_number = week_
                            var week_type = ""
                            if (week_number % 2 == 0){
                                week_type = "по чётным"
                            }else{
                                week_type = "по нечётным"
                            }
                            for (i in 0..rows.size - 1) {
                                val time = rows[i].select("div.schedule__table-time").text().toString()
                                val cell = rows[i].select("div.schedule__table-cell")[1]
                                val lessons = cell.select("> *")
                                for (l in lessons){
                                    val time = l.select("div.schedule__table-time").text()
                                    val items = l.select("div.schedule__table-item")
                                    for (t in items){
                                        var current = false
                                        var name = t.ownText().replace("·", "").replace(",", "")
                                        if (t.select("span.schedule__table-label").toString() != ""){
                                            val label = t.select("span.schedule__table-label").select("span").first().text()
                                            Log.e("label", label)
                                            val label_array = label.split(" ")
                                            for (l in label_array){
                                                if (l == week_number.toString()){
                                                    current = true
                                                }
                                            }
                                            if (week_type == label){
                                                current = true
                                            }

                                        }else{
                                            current = true
                                        }


                                        val type = t.select("span.schedule__table-typework").first().ownText()
                                        val aud = t.parent().parent().select("div.schedule__table-class").text()
                                        var person = ""
                                        for (p in t.select("a")){
                                            person = person + p.text() + "\n"
                                        }
                                        if (person != ""){
                                            person = person.substring(0, person.length - 1)
                                        }

                                        if (name != "" && current){
                                            val lessonCard = mInflater.inflate(R.layout.item, null, false)
                                            lessonCard.findViewById<TextView>(R.id.time).text = time
                                            lessonCard.findViewById<TextView>(R.id.lesson).text = name
                                            lessonCard.findViewById<TextView>(R.id.aud).text = aud
                                            if (person == ""){
                                                (lessonCard.findViewById<TextView>(R.id.person).parent as LinearLayout).visibility = View.GONE
                                            }else{
                                                lessonCard.findViewById<TextView>(R.id.person).text = person
                                            }
                                            if (type == ""){
                                                lessonCard.findViewById<TextView>(R.id.type).visibility = View.GONE
                                            }else{
                                                lessonCard.findViewById<TextView>(R.id.type).text = type
                                            }
                                            if (aud == ""){
                                                (lessonCard.findViewById<TextView>(R.id.aud).parent as LinearLayout).visibility = View.GONE
                                            }
                                            else{
                                                lessonCard.findViewById<TextView>(R.id.aud).text = aud

                                            }
                                            linearLayout?.addView(lessonCard)
                                            ObjectAnimator.ofFloat(lessonCard, "alpha", 0f, 1f).apply {
                                                duration = 260
                                                start()
                                            }
                                        }
                                    }
                                }


                            }
                        }


                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }
            } catch (e: Throwable){
                Log.e("ficus.timetable", e.stackTraceToString())
            } catch (e: Exception){
                Log.e("ficus.timetable", e.stackTraceToString())
            }

        }
    }

}
