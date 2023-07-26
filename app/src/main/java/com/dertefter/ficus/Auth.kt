package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.color.DynamicColors
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException


class Auth : AppCompatActivity() {


    private fun View.blink(
        times: Int = Animation.INFINITE,
        duration: Long = 400L,
        offset: Long = 5L,
        minAlpha: Float = 0.6f,
        maxAlpha: Float = 1.0f,
        repeatMode: Int = Animation.REVERSE
    ) {
        startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
            it.duration = duration
            it.startOffset = offset
            it.repeatMode = repeatMode
            it.repeatCount = times
        })
    }

    private fun View.blink2(
        times: Int = Animation.INFINITE,
        duration: Long = 700L,
        offset: Long = 40L,
        minAlpha: Float = 1.0f,
        maxAlpha: Float = 0.0f,
        repeatMode: Int = Animation.REVERSE
    ) {
        startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
            it.duration = duration
            it.startOffset = offset
            it.repeatMode = repeatMode
            it.repeatCount = times
        })
    }

    var tokenId: String = ""
    var gr = ""

    fun getGroup() {
        val context: Context = Ficus.applicationContext()
        val inta =
            Intent(context, Work::class.java)
        startActivity(inta)
    }


    fun authFun(login: String, password: String) {
        val url1 = "https://login.nstu.ru/"
        val retrofit = Retrofit.Builder().baseUrl(url1).build()
        val service = retrofit.create(APIService::class.java)

        val jsonObjectString =
            "{\"authId\":\"eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAib3RrIjogInR2ZW9yazY0dHU5aDc5dTRtb2xoZTBrb3NkIiwgInJlYWxtIjogIm89bG9naW4sb3U9c2VydmljZXMsZGM9b3BlbmFtLGRjPWNpdSxkYz1uc3R1LGRjPXJ1IiwgInNlc3Npb25JZCI6ICJBUUlDNXdNMkxZNFNmY3dIV1l6elZqbTdlbjREYXptS2ZfQktXLTA0UGR1M0lMay4qQUFKVFNRQUNNRElBQWxOTEFCTTJNamc0T0RrM05qUXpNVFE1TXpJMk56TTUqIiB9.iQ7F98fLLFrcDlSI5kYU14d9_Dg9lKN5meoGYIdXxcA\",\"template\":\"\",\"stage\":\"JDBCExt1\",\"header\":\"Авторизация\",\"callbacks\":[{\"type\":\"NameCallback\",\"output\":[{\"name\":\"prompt\",\"value\":\"Логин:\"}],\"input\":[{\"name\":\"IDToken1\",\"value\":\"$login\"}]},{\"type\":\"PasswordCallback\",\"output\":[{\"name\":\"prompt\",\"value\":\"Пароль:\"}],\"input\":[{\"name\":\"IDToken2\",\"value\":\"$password\"}]}]}"
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
        CoroutineScope(Dispatchers.Main).launch {

            try {
                val response = service.authPart1(requestBody)
                withContext(Dispatchers.IO) {
                    if (response.isSuccessful) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string() // About this thread blocking annotation : https://github.com/square/retrofit/issues/3255
                            )
                        )

                        val Jobject = JSONObject(prettyJson)
                        tokenId = Jobject.getString("tokenId").toString()
                        AppPreferences.token = tokenId
                        var CookieString = "\"NstuSsoToken\"=\"$tokenId\""
                        val requestBody2 =
                            CookieString.toRequestBody("application/json".toMediaTypeOrNull())
                        CoroutineScope(Dispatchers.IO).launch {

                            // Do the POST request and get response
                            val response2 = service.authPart2(requestBody2)

                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {

                                    var r = response.body()?.string()
                                        ?.toString()
                                    if (r != null) {
                                        AppPreferences.login = login
                                        AppPreferences.password = password
                                        getGroup()
                                    }

                                } else {
                                    val context: Context =
                                        Ficus.applicationContext()
                                    var inta = Intent(
                                        context,
                                        Login::class.java
                                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(inta)

                                }
                            }
                        }
                    } else {
                        Log.e("a", "неверный логин или пароль")


                    }


                }
            }  catch (e: Throwable) {
                Log.e("a", "1")
                val context: Context = Ficus.applicationContext()
                var inta = Intent(
                    context,
                    NetworkErrorActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(inta)

            }  catch (e: HttpException) {
                Log.e("a", "2")
                val context: Context = Ficus.applicationContext()
                var inta = Intent(
                    context,
                    NetworkErrorActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(inta)
            }  catch (e: IOException) {
                Log.e("a", "3")
                val context: Context = Ficus.applicationContext()
                var inta = Intent(
                    context,
                    NetworkErrorActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(inta)
            }
        }


    }

    var icon: LottieAnimationView? = null
    var icon2: ImageView? = null
    var text1: TextView? = null
    var animationLayout: LinearLayout? = null

    private  fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (Math.random() * (end - start + 1)).toInt() + start
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.activity_auth)
        animationLayout = findViewById(R.id.auth_text_layout)
        text1 = findViewById(R.id.auth_text1)
        icon = findViewById(R.id.authIcon1)
        icon2 = findViewById(R.id.authIcon2)
        if (AppPreferences.nstu_icon == true){
            icon?.visibility = View.GONE
            icon2?.visibility = View.VISIBLE
        }
        AppPreferences.di = false
        ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f).setDuration(300).start()
        ObjectAnimator.ofFloat(icon, "scaleX", 0f, 1f).setDuration(200).start()
        ObjectAnimator.ofFloat(icon, "scaleY", 0f, 1f).setDuration(200).start()
        val r = rand(1, 9)
        when (r){
            1 -> text1?.text = "Подключаемся к НГТУ"
            2 -> text1?.text = "Поливаем фикус"
            3 -> text1?.text = "Подключаемся к НГТУ"
            4 -> text1?.text = "Подключаемся к НГТУ"
            5 -> text1?.text = "Подключаемся к сети"
            6 -> text1?.text = "Подключаемся к НГТУ НЭТИ"
            7 -> text1?.text = "Подключаемся к сети"
            8 -> text1?.text = "Подключаемся к сети"
            9 -> text1?.text = "Подключаемся к НГТУ НЭТИ"
        }
        animationLayout?.blink()
        AppPreferences.setup(Ficus.applicationContext())

        var intent_login = Intent(this, Login::class.java)
        var saved_login: String? = AppPreferences.login
        var saved_password: String? = AppPreferences.password
        if (saved_login != "" && saved_password != "" && saved_login != null && saved_password != null) {
            authFun(saved_login, saved_password)
        } else {
            if (AppPreferences.guest == true){
                val context: Context = Ficus.applicationContext()
                val inta =
                    Intent(context, Guest::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(inta)
            }
            else{
                startActivity(intent_login)
            }

        }

    }
}