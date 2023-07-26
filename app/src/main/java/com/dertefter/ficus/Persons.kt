package com.dertefter.ficus

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.*
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.io.IOException
import java.util.*


class Persons : AppCompatActivity() {
    var spinner: ProgressBar? = null
    var toolbar: MaterialCardView? = null
    var toolbarBack: MaterialToolbar? = null
    var findPersonField: EditText? = null
    var findPersonButton: Button? = null
    var personsView: LinearLayout? = null
    var tableView: LinearLayout? = null
    var anim: LinearLayout? = null
    var info_text: TextView? = null
    var week = 0
    var weeks_view: LinearLayout? = null
    var daySelection: CardView? = null
    var day: Int = 1
    var arrowLeftButton: ImageButton? = null
    var arrowRightButton: ImageButton? = null
    var days: FrameLayout? = null
    var dayView: TextView? = null
    var calendar: Calendar = Calendar.getInstance()
    var link = ""
    var personCard: MaterialCardView? = null
    var my_group_only = false
    fun arrowRight() {
        val a = ObjectAnimator.ofFloat(dayView, "translationX", 300f, 0f)
        a.duration = 140
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
        val a = ObjectAnimator.ofFloat(dayView, "translationX", -300f, 0f)
        a.duration = 140
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

    fun selectWeek(week_: Int?, card: View){
        for (c in weeks_view?.children!!){
            c.alpha = 0.7f
        }
        card.alpha = 1f
        (weeks_view?.parent as HorizontalScrollView).smoothScrollTo(card.left, 0)
        lessons(week_)
    }

    override fun onBackPressed() {
        if (toolbarBack?.visibility == View.VISIBLE){
            showSearch()
        }else{
            finish()
        }
    }

    fun lessons(week_: Int?) {
        weeks_view?.visibility = View.VISIBLE
        spinner?.visibility = View.VISIBLE
        val mInflater = LayoutInflater.from(this@Persons)
        for (i in 0..5) {
            ((days?.get(i) as FrameLayout).get(0) as LinearLayout).removeAllViews()
        }
        days?.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl(link)
            .build()
        val service = retrofit.create(APIService::class.java)


        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.Study()
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
                        val rows = s.select("> *")

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
                            val week_number = week_!!
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
                                            if (label.contains(week_.toString())){
                                                current = true
                                            }

                                        }else{
                                            current = true
                                        }


                                        val type = t.select("span.schedule__table-typework").first().ownText()
                                        val aud = t.parent().parent().select("div.schedule__table-class").text()
                                        var person = ""
                                        for (p in t.select("a")){
                                            person = person + " " + p.text()
                                        }
                                        val group = t.select("b").first().text()

                                        if (name != "" && current){
                                            val lessonCard = mInflater.inflate(R.layout.item, null, false)
                                            lessonCard.findViewById<TextView>(R.id.time).text = time
                                            lessonCard.findViewById<TextView>(R.id.type).text = type
                                            lessonCard.findViewById<TextView>(R.id.lesson).text = name
                                            lessonCard.findViewById<TextView>(R.id.aud).text = aud

                                            lessonCard.findViewById<TextView>(R.id.person).text = group
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



                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }
            } catch (e: Throwable){
                Log.e("ficus.timetable", e.toString())
            } catch (e: Exception){
                Log.e("ficus.timetable", e.toString())
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.persons_screen)
        weeks_view = findViewById(R.id.weeks)
        daySelection = findViewById(R.id.daySelection)
        arrowLeftButton = findViewById(R.id.arrowLeft)
        arrowRightButton = findViewById(R.id.arrowRight)
        dayView = findViewById(R.id.day_state)
        personCard = findViewById(R.id.person_card)
        arrowRightButton?.setOnClickListener {
            arrowRight()
        }
        arrowLeftButton?.setOnClickListener {
            arrowLeft()
        }
        days = findViewById(R.id.days)
        var today: Int = calendar.get(Calendar.DAY_OF_WEEK)
        today -= 1
        if (today < 1) {
            today = 1
        }
        day = today
        if (savedInstanceState?.get("day") != null) {
            day = savedInstanceState.get("day") as Int
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




        personsView = findViewById(R.id.persons_view)
        anim = findViewById(R.id.persons_anim)
        info_text = findViewById(R.id.persons_info)
        tableView = findViewById(R.id.person_timetable_view)
        tableView?.addSystemWindowInsetToPadding(bottom = true)
        toolbar = findViewById(R.id.toolbarPersons)
        toolbarBack = findViewById(R.id.topAppBarBack)
        toolbarBack?.setNavigationOnClickListener {
            showSearch()
        }
        toolbarBack?.addSystemWindowInsetToMargin(top = true)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        spinner = findViewById(R.id.spinner_persons)
        findPersonField = findViewById(R.id.findPersonField)
        findPersonButton = findViewById(R.id.findPersonButton)
        findPersonButton?.setOnClickListener {
            findPerson(findPersonField?.text.toString())
        }
        findPersonField?.doOnTextChanged { text, start, before, count ->
            findPersonButton?.isEnabled = text?.length!! > 2
        }
        findPersonField?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                findPerson(findPersonField?.text.toString())
                return@OnKeyListener true
            }
            false
        })

        }

    fun showSearch(){
        findPersonField?.setText("")
        toolbar?.visibility = View.VISIBLE
        personsView?.visibility = View.VISIBLE
        toolbarBack?.visibility = View.GONE
        tableView?.visibility = View.GONE
        anim?.visibility = View.VISIBLE
        info_text?.text = "Введите ФИО преподавателя"
        ObjectAnimator.ofFloat(anim, "alpha", 0f, 1f).apply {
            duration = 240
            start()
        }
    }
    fun showTimetable(link: String, name: String, img: String, site: String, mail: String){
        anim?.visibility = View.GONE
        val newLink = link.replace("joint_timetable", "lessons")
        this.link = "$newLink/"
        spinner?.visibility = View.VISIBLE
        toolbar?.visibility = View.GONE
        personsView?.visibility = View.GONE
        toolbarBack?.visibility = View.VISIBLE
        personsView?.removeAllViews()
        tableView?.visibility = View.VISIBLE

        personCard!!.findViewById<TextView>(R.id.name).text = name
        personCard!!.findViewById<TextView>(R.id.site).text = site
        personCard!!.findViewById<TextView>(R.id.mail).text = mail
        if (img != "") {
            val transformation: Transformation =
                RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(0f)
                    .cornerRadiusDp(60f)
                    .oval(false)
                    .build()
            Picasso.get().load(img).resize(200, 200).centerCrop()
               .transform(transformation).into(personCard!!.findViewById<ImageView>(R.id.profile_image))
        }else{
           personCard!!.findViewById<ImageView>(R.id.profile_image).setImageResource(R.drawable.account_circle_filled)
        }
        lessons(week)

    }

    fun findPerson(p: String){
        findPersonField?.hideSoftInput()
        anim?.visibility = View.GONE
        spinner?.visibility = View.VISIBLE
        personsView?.removeAllViews()
        val p_ = p.replace("ё", "e")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.findPerson(p_)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val result = doc.select("div.search-result")
                        val results = result.select("div.search-result__item")
                        for (i in results){
                            var profile_pic = ""
                            var site = ""
                            var mail = ""
                            var link = ""
                            val person_name = i.select("a").first().text()
                            val  mInflater = LayoutInflater.from(this@Persons)
                            var item: View =
                                mInflater!!.inflate(R.layout.item_person, null, false)
                            item.findViewById<TextView>(R.id.person_name).text = person_name
                            val tbody = i.select("tbody")
                            val tr = tbody.select("tr")

                            for (t in tr){
                                if (t.toString().contains("Текущее расписание")){
                                    link = t.select("a").first().attr("href")
                                }
                                if (t.toString().contains("Персональный сайт")){
                                    site = t.select("a").first().attr("href").toString()
                                }
                                if (t.toString().contains("Электронная почта")){
                                    val before = t.select("span").first().attr("before").toString()
                                    val after = t.select("span").first().attr("after").toString()
                                    mail = "$before@$after"
                                }

                            }

                            val img = i.select("img").first()
                            if (img != null){
                                val transformation: Transformation =
                                    RoundedTransformationBuilder()
                                        .borderColor(Color.BLACK)
                                        .borderWidthDp(0f)
                                        .cornerRadiusDp(60f)
                                        .oval(false)
                                        .build()
                                profile_pic = "https://www.nstu.ru/" + img.attr("src")
                                Picasso.get().load(profile_pic).resize(200, 200).centerCrop().transform(transformation).into(item.findViewById<ImageView>(R.id.profile_image))
                            }else{
                                item.findViewById<ImageView>(R.id.profile_image).setImageResource(R.drawable.account_circle_filled)
                            }
                            if (link != ""){
                                item.isClickable = true
                                item.setOnClickListener {
                                    showTimetable(link, person_name, profile_pic, site, mail)
                                    findPersonField?.hideSoftInput()
                                }
                                personsView?.addView(item)
                                spinner?.visibility = View.GONE
                            }

                        }
                        if (results.size == 0){
                            anim?.visibility = View.VISIBLE
                            ObjectAnimator.ofFloat(anim, "alpha", 0f, 1f).setDuration(240).start()
                            info_text?.text = "Ничего не нашлось"
                            spinner?.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Throwable){
                Log.e("person", e.stackTraceToString())
            } catch (e: Exception){
                Log.e("person", e.stackTraceToString())
            } catch (e: IOException){
                Log.e("person", e.stackTraceToString())
            }

        }
    }

    fun View.hideSoftInput() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }







    private fun View.addSystemWindowInsetToMargin(
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

