package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DiscretePathEffect
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
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
import java.util.concurrent.TimeUnit
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest

class Score : Fragment(R.layout.fragment_score) {

    fun dpToPx(dp: Int): Int {
        val scale =  Ficus.applicationContext().getResources().getDisplayMetrics().density
        return (dp * scale + 0.5f).toInt()
    }

    var scoreView: FrameLayout? = null
    var semSelection: CardView? = null
    var count: Int = 0
    var max_count: Int = 0
    var selectedSemestr: Int = 1
    var arrowLeft: ImageButton? = null
    var arrowRight: ImageButton? = null
    var semTextView: TextView? = null
    var scrollView: NestedScrollView? = null
    var spinner: ProgressBar? = null
    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun score() {

        spinner?.visibility = View.VISIBLE
        val mInflater = LayoutInflater.from(activity)
        val client = OkHttpClient().newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val url1 = "https://ciu.nstu.ru/student_study/student_info/progress/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url1)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.Study()

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main){
                        spinner?.visibility = View.GONE
                    }

                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val s = doc.body().select("main.page-content")
                    var tables = s.select("table.tdall")
                    count = 0
                    var size = tables.size
                    for (i in 1..size - 1) {
                        count++
                        max_count++
                        var semestr = LinearLayout(Ficus.applicationContext())
                        val padding = dpToPx(8)
                        semestr.setPadding(padding,padding,padding,padding)
                        semestr.orientation = LinearLayout.VERTICAL
                        semestr.visibility = View.GONE
                        semestr.dividerDrawable = ContextCompat.getDrawable(Ficus.applicationContext(), R.drawable.empty_divider_8dp)
                        semestr.showDividers = 2
                        val trs = tables[i].select("tr.last_progress")

                        for (j in trs) {
                            val item: View = mInflater.inflate(R.layout.score_item, null, false)
                            item.findViewById<TextView>(R.id.subject).text =
                                j.select("td")[1]?.ownText().toString()
                            item.findViewById<TextView>(R.id.count).text =
                                "Балл: " + j.select("td")[3]?.select("span")?.first()?.ownText()
                                    .toString()
                            item.findViewById<TextView>(R.id.ECTS).text =
                                "ECTS: " + j.select("td")[5]?.select("span")?.first()?.ownText()
                                    .toString()
                            item.findViewById<TextView>(R.id.finaly).text =
                                "Оценка: " + j.select("td")[4]?.select("span")?.first()?.ownText()
                                    .toString().replace("Зач", "Зачёт")
                            var accept = true
                            val checkString = j.select("td")[4]?.select("span")?.first()?.ownText()
                                .toString().replace("Зач", "Зачёт")
                            if (checkString != "Зачёт" && checkString != "5" && checkString != "4" && checkString != "3"){
                                accept = false
                            }
                            if (j.select("span.warning_text").text() == "Н/Я"){
                                item.findViewById<TextView>(R.id.ECTS).visibility = View.GONE
                                item.findViewById<TextView>(R.id.count).visibility = View.GONE
                                item.findViewById<TextView>(R.id.finaly).text = "Оценка" + j.select("span.warning_text").text().toString()
                            }
                            else if (j.select("span.warning_text").text() == "Н/Д"){
                                item.findViewById<TextView>(R.id.ECTS).visibility = View.GONE
                                item.findViewById<TextView>(R.id.count).visibility = View.GONE
                                item.findViewById<TextView>(R.id.finaly).text = "Оценка: Не допуск"
                            }
                            if (!accept){
                                item.findViewById<TextView>(R.id.subject).setTextColor(Color.WHITE)

                            }
                            semestr.addView(item)
                            withContext(Dispatchers.Main){
                                ObjectAnimator.ofFloat(item, "alpha", 0f, 1f).apply {
                                    duration = 300
                                    start()
                                }
                            }

                        }
                        withContext(Dispatchers.Main) {
                            scoreView?.addView(semestr)
                            semSelection?.visibility = View.VISIBLE
                            ObjectAnimator.ofFloat(semSelection, "alpha", 0f, 1f).apply {
                                duration = 300
                                start()
                            }
                        }
                    }
                    for (i in 0..count - 1) {
                        if (i == count - 1) {
                            (scoreView?.get(i) as LinearLayout).visibility = View.VISIBLE
                        } else {
                            (scoreView?.get(i) as LinearLayout).visibility = View.GONE
                        }
                    }
                    if (count == 0) {
                        arrowLeft?.visibility = View.INVISIBLE
                    } else {
                        arrowLeft?.visibility = View.VISIBLE
                    }
                    if (count == max_count) {
                        arrowRight?.visibility = View.INVISIBLE
                    } else {
                        arrowRight?.visibility = View.VISIBLE
                    }
                    semTextView?.text = (count).toString() + " семестр"


                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }

            } catch (e: Exception){
                Log.e("ficus.score", e.stackTraceToString())
            } catch (e: Throwable){
                Log.e("ficus.score", e.stackTraceToString())
            }

        }
    }

    fun arrowLeft() {
        val a = ObjectAnimator.ofFloat(semTextView, "translationX", -300f, 0f)
        a.duration = 140
        a.start()
        ObjectAnimator.ofFloat(scoreView, "alpha", 0f, 1f).apply {
            duration = 140
            start()
        }
        ObjectAnimator.ofFloat(scoreView, "translationX", -300f, 0f).apply {
            duration = 120
            start()
        }
        if (count > 1) {
            count--
        }
        for (i in 0..max_count - 1) {
            if (i == count - 1) {
                (scoreView?.get(i) as LinearLayout).visibility = View.VISIBLE
            } else {
                (scoreView?.get(i) as LinearLayout).visibility = View.GONE
            }
        }
        if (count == 1) {
            arrowLeft?.visibility = View.INVISIBLE
        } else {
            arrowLeft?.visibility = View.VISIBLE
        }
        if (count == max_count) {
            arrowRight?.visibility = View.INVISIBLE
        } else {
            arrowRight?.visibility = View.VISIBLE
        }
        semTextView?.text = (count).toString() + " семестр"
    }

    fun arrowRight() {
        val a = ObjectAnimator.ofFloat(semTextView, "translationX", 300f, 0f)
        a.duration = 140
        a.start()
        ObjectAnimator.ofFloat(scoreView, "alpha", 0f, 1f).apply {
            duration = 140
            start()
        }
        ObjectAnimator.ofFloat(scoreView, "translationX", 300f, 0f).apply {
            duration = 120
            start()
        }
        if (count < max_count) {
            count++
        }
        for (i in 0..max_count - 1) {
            if (i == count - 1) {
                (scoreView?.get(i) as LinearLayout).visibility = View.VISIBLE
            } else {
                (scoreView?.get(i) as LinearLayout).visibility = View.GONE
            }
        }
        if (count == 1) {
            arrowLeft?.visibility = View.INVISIBLE
        } else {
            arrowLeft?.visibility = View.VISIBLE
        }
        if (count == max_count) {
            arrowRight?.visibility = View.INVISIBLE
        } else {
            arrowRight?.visibility = View.VISIBLE
        }
        semTextView?.text = (count).toString() + " семестр"
    }



    var ad: BannerAdView? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ad = view.findViewById(R.id.ya_banner)
        ad?.setAdUnitId(getString(R.string.ad_score))
        ad?.setAdSize(AdSize.stickySize(400))
        val adRequest: AdRequest = AdRequest.Builder().build()
        ad?.loadAd(adRequest)
        scoreView = view.findViewById(R.id.score_view)
        semSelection = view.findViewById(R.id.scoreSelection)
        spinner = view.findViewById(R.id.spinner)
        semTextView = view.findViewById(R.id.score_state)
        scrollView = view.findViewById(R.id.nestedScrollView)
        arrowLeft = view.findViewById(R.id.arrowLeft)
        arrowRight = view.findViewById(R.id.arrowRight)
        arrowLeft?.setOnClickListener {
            arrowLeft()
        }
        arrowRight?.setOnClickListener {
            arrowRight()
        }
        score()

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