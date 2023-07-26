package com.dertefter.ficus

import ImageGetter
import android.animation.ObjectAnimator
import android.icu.util.Calendar
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import retrofit2.Retrofit


class Money : AppCompatActivity() {
    var spinner: ProgressBar? = null
    var toolbar: MaterialToolbar? = null
    var money_view: TextView? = null
    var years: LinearLayout? = null
    //https://ciu.nstu.ru/student_study/personal/money

    fun selectYear(y: String?, card: View){
        for (c in years?.children!!){
            c.alpha = 0.7f
        }
        card.alpha = 1f
        money_view?.text = ""
        (years?.parent as HorizontalScrollView).smoothScrollTo(card.left, 0)
        displayMoneyByYear(y)
    }

    private fun displayHtml(html: String) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, money_view!!)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        money_view?.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        money_view?.text = styledText
    }

    fun displayMoneyByYear(year: String?){
        spinner?.visibility = View.VISIBLE
        money_view?.visibility = View.GONE
        var tokenId = AppPreferences.token
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=$tokenId")
                    .build()
                chain.proceed(authorized)
            })
            .build()

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/personal/money/")
            .client(client)
            .build()
        var params: HashMap<String?, String?> = HashMap()
        if (year != null){
            params["year"] = year
        }else{
            params["year"] = Calendar.getInstance().get(Calendar.YEAR).toString()
        }
        params["month"] = "0"
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        spinner?.visibility = View.GONE
                        val pretty = response.body()?.string().toString()
                        val doc = Jsoup.parse(pretty)
                        val form = doc.select("form[action=https://ciu.nstu.ru/student_study/personal/money]").first()
                        if (years?.childCount == 0){
                            ObjectAnimator.ofFloat(years, "alpha", 0f, 1f).apply {
                                duration = 230
                                start()
                            }
                            val years_list = form.select("select[name=year] option")
                            for (y in years_list){
                                val year = y.attr("value")
                                val year_item = layoutInflater.inflate(R.layout.timetable_week_tab, null, false)
                                year_item.findViewById<TextView>(R.id.week_text).text = year
                                if (y.hasAttr("selected")){
                                    year_item.findViewById<ImageView>(R.id.week_now)?.visibility = View.VISIBLE
                                    year_item?.alpha = 1f
                                }else{
                                    year_item.findViewById<ImageView>(R.id.week_now)?.visibility = View.GONE
                                    year_item?.alpha = 0.7f
                                }
                                year_item.setOnClickListener {
                                    selectYear(year, it)
                                }
                                years?.addView(year_item)
                            }

                        }



                        displayHtml(form.toString())
                        money_view?.text = money_view?.text?.toString()!!.replace("2021","").replace("2022", "").replace("2023", "").replace("2024", "").replace("2025", "")
                        money_view?.text = money_view?.text?.toString()?.replace("Год:","")
                        money_view?.text = money_view?.text?.toString()?.replace("всеянварьфевральмартапрельмайиюньиюльавгустсентябрьоктябрьноябрьдекабрь","")
                        money_view?.text = money_view?.text?.toString()?.replace("Месяц:","")

                        //make months are bold
                        money_view?.text = money_view?.text?.toString()!!.replace("Январь","<b>Январь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Февраль","<b>Февраль</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Март","<b>Март</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Апрель","<b>Апрель</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Май","<b>Май</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Июнь","<b>Июнь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Июль","<b>Июль</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Август","<b>Август</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Сентябрь","<b>Сентябрь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Октябрь","<b>Октябрь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Ноябрь","<b>Ноябрь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replace("Декабрь","<b>Декабрь</b>", true)
                        money_view?.text = money_view?.text?.toString()!!.replaceFirst("\n","")
                        money_view?.text = money_view?.text?.toString()?.replace("\n","<br>")
                        displayHtml(money_view?.text.toString())
                        money_view?.visibility = View.VISIBLE
                        ObjectAnimator.ofFloat(money_view, "alpha", 0f, 1f).apply {
                            duration = 300
                            start()
                        }

                    }
                }
            }  catch (e: Throwable) {
                Log.e("thr", e.toString())
            }


        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.money_screen)
        toolbar = findViewById(R.id.toolbar_money)
        spinner = findViewById(R.id.spinner)
        money_view = findViewById(R.id.money_view)
        years = findViewById(R.id.years)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        displayMoneyByYear(null)

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

