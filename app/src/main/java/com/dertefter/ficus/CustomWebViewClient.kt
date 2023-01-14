package com.dertefter.ficus

import android.net.http.SslError
import android.util.Log
import android.view.View
import android.webkit.*
import okhttp3.*
import java.io.ByteArrayInputStream
import java.io.IOException

class CustomWebViewClient : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()

        val okRequest: Request = Request.Builder().url(request!!.url.toString()).addHeader("Access-Control-Allow-Origin","dispace.edu.nstu.ru")
            .addHeader("Access-Control-Allow-Credentials", "true")
            .addHeader("ContentType", "application/x-www-form-urlencoded")
            .addHeader("Access-Control-Allow-Headers", "accept, authorization, Content-Type").build()
        var result: WebResourceResponse? = null
        try{
            val response = retrofit2.Response.success(client.newCall(okRequest).execute().body!!)
            val result = WebResourceResponse("", "", ByteArrayInputStream(response.body()!!.bytes()))
            return result

        }catch (e: IOException){
            Log.e("CustomWebViewClient", e.toString())
            return result
        }catch (e: Exception){
            Log.e("CustomWebViewClient", e.toString())
            return result
        }

    }

    override fun onPageFinished(view: WebView, url: String) {
        // hide element by class name
        view.loadUrl("javascript:(function() { " +
                "document.getElementsByClassName('main-header')[0].style.display='none'; })()");
        // hide element by id
        view.loadUrl("javascript:(function() { " +
                "document.getElementById('your_id').style.display='none';})()");
        view.visibility = View.VISIBLE

    }
}