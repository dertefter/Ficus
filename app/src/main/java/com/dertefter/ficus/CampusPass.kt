package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit


class CampusPass : AppCompatActivity(){

    fun createPass(calendar_date: String, interval_start: String, interval_id: String){
        pass?.visibility = View.GONE
        no_pass?.visibility = View.GONE
        set_pass?.visibility = View.GONE
        spinner?.visibility = View.VISIBLE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/campus_pass/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val map = HashMap<String?, String?>()
        map["make_reservation"] = "true"
        map["calendar_date"] = calendar_date
        map["interval_start"] = interval_start
        map["interval_id"] = interval_id
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.postForm(map)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    spinner?.visibility = View.GONE
                    readPass()
                }
            }
        }
    }

    fun cancelPass(id: String){
        spinner?.visibility = View.VISIBLE
        pass?.visibility = View.GONE
        no_pass?.visibility = View.GONE
        set_pass?.visibility = View.GONE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/campus_pass/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val map = HashMap<String?, String?>()
        map["cancel_reservation"] = "true"
        map["reservation_id"] = id
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.postForm(map)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    spinner?.visibility = View.GONE
                    readPass()
                }
            }
        }
    }

    fun sendDate(data: String){
        spinner?.visibility = View.VISIBLE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/campus_pass/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.postForm(hashMapOf("selected_date" to data))
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    spinner?.visibility = View.GONE
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.string()
                        )
                    )
                    val json = JSONObject(prettyJson)
                    val doc = Jsoup.parse(json.get("html").toString())
                    val times_ = doc.select("button")
                    for (time in times_){
                        if (!("disabled" in time.toString())){
                            val time_text = time.ownText().toString()
                            val data_interval = time.attr("data-interval")
                            val data_interval_start = time.attr("data-interval-start")
                            val time_item = layoutInflater.inflate(R.layout.timecard, null, false)
                            time_item.findViewById<TextView>(R.id.data).text = time_text
                            time_item?.setOnClickListener {
                                MaterialAlertDialogBuilder(this@CampusPass)
                                    .setTitle("Запись")
                                    .setMessage("Вы уверены, что хотите записаться на $time_text?")
                                    .setNegativeButton("Отмена") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    .setPositiveButton("Записаться") { dialog, which ->
                                        createPass(data, data_interval_start, data_interval)
                                    }
                                    .show()
                            }
                            times?.addView(time_item)
                            set_pass_info?.text = "Выберите время"
                            ObjectAnimator.ofFloat(set_pass_info, "alpha", 0f, 1f).apply {
                                duration = 300
                                start()
                            }
                        }
                    }


                }
            }
        }
    }

    fun readPass(){
        spinner?.visibility = View.VISIBLE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.readCampusPass()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    spinner?.visibility = View.GONE
                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val switch_content = doc.select("div[id=switch-content]")
                    var check = false
                    val button_cancel = switch_content.select("button[id=cancel_reservation]").first()
                    if (button_cancel != null){
                        check = true
                        val id = button_cancel.attr("data-reservation-id")
                        cancel_button?.setOnClickListener {
                            cancelPass(id)
                        }
                    }
                    if (!check){
                        no_pass?.visibility = View.VISIBLE
                        pass?.visibility = View.GONE
                        val days_container = doc.body().select("div.days")
                        val days = days_container.select("a.day-item.day-item--available")
                        for (day in days){
                            var day_item = layoutInflater.inflate(R.layout.timecard, null)
                            val data_calendar_date = day.attr("data-calendar-date").toString()
                            val text = day.ownText().toString()
                            day_item?.findViewById<TextView>(R.id.data)?.text = text
                            day_item?.setOnClickListener {
                                sendDate(data_calendar_date)
                                dates?.removeAllViews()
                                day_item?.setOnClickListener {}
                                dates?.addView(day_item)
                            }
                            dates?.addView(day_item)
                        }
                    }else{
                        no_pass?.visibility = View.GONE
                        pass?.visibility = View.VISIBLE
                        val pass_info = switch_content.select("div[style=\"font-size: 16px; font-weight: bold; color: #333732; text-align: center\"]").first()
                        val pass_info_text = pass_info.text().split(". ")
                        val taloon_string = pass_info_text[1].split(" ")
                        val date_string = pass_info_text[0].split(" ")
                        val taloon = taloon_string[1]
                        var date = date_string[3]
                        date = parseDate(date)
                        val time = date_string[5].replace(".", "")
                        Log.e("taloon", "$taloon, $date, $time")
                        pass_time?.text = time
                        pass_date?.text = date
                        pass_talon?.text = taloon
                        ObjectAnimator.ofFloat(pass, "alpha", 0f, 1f).apply {
                            duration = 200
                            start()
                        }
                        ObjectAnimator.ofFloat(passCard, "rotationX", 40f, 0f).apply {
                            duration = 200
                            start()
                        }
                        ObjectAnimator.ofFloat(passCard, "alpha", 0f, 1f).apply {
                            duration = 240
                            start()
                        }

                    }


                }
            }

        }

    }

    fun parseDate(value: String): String{
        val dateArr = value.split(".")
        val month = when(dateArr[1]){
            "01" -> "января"
            "02" -> "февраля"
            "03" -> "марта"
            "04" -> "апреля"
            "05" -> "мая"
            "06" -> "июня"
            "07" -> "июля"
            "08" -> "августа"
            "09" -> "сентября"
            "10" -> "октября"
            "11" -> "ноября"
            "12" -> "декабря"
            else -> "января"
        }
        val day = dateArr[0].toInt().toString()
        val year = dateArr[2].toString()
        return("$day $month $year года")
    }

    var dates: LinearLayout? = null
    var times: LinearLayout? = null
    var toolbar: Toolbar? = null
    var set_pass_info: TextView? = null
    var no_pass: LinearLayout? = null
    var pass: LinearLayout? = null
    var set_pass: LinearLayout? = null
    var pass_time: TextView? = null
    var pass_date: TextView? = null
    var passCard: MaterialCardView? = null
    var pass_talon: TextView? = null
    var cancel_button: Button? = null
    var set_pass_button: Button? = null
    var spinner: CircularProgressIndicator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.campus_pass)
        set_pass_info = findViewById(R.id.set_pass_info)
        toolbar = findViewById(R.id.toolbar_campus_pass)
        toolbar?.setNavigationOnClickListener { finish() }
        no_pass = findViewById(R.id.no_pass)
        pass = findViewById(R.id.pass)
        dates = findViewById(R.id.pass_dates)
        times = findViewById(R.id.pass_times)
        set_pass = findViewById(R.id.set_pass)
        spinner = findViewById(R.id.pass_spinner)
        pass_time = findViewById(R.id.pass_time)
        pass_date = findViewById(R.id.pass_date)
        passCard = findViewById(R.id.pass_card)
        pass_talon = findViewById(R.id.pass_talon)
        cancel_button = findViewById(R.id.cancel_pass_button)
        set_pass_button = findViewById(R.id.set_pass_button)
        set_pass_button?.setOnClickListener {
            pass?.visibility = View.GONE
            no_pass?.visibility = View.GONE
            set_pass?.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(set_pass, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }
        }
        readPass()


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
}

