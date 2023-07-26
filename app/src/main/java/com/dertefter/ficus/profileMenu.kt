package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
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
import java.io.File
import java.io.InputStream


class profileMenu : Fragment(R.layout.profile_menu) {
    var nameText: TextView? = null
    var wifiButton: MaterialCardView? = null
    var downloadsButton: MaterialCardView? = null
    var booksButton: MaterialCardView? = null
    var workButton: MaterialCardView? = null
    var personsButton: MaterialCardView? = null
    var profileDataButton: MaterialCardView? = null
    var diSpace: MaterialCardView? = null
    var passButton: MaterialCardView? = null
    var moneyButton: MaterialCardView? = null
    var docsButton: MaterialCardView? = null
    var tg: MaterialCardView? = null
    var hide_tg: Button? = null
    var scoreButton: MaterialCardView? = null
    var ad: BannerAdView? = null
    fun editProfile() {
        val profiledataIntent = Intent(
            Ficus.applicationContext(),
            ProfileData::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Ficus.applicationContext().startActivity(profiledataIntent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ad = view.findViewById(R.id.ya_banner)
        ad?.setAdUnitId(getString(R.string.ad_profile))
        ad?.setAdSize(AdSize.stickySize(600))
        val adRequest: AdRequest = AdRequest.Builder().build()
        ad?.loadAd(adRequest)
        hide_tg = view.findViewById(R.id.hide_tg)

        scoreButton = view.findViewById(R.id.score_button)
        scoreButton?.setOnClickListener {
            val scoreIntent = Intent(
                Ficus.applicationContext(),
                ScoreActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(scoreIntent)
        }

        docsButton = view.findViewById(R.id.docs_button)
        docsButton?.setOnClickListener {
            val docsIntent = Intent(
                Ficus.applicationContext(),
                Docs::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(docsIntent)
        }

        profileDataButton = view.findViewById(R.id.profile_data_button)
        passButton = view.findViewById(R.id.campus_pass_button)
        passButton?.setOnClickListener {
            val inta = Intent(
                Ficus.applicationContext(),
                CampusPass::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(inta)
        }

        moneyButton = view.findViewById(R.id.money_button)
        moneyButton?.setOnClickListener {
            val inta = Intent(
                Ficus.applicationContext(),
                Money::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(inta)
        }

        downloadsButton = view.findViewById(R.id.downloads_button)
        downloadsButton?.setOnClickListener {
            val downloadsIntent = Intent(
                Ficus.applicationContext(),
                Downloads::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(downloadsIntent)
        }
        booksButton = view.findViewById(R.id.books_button)
        wifiButton = view.findViewById(R.id.wifi_button)
        workButton = view.findViewById(R.id.work_button)
        diSpace = view.findViewById(R.id.dispace_button)
        diSpace?.setOnClickListener {
            if (diSpace?.alpha == 1f){
                AppPreferences.di = true
                activity?.findViewById<LinearLayout>(R.id.di)?.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(activity?.findViewById<LinearLayout>(R.id.di), "alpha", 0f, 1f).apply {
                    duration = 400
                    start()
                }
                activity?.findViewById<LinearLayout>(R.id.lk)?.visibility = View.GONE
            }else{
                Toast.makeText(Ficus.applicationContext(), "Ошибка подключения к DiSpace\nПопробуйте позже", Toast.LENGTH_SHORT).show()
            }


        }
        workButton?.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nstu.ru/career_center/offers/"))
            startActivity(browserIntent)
        }
        personsButton = view.findViewById(R.id.persons_button)
        personsButton?.setOnClickListener {
            val inta = Intent(
                Ficus.applicationContext(),
                Persons::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(inta)
        }
        tg = view.findViewById(R.id.telegram_button)
        tg?.setOnClickListener {
            val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nstumobile_dev/"))
            startActivity(browserIntent)
        }

        if (AppPreferences.show_tg == false){
            tg?.visibility = View.GONE
        }
        hide_tg?.setOnClickListener {
            tg?.visibility = View.GONE
            AppPreferences.show_tg = false
        }

        booksButton?.setOnClickListener {
            val campusIntent = Intent(
                Ficus.applicationContext(),
                Books::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(campusIntent)
        }
        profileDataButton?.setOnClickListener {
            editProfile()

        }




        wifiButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                wifiNnstu()
            }
        })
        nameText = view.findViewById(R.id.name)
        if (AppPreferences.name == null){
            val client = OkHttpClient().newBuilder()
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val original: Request = chain.request()
                    val authorized: Request = original.newBuilder()
                        .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                        .build()
                    chain.proceed(authorized)
                })
                .build()

            val url1 = "https://ciu.nstu.ru/student_study/"
            var retrofit = Retrofit.Builder()
                .baseUrl(url1)
                .client(client)
                .build()
            val service = retrofit.create(APIService::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                val response = service.Study()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try{
                            val pretty = response.body()?.string().toString()
                            val doc: Document = Jsoup.parse(pretty)

                            var fio = doc.body().select("span.fio").first()
                            Log.e("fio", fio.toString())
                            var fio_arr = fio.text().split(" ")
                            var group = fio_arr[fio_arr.size - 1]
                            var name = fio_arr[1]
                            AppPreferences.group = group
                            AppPreferences.name = name

                            nameText?.text = name
                        }  catch (e: Exception){
                            AppPreferences.name = "Профиль"
                            AppPreferences.fullName = ""
                        }


                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }
            }
        } else {
            nameText?.text = AppPreferences.name

        }

    }

    fun wifiNnstu() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://wifi.nstu.ru/"))
        startActivity(browserIntent)
    }

}