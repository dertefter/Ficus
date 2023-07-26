package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.io.File
import java.io.InputStream


class DiProfileMenu : Fragment(R.layout.di_profile_menu) {
    var nameText: TextView? = null
    var downloadsButton: MaterialCardView? = null
    var profileDataButton: MaterialCardView? = null
    var lkButton: MaterialCardView? = null

    fun editProfile() {
        val profiledataIntent = Intent(
            Ficus.applicationContext(),
            ProfileData::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Ficus.applicationContext().startActivity(profiledataIntent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadsButton = view.findViewById(R.id.downloads_button)
        downloadsButton?.setOnClickListener {
            val downloadsIntent = Intent(
                Ficus.applicationContext(),
                Downloads::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            downloadsIntent.putExtra("fromDi", "yes")
            Ficus.applicationContext().startActivity(downloadsIntent)
        }
        profileDataButton = view.findViewById(R.id.profile_data_button)
        lkButton = view.findViewById(R.id.lk_button)
        lkButton?.setOnClickListener {
            AppPreferences.di = false
            activity?.findViewById<LinearLayout>(R.id.lk)?.visibility = View.VISIBLE
            activity?.findViewById<LinearLayout>(R.id.di)?.visibility = View.GONE
            ObjectAnimator.ofFloat(activity?.findViewById<LinearLayout>(R.id.lk), "alpha", 0f, 1f).apply {
                duration = 400
                start()
            }

        }
        profileDataButton?.setOnClickListener {
            editProfile()

        }

        nameText = view.findViewById(R.id.name)
        if (AppPreferences.name == null) {
            val client = OkHttpClient().newBuilder()
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
                val response = service.Study()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try{
                            val pretty = response.body()?.string().toString()
                            //body > div.sysCaption > div:nth-child(5) > div:nth-child(6)
                            val doc: Document = Jsoup.parse(pretty)
                            var s = doc.body().select("div.sysCaption").select("div:nth-child(5)").select("div:nth-child(6)").text().toString()
                            val sarr = s.split(" ").toTypedArray()
                            AppPreferences.fullName = sarr[0] + " " + sarr[1] + " " + sarr[2]
                            s = sarr[1]
                            nameText?.text = s
                            AppPreferences.name = s
                        }  catch (e: Exception){
                            AppPreferences.name = "Профиль"
                            AppPreferences.fullName = ""
                            profileDataButton?.visibility = View.GONE
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