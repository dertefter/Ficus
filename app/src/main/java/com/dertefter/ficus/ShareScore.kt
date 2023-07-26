package com.dertefter.ficus

import AppPreferences
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import retrofit2.Retrofit

class ShareScore : AppCompatActivity() {
    var shareButton: Button? = null
    var linkView: TextView? = null
    var updateButton: Button? = null
    var spinner: ProgressBar? = null
    var toolbar: Toolbar? = null
    fun updateLink(){
        linkView?.text = ""
        spinner?.visibility = View.VISIBLE
        shareButton?.isEnabled = false
        val params = HashMap<String?, String?>()
        params["generate_access_url"] = "true"
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
            .baseUrl("https://ciu.nstu.ru/student_study/student_info/link_progress/")
            .client(client)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString().replace("{\"access_url\":\"", "").replace("\"}", "").replace("\\", "")
                        linkView?.text = pretty
                        spinner?.visibility = View.INVISIBLE
                        shareButton?.isEnabled = true
                    }
                }
            }  catch (e: Throwable) {
                Snackbar.make(
                    findViewById(R.id.pr_data_frame),
                    "Ошибка! Попробуйте позже...",
                    Snackbar.LENGTH_SHORT
                ).setTextColor(getColor(R.color.md_theme_dark_inverseSurface))
                    .show()
                spinner?.visibility = View.INVISIBLE


            }


        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.share_screen)
        toolbar = findViewById(R.id.toolbar_share_score)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        shareButton = findViewById(R.id.shareScoreButton)
        linkView = findViewById(R.id.shareScoreLink)
        updateButton = findViewById(R.id.updateShareButton)
        updateButton?.setOnClickListener {
            updateLink()
        }
        spinner = findViewById(R.id.spinner_share)
        spinner?.visibility = View.VISIBLE
        shareButton?.isEnabled = false
        shareButton?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, linkView?.text)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Поделиться ссылкой"))
        }
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

        val url1 = "https://ciu.nstu.ru/student_study/student_info/link_progress/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url1)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.Study()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val pretty = response.body()?.string().toString()
                    var doc = Jsoup.parse(pretty)
                    val link = doc.body().select("textarea").text()
                    linkView?.text = link
                    spinner?.visibility = View.INVISIBLE
                    shareButton?.isEnabled = true


                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }

    }
}