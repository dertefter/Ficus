package com.dertefter.ficus.wearable

import AppPreferences
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.wear.ambient.AmbientModeSupport
import com.google.android.gms.wearable.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.math.log

class WearAuth : Activity() ,
    DataClient.OnDataChangedListener  {
    var tokenId: String = ""
    var hello: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wearauth)
        AppPreferences.setup(applicationContext)
        hello = findViewById(R.id.hello_layout)


        Log.e("AppPreferences", "login: ${AppPreferences.login}, password: ${AppPreferences.password}")
        if (AppPreferences.login != null && AppPreferences.password != null) {
            hello?.visibility = View.GONE
            val login = AppPreferences.login!!
            val password = AppPreferences.password!!
            authFun(login, password)
        }else{
            Log.e("wear", "start listener")
            hello?.visibility = View.VISIBLE
            Wearable.getDataClient(this).addListener(this)
        }



    }


    override fun onDataChanged(p0: DataEventBuffer) {
        for (event in p0) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                val item = event.getDataItem()
                val uri = item.getUri()
                val path = uri.getPath()
                val nodeId = item.getUri().getAuthority()
                val dataItem = DataMapItem.fromDataItem(item)
                val dataMap = (dataItem as DataMapItem).dataMap
                if (dataMap.getString("login") != null) {
                    val login = dataMap.getString("login")!!
                    val password = dataMap.getString("password")!!
                    authFun(login, password)
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
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
                                        val inta = Intent(this@WearAuth, WearTimetable::class.java)
                                        inta.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(inta)
                                    }

                                } else {
                                    Log.e("error", "error")

                                }
                            }
                        }
                    } else {
                        Log.e("a", "неверный логин или пароль")


                    }


                }
            }  catch (e: Throwable) {
                Log.e("error", "error")

            }  catch (e: HttpException) {
                Log.e("error", "error")
            }  catch (e: IOException) {
                Log.e("error", "error")
            }
        }


    }



}
