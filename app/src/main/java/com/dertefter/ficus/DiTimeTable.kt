package com.dertefter.ficus

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.style.ImageSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.google.android.material.card.MaterialCardView
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.*



class DiTimeTable : Fragment(R.layout.di_timetable_fragment), EventsCalendar.Callback {
    var calendar: EventsCalendar? = null
    var eventsView: LinearLayout? = null
    val eventsArray = HashMap<String?, MutableList<String>>()
    var scroll_view: NestedScrollView? = null
    var no_events: LinearLayout? = null
    fun updateEvents(){
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/calendar/proceed/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "get_event_by_role"
        params["group_id"] = "false"
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getDispaceEvents(params)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        var prettyJson = ""
                        try{
                            prettyJson = gson.toJson(
                                JsonParser.parseString(
                                    response.body()
                                        ?.string()
                                )
                            )
                        }
                         catch (e: Exception){
                            Log.e("diTimeTable", e.toString())
                        }
                        val json = JSONObject(prettyJson)
                        if (json.has("events")){
                            val events = json.getJSONArray("events")
                            for(i in 0 until events.length()){
                                val event_name = events.getJSONObject(i).getString("event_title")
                                val event_teacher = events.getJSONObject(i).getString("event_teacher")
                                val event_start = events.getJSONObject(i).getString("event_start")
                                val is_available = events.getJSONObject(i).getString("is_available")
                                var link = ""
                                if (events.getJSONObject(i).has("join_link")){
                                    link = events.getJSONObject(i).getString("join_link")
                                }

                                val event_start_string = event_start.toString().split(" ")[0]
                                val event_time = event_start.toString().split(" ")[1]
                                val eventsThisDay: MutableList<String> = mutableListOf()
                                eventsThisDay.add(event_name)
                                eventsThisDay.add(event_teacher)
                                eventsThisDay.add(is_available)
                                eventsThisDay.add(link)
                                eventsThisDay.add(event_time)
                                eventsArray[event_start_string] = eventsThisDay
                                val c = Calendar.getInstance()
                                Log.e("set", event_start_string.split("-")[1].toInt().toString())
                                val year = event_start_string.split("-")[0].toInt()
                                val month = event_start_string.split("-")[1].toInt() - 1
                                val day = event_start_string.split("-")[2].toInt()
                                c.set(year, month, day)
                                calendar?.addEvent(c)

                            }
                            calendar?.setCurrentSelectedDate(Calendar.getInstance())
                            this@DiTimeTable.onDaySelected(Calendar.getInstance())
                        }

                    }
                }
            } catch (e: Exception){
                Log.e("diTimeTable", e.toString())
            } catch (e: Throwable){
                Log.e("diTimeTable", e.toString())
            } catch (e: IOException){
                Log.e("diTimeTable", e.toString())
            }

        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        no_events = view.findViewById(R.id.no_events)
        calendar = view.findViewById(R.id.eventsCalendar)
        (calendar?.parent as MaterialCardView).addSystemWindowInsetToMargin(top = true)
        scroll_view = view.findViewById(R.id.events_scroll_view)
        eventsView = view.findViewById(R.id.events_view)
        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        calendar!!.setSelectionMode(calendar!!.SINGLE_SELECTION)
            .setToday(today)
            .setMonthRange(today, end)
            .setWeekStartDay(Calendar.MONDAY, false)
            .setCallback(this)
            .build()
        updateEvents()



    }






    fun View.addSystemWindowInsetToPadding(
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val (initialLeft, initialTop, initialRight, initialBottom) =
            listOf(paddingLeft, paddingTop, paddingRight, paddingBottom)

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            view.updatePadding(
                left = initialLeft + (if (left) insets.systemWindowInsetLeft else 0),
                top = initialTop + (if (top) insets.systemWindowInsetTop else 0),
                right = initialRight + (if (right) insets.systemWindowInsetRight else 0),
                bottom = initialBottom + (if (bottom) insets.systemWindowInsetBottom else 0)
            )

            insets
        }
    }

    fun View.addSystemWindowInsetToMargin(
        left: Boolean = false,
        top: Boolean = false,
        right: Boolean = false,
        bottom: Boolean = false
    ) {
        val (initialLeft, initialTop, initialRight, initialBottom) =
            listOf(marginLeft, marginTop, marginRight, marginBottom)

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            view.updateLayoutParams {
                (this as? ViewGroup.MarginLayoutParams)?.let {
                    updateMargins(
                        left = initialLeft + (if (left) insets.systemWindowInsetLeft else 0),
                        top = initialTop + (if (top) insets.systemWindowInsetTop else 0),
                        right = initialRight + (if (right) insets.systemWindowInsetRight else 0),
                        bottom = initialBottom + (if (bottom) insets.systemWindowInsetBottom else 0)
                    )
                }
            }

            insets
        }
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        //pass
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        no_events?.visibility = View.GONE
        val selected = EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.YYYY_MM_DD).replace("/", "-")
        Log.e("selected", selected)
        eventsView?.removeAllViews()
        if (eventsArray[selected] != null){
            Log.e("e", eventsArray[selected].toString())
            val event_item = layoutInflater.inflate(R.layout.di_event_item, null, false)
            val event_name = eventsArray[selected]?.get(0)

            val parse_teacher = eventsArray[selected]?.get(1)?.replace("\"", "")?.replace("[", "")?.replace("]", "")?.replace("}", "")?.replace("{", "")?.split(",")
            Log.e("teacher", parse_teacher.toString())
            val event_teacher = parse_teacher?.get(1)?.replace("surname:", "") + " " + parse_teacher?.get(2)?.replace("name:","") + " " + parse_teacher?.get(3)?.replace("patronymic:", "")
            val is_available = eventsArray[selected]?.get(2)
            val event_time = eventsArray[selected]?.get(4)?.split(":")?.get(0) + ":" + eventsArray[selected]?.get(4)?.split(":")?.get(1)
            val link = eventsArray[selected]?.get(3)
            if (is_available == "false" && link != ""){
                event_item.findViewById<Button>(R.id.connect_buton).isEnabled = false
            }else{
                event_item.findViewById<Button>(R.id.connect_buton).isEnabled = true
                event_item.findViewById<Button>(R.id.connect_buton).setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(browserIntent)
                }
            }
            event_item.findViewById<TextView>(R.id.event_name).text = event_name
            event_item.findViewById<TextView>(R.id.event_teacher).text = event_teacher
            event_item.findViewById<TextView>(R.id.event_time).text = "Начало в $event_time"
            eventsView?.addView(event_item)
            ObjectAnimator.ofFloat(event_item, "alpha", 0f, 1f).apply {
                duration = 200
                start()
            }
        } else {
            no_events?.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(no_events, "alpha", 0f, 1f).apply {
                duration = 200
                start()
            }
        }
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        //pass
    }
}


