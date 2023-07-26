package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.util.*


class timeTableGuest : Fragment(R.layout.timetable_fragment_guest) {
    var toolbar: Toolbar? = null
    var daySelection: CardView? = null
    var timeTableScrollView: NestedScrollView? = null
    var calendar: Calendar = Calendar.getInstance()
    var isSessia: Boolean = false
    var day: Int = 1
    var arrowLeftButton: ImageButton? = null
    var arrowRightButton: ImageButton? = null
    var days: FrameLayout? = null
    var dayView: TextView? = null
    var exams: FrameLayout? = null
    var spinner: ProgressBar? = null
    var week = 0
    var groupSelection: LinearLayout? = null
    var groupField: TextInputEditText? = null
    var findGroup: Button? = null
    var findGroupInfo: TextView? = null
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isSessia) {
            outState.putBoolean("sessia", true)
        }
        outState.putInt("day", day)
    }

    var link2 = "https://www.nstu.ru/studies/schedule/schedule_session/"

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
        return("$day $month 20$year года")
    }
    fun checkSessia(){
        val retrofit = Retrofit.Builder()
            .baseUrl(link2)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.timetable(AppPreferences.group)
                val pretty = response.body()?.string().toString()
                val doc: Document = Jsoup.parse(pretty)
                val s = doc.body().select("div.schedule__session-body")
                if (s.toString() != ""){
                    Snackbar.make(requireView(), "Доступно расписание сессии!", Snackbar.LENGTH_INDEFINITE).setAction("Показать") {
                        sessia()
                    }.show()
                }
            } catch (e: Exception){
                Log.e("ficus.timetable.sessia", e.toString())
            } catch (e: Throwable){
                Log.e("ficus.timetable.sessia", e.toString())
            }

        }
    }
    fun sessia() {
        weeks_view?.visibility = View.GONE
        spinner?.visibility = View.VISIBLE
        daySelection?.visibility = View.GONE
        days?.visibility = View.GONE
        exams?.visibility = View.VISIBLE
        toolbar?.title = "Расписание сессии"
        val mInflater = LayoutInflater.from(activity)
        isSessia = true
        (exams?.get(0) as LinearLayout).removeAllViews()
        val retrofit = Retrofit.Builder()
            .baseUrl(link2)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.timetable(AppPreferences.group)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        spinner?.visibility = View.INVISIBLE
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val s = doc.body().select("div.schedule__session-body")
                        val rows = s.select("> *")
                        for (i in rows) {
                            val date_ = i.select("div.schedule__session-day").first().text()
                            val date = parseDate(date_)
                            val time = i.select("div.schedule__session-time").first().text()
                            val aud = i.select("div.schedule__session-class").first().text()
                            val lesson = i.select("div.schedule__session-item").first().ownText()
                            val teacher = i.select("a").first().ownText()
                            val label_ = i.select("div[data-type=\"label\"]").first().select("div.schedule__session-label")
                            val type_ = label_.attr("data-exam")
                            val isExam: Boolean = type_ == "true"
                            val item: View
                            if (isExam){
                                item = mInflater.inflate(R.layout.item2exam, null)
                            }
                            else{
                                item = mInflater.inflate(R.layout.item2, null)
                            }
                            item.findViewById<TextView>(R.id.date).text = date
                            item.findViewById<TextView>(R.id.time).text = time
                            item.findViewById<TextView>(R.id.aud).text = aud
                            item.findViewById<TextView>(R.id.lesson).text = lesson
                            item.findViewById<TextView>(R.id.person).text = teacher
                            if (isExam){
                                item.findViewById<TextView>(R.id.type).text = "Экзамен"

                            } else {
                                item.findViewById<TextView>(R.id.type).text = "Консультация"
                            }
                            (exams?.get(0) as LinearLayout).addView(item)

                        }


                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }
            } catch (e: Exception){
                Log.e("ficus.timetable.sessia", e.toString())
            } catch (e: Throwable){
                Log.e("ficus.timetable.sessia", e.toString())
            }

        }
    }

    fun selectWeek(week_: Int?, card: View){
        for (c in weeks_view?.children!!){
            c.alpha = 0.7f
        }
        card.alpha = 1f
        (weeks_view?.parent as HorizontalScrollView).smoothScrollTo(card.left, 0)
        lessons(week_)
    }

    fun lessons(week_: Int?) {
        weeks_view?.visibility = View.VISIBLE
        spinner?.visibility = View.VISIBLE
        toolbar?.title = "Расписание занятий"
        val mInflater = LayoutInflater.from(activity)
        isSessia = false
        for (i in 0..5) {
            ((days?.get(i) as FrameLayout).get(0) as LinearLayout).removeAllViews()
        }
        days?.visibility = View.VISIBLE
        exams?.visibility = View.GONE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)


        CoroutineScope(Dispatchers.IO).launch {
            try{
                var week_value = ""
                if (week_ != null){
                    week_value = week_.toString()
                }
                val response = service.getTimetableGuest(AppPreferences.group, week_value)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        for (d_ in days?.children!!){
                            ((d_ as FrameLayout).get(0) as LinearLayout).removeAllViews()
                        }
                        spinner?.visibility = View.INVISIBLE
                        daySelection?.visibility = View.VISIBLE
                        ObjectAnimator.ofFloat(daySelection, "alpha",  1f).apply {
                            duration = 300
                            start()
                        }
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val s = doc.body().select("div.schedule__table-body").first()
                        val weekLabel = doc.body().select("div.schedule__title").select("span.schedule__title-label").text()
                        val sessiaNow = weekLabel.contains("сессия")
                        if (sessiaNow){
                            weeks_view?.visibility = View.GONE
                            daySelection?.visibility = View.GONE
                        }else{

                        }
                        val rows = s.select("> *")
                        run {
                            if (weekLabel.contains("каникулы", true)){
                                week = 1
                            }else{
                                week = weekLabel.split(" ")[0].toInt()
                            }
                            if (weeks_view?.childCount == 0){
                                for (w in week..week+6){
                                    val week_item = mInflater.inflate(R.layout.timetable_week_tab, null, false)
                                    week_item.findViewById<TextView>(R.id.week_text).text = "Неделя $w"
                                    if (w == week){
                                        week_item.findViewById<ImageView>(R.id.week_now)?.visibility = View.VISIBLE
                                        week_item?.alpha = 1f
                                    }else{
                                        week_item.findViewById<ImageView>(R.id.week_now)?.visibility = View.GONE
                                        week_item?.alpha = 0.7f
                                    }
                                    week_item.setOnClickListener {
                                        selectWeek(w, it)
                                    }
                                    weeks_view?.addView(week_item)
                                }
                            }
                            for (i in 0..rows.size - 1) {
                                val time = rows[i].select("div.schedule__table-time").text().toString()
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
                                            person = person + p.text() + "\n"
                                        }
                                        if (person != ""){
                                            person = person.substring(0, person.length - 1)
                                        }

                                        if (name != ""){
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

                                            var this_day: FrameLayout = days?.get(i) as FrameLayout
                                            (this_day.get(0) as LinearLayout).addView(lessonCard)
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

    fun arrowRight() {
        ObjectAnimator.ofFloat(days, "alpha", 0f, 1f).apply {
            duration = 140
            start()
        }
        ObjectAnimator.ofFloat(days, "translationX", 300f, 0f).apply {
            duration = 120
            start()
        }
        val a = ObjectAnimator.ofFloat(dayView, "translationX", 300f, 0f)
        a.duration = 160
        a.start()
        if (day < 6) {
            day++
            for (i in 0..5) {
                if (i == day - 1) {
                    days?.get(i)?.visibility = View.VISIBLE
                } else {
                    days?.get(i)?.visibility = View.GONE
                }
            }
            if (day == 1) {
                dayView?.text = "Понедельник"
            } else if (day == 2) {
                dayView?.text = "Вторник"
            } else if (day == 3) {
                dayView?.text = "Среда"
            } else if (day == 4) {
                dayView?.text = "Четверг"
            } else if (day == 5) {
                dayView?.text = "Пятница"
            } else if (day == 6) {
                dayView?.text = "Суббота"
            }
            if (day == 6) {
                arrowRightButton?.visibility = View.INVISIBLE
            } else {
                arrowRightButton?.visibility = View.VISIBLE
            }
            if (day == 1) {
                arrowLeftButton?.visibility = View.INVISIBLE
            } else {
                arrowLeftButton?.visibility = View.VISIBLE
            }
        }
    }

    fun arrowLeft() {
        ObjectAnimator.ofFloat(days, "alpha", 0f, 1f).apply {
            duration = 140
            start()
        }
        ObjectAnimator.ofFloat(days, "translationX", -300f, 0f).apply {
            duration = 120
            start()
        }

        val a = ObjectAnimator.ofFloat(dayView, "translationX", -300f, 0f)
        a.duration = 160
        a.start()
        if (day > 1) {
            day--
            for (i in 0..5) {
                if (i == day - 1) {
                    days?.get(i)?.visibility = View.VISIBLE
                } else {
                    days?.get(i)?.visibility = View.GONE
                }
            }
            if (day == 1) {
                dayView?.text = "Понедельник"
            } else if (day == 2) {
                dayView?.text = "Вторник"
            } else if (day == 3) {
                dayView?.text = "Среда"
            } else if (day == 4) {
                dayView?.text = "Четверг"
            } else if (day == 5) {
                dayView?.text = "Пятница"
            } else if (day == 6) {
                dayView?.text = "Суббота"
            }
            if (day == 6) {
                arrowRightButton?.visibility = View.INVISIBLE
            } else {
                arrowRightButton?.visibility = View.VISIBLE
            }
            if (day == 1) {
                arrowLeftButton?.visibility = View.INVISIBLE
            } else {
                arrowLeftButton?.visibility = View.VISIBLE
            }
        }
    }

    fun findGroup(value: String){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.findGroups(value)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        val json = JSONObject(prettyJson)
                        val items = json.getString("items")
                        if (items.toString() == ""){
                            Toast.makeText(Ficus.applicationContext(), "Группа не найдена", Toast.LENGTH_SHORT).show()
                        }else{
                            val doc = Jsoup.parse(items)
                            if (doc.select("a").size != 1){
                                Toast.makeText(Ficus.applicationContext(), "Группа не найдена", Toast.LENGTH_SHORT).show()
                            }else{
                                val group = doc.select("a").first().text()
                                Log.e("group", group)
                                AppPreferences.group = group
                                groupField?.hideSoftInput()
                                lessons(null)
                                toolbar?.visibility = View.VISIBLE
                                groupSelection?.visibility = View.GONE
                            }

                        }

                    }
                }
            }
             catch (e: Throwable){}
             catch (e: Exception){ }

        }

    }

    fun View.hideSoftInput() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    var weeks_view: LinearLayout? = null
    var ad: BannerAdView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ad = view.findViewById(R.id.ya_banner)
        ad?.setAdUnitId(getString(R.string.ad_timetable))
        ad?.setAdSize(AdSize.stickySize(600))
        val adRequest: AdRequest = AdRequest.Builder().build()
        ad?.loadAd(adRequest)
        groupSelection = view.findViewById(R.id.group_selection)
        groupField = view.findViewById(R.id.group_field)
        findGroup = view.findViewById(R.id.find_group_button)
        findGroupInfo = view.findViewById(R.id.select_group_unfo)
        groupField?.doOnTextChanged { text, start, before, count ->
            findGroup?.isEnabled = text.toString().length > 0
        }
        findGroup?.setOnClickListener {
            findGroup(groupField?.text.toString())
        }
        weeks_view = view.findViewById(R.id.weeks)
        spinner = view.findViewById(R.id.spinner_timetable)
        exams = view.findViewById(R.id.exams)
        daySelection = view.findViewById(R.id.daySelection)
        arrowLeftButton = view.findViewById(R.id.arrowLeft)
        arrowRightButton = view.findViewById(R.id.arrowRight)
        dayView = view.findViewById(R.id.day_state)
        arrowRightButton?.setOnClickListener {
            arrowRight()
        }
        arrowLeftButton?.setOnClickListener {
            arrowLeft()
        }
        days = view.findViewById(R.id.days)
        var today: Int = calendar.get(Calendar.DAY_OF_WEEK)
        today -= 1
        if (today < 1) {
            today = 1
        }
        day = today
        if (savedInstanceState?.get("day") != null) {
            day = savedInstanceState?.get("day") as Int
        }
        for (i in 0..5) {
            if (i == day - 1) {
                days?.get(i)?.visibility = View.VISIBLE
            } else {
                days?.get(i)?.visibility = View.GONE
            }
        }
        if (day == 6) {
            arrowRightButton?.visibility = View.INVISIBLE
        } else {
            arrowRightButton?.visibility = View.VISIBLE
        }
        if (day == 1) {
            arrowLeftButton?.visibility = View.INVISIBLE
        } else {
            arrowLeftButton?.visibility = View.VISIBLE
        }
        if (day == 1) {
            dayView?.text = "Понедельник"
        } else if (day == 2) {
            dayView?.text = "Вторник"
        } else if (day == 3) {
            dayView?.text = "Среда"
        } else if (day == 4) {
            dayView?.text = "Четверг"
        } else if (day == 5) {
            dayView?.text = "Пятница"
        } else if (day == 6) {
            dayView?.text = "Суббота"
        }

        timeTableScrollView = view.findViewById(R.id.timetable_scrollview)

        toolbar = view.findViewById(R.id.toolbar_shedule)
        toolbar?.addSystemWindowInsetToMargin(top = true)

        if (AppPreferences.group != null){
            toolbar?.title = AppPreferences.group
            toolbar?.visibility = View.VISIBLE
            groupSelection?.visibility = View.GONE
            if (savedInstanceState?.getBoolean("sessia") == false || savedInstanceState?.getBoolean("sessia") == null) {
                lessons(null)
            } else {
                sessia()
            }
        }else{
            groupSelection?.visibility = View.VISIBLE
            toolbar?.visibility = View.GONE

        }

        toolbar?.setOnMenuItemClickListener {
            if (it.itemId == R.id.l1) {
                lessons(null)
            }
            if (it.itemId == R.id.l2) {
                sessia()
            }
            if (it.itemId == R.id.l3) {
                val inta = Intent(
                    Ficus.applicationContext(),
                    Persons::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Ficus.applicationContext().startActivity(inta)
            }

            true
        }
        checkSessia()

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
}

