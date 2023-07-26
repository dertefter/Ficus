package com.dertefter.ficus

import AppPreferences
import ImageGetter
import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.*


class DiReadMessages : AppCompatActivity() {

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            runOnUiThread {
                Toast.makeText(this, "Файл сохранен в Загрузки", Toast.LENGTH_SHORT).show()
                val inta = Intent(
                    Ficus.applicationContext(),
                    Downloads::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Ficus.applicationContext().startActivity(inta)
            }
            return pathWhereYouWantToSaveFile
        } catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }

    fun downloadFile(_file: String, _name: String){
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/proceed/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.downloadFile("get_attach", _file, _name).body()
            val filesList = File(filesDir.absolutePath).listFiles()
            if (filesList != null) {
                if ("down" !in filesList.toString()){
                    File(filesDir.absolutePath+"/down").mkdir()
                }
            }else{
                File(filesDir.absolutePath+"/down").mkdir()
            }


            var pathWhereYouWantToSaveFile = filesDir.absolutePath + "/down/" + _name
            saveFile(response, pathWhereYouWantToSaveFile)




        }

    }

    private fun displayHtml(html: String, text : TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, text)

        // Using Html framework to parse html
        var styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,null)

        // to enable image/link clicking
        text.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        styledText = noTrailingwhiteLines(styledText) as Spanned
        text.text = styledText
    }

    private fun noTrailingwhiteLines(text: CharSequence): CharSequence? {
        var text = text
        while (text[text.length - 1] == '\n') {
            text = text.subSequence(0, text.length - 1)
        }
        return text
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
    var toolbar: MaterialToolbar? = null
    var appBarLayout: AppBarLayout? = null
    var mes_view: LinearLayout? = null
    var mes_scroll_view: NestedScrollView? = null
    var textFieldFrame: LinearLayout? = null
    var read_mes_scroll_view: NestedScrollView? = null
    var spinner: ProgressBar? = null
    var top_mes: ProgressBar? = null
    var page = 1
    var remain = 0
    var send_textfield: EditText? = null
    var send_button: ImageButton? = null

    fun sendmes(mes: String, chatid: String) {
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/proceed/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "send_msgs"
        params["rec_students"] = chatid
        params["message"] = "<p>$mes</p>"
        params["text_signature"] = ""
        params["add_signature"] = "false"
        val mInflater = LayoutInflater.from(this)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getDispaceMessages(params)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val message_item = mInflater.inflate(R.layout.di_message, null)
                    val _id = ""
                    val _time = "только что"
                    val _text = "<p>$mes</p>"
                    displayHtml(_text!!, message_item.findViewById(R.id.text))
                    message_item.findViewById<TextView>(R.id.date).text = _time
                    mes_view!!.addView(message_item)
                    mes_scroll_view?.post {
                        mes_scroll_view?.smoothScrollTo(0, mes_view?.height!! + 100)
                        send_textfield?.setText("")
                    }
                    ObjectAnimator.ofFloat(message_item, "alpha", 0f, 1f).apply {
                        duration = 400
                        start()
                    }

                }
            }
        }
    }

    fun mes(page: Int, uw_id: String, to_top: Boolean) {
        val mInflater = LayoutInflater.from(this)
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()

        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/messages/")
            .client(client)
            .build()

        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "dialog"
        params["uw_id"] = uw_id
        params["page"] = page.toString()
        val top_mes_y =  mes_view?.getChildAt(0)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getDispaceMessages(params)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        spinner?.visibility = View.GONE
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        val json = JSONObject(prettyJson)
                        var messages = json.getJSONArray("data")
                        if(to_top){
                            val reversed_messages = JSONArray()
                            for (i in messages.length() - 1 downTo 0) {
                                reversed_messages.put(messages[i])
                            }
                            messages = reversed_messages

                        }
                        remain = json.getInt("remain")
                        if (remain > 0){
                            top_mes?.visibility = View.VISIBLE
                        }else{
                            top_mes?.visibility = View.GONE
                        }
                        for(i in 0 until messages.length()){
                            val sender_id = messages.getJSONObject(i).getString("sender_id")

                            if (sender_id == uw_id){
                                val message_item = mInflater.inflate(R.layout.di_message2, null)
                                val _id = messages.getJSONObject(i).getString("id")
                                val _text = messages.getJSONObject(i).getString("message").replace("/files/message_attach/", "https://dispace.edu.nstu.ru/files/message_attach/")
                                val _date = messages.getJSONObject(i).getString("date")
                                if (messages.getJSONObject(i).has("msg_files")){
                                    val _file = messages.getJSONObject(i).getString("msg_files")
                                    val _file_arr = _file.split("~=~")
                                    val _file_item = mInflater.inflate(R.layout.di_file, null)
                                    _file_item.findViewById<TextView>(R.id.file_name).text = _file_arr[1]
                                    _file_item.setOnClickListener {
                                        downloadFile(_file_arr[2], _file_arr[1])
                                    }
                                    message_item.findViewById<LinearLayout>(R.id.files).addView(_file_item)
                                    message_item.findViewById<LinearLayout>(R.id.files)?.visibility = View.VISIBLE
                                }
                                displayHtml(_text!!, message_item.findViewById(R.id.text))
                                message_item.findViewById<TextView>(R.id.date).text = _date
                                message_item.findViewById<TextView>(R.id.mesid).text = _id

                                //auto scroll down
                                if (to_top){
                                    mes_view?.addView(message_item, 0)
                                    mes_scroll_view?.post {
                                        mes_scroll_view?.smoothScrollTo(0, top_mes_y?.top!!)
                                    }

                                }else{
                                    mes_view?.addView(message_item)
                                    mes_scroll_view?.post {
                                        mes_scroll_view?.smoothScrollTo(0, mes_view?.height!! + 100)
                                    }
                                }
                                ObjectAnimator.ofFloat(message_item, "alpha", 0f, 1f).apply {
                                    duration = 400
                                    start()
                                }
                            }else{
                                val message_item = mInflater.inflate(R.layout.di_message, null)
                                val _id = messages.getJSONObject(i).getString("id")
                                val _text = messages.getJSONObject(i).getString("message").replace("/files/message_attach/", "https://dispace.edu.nstu.ru/files/message_attach/")
                                val _date = messages.getJSONObject(i).getString("date")

                                displayHtml(_text!!, message_item.findViewById(R.id.text))
                                message_item.findViewById<TextView>(R.id.date).text = _date
                                message_item.findViewById<TextView>(R.id.mesid).text = _id

                                //auto scroll down
                                if (to_top){
                                    mes_view?.addView(message_item, 0)
                                    mes_scroll_view?.post {
                                        mes_scroll_view?.smoothScrollTo(0, top_mes_y?.top!!)
                                    }


                                }else{
                                    mes_view?.addView(message_item)
                                    mes_scroll_view?.post {
                                        mes_scroll_view?.smoothScrollTo(0, mes_view?.height!! + 100)
                                    }
                                }

                            }



                        }



                    }


                }
            }  catch (e: Throwable) {
                Log.e("dimes", e.toString())
            } catch (e: IOException){
                Log.e("dimes", e.toString())
            }
        }
    }

    private fun textChecker() {
        send_button?.isEnabled = send_textfield?.text.toString().replace(" ", "") != "" && send_textfield?.text != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.di_read_messagse_layout)
        var messageID: String = intent.getStringExtra("mesid")!!
        val photo: String = intent.getStringExtra("photo")!!
        read_mes_scroll_view = findViewById(R.id.read_mes_scroll_view)
        top_mes = findViewById(R.id.top_mes)
        send_textfield = findViewById(R.id.send_message_field)
        send_button = findViewById(R.id.send_message_button)
        send_button?.isEnabled = false
        send_textfield?.doOnTextChanged { text, start, count, after -> textChecker() }
        send_button?.setOnClickListener { sendmes(send_textfield?.text.toString(), messageID) }
        appBarLayout = findViewById(R.id.read_messages_toolbar_layout)
        mes_view = findViewById(R.id.mes_view)
        spinner = findViewById(R.id.spinner_read_mes)
        mes_view?.addSystemWindowInsetToMargin(bottom = true)
        mes_scroll_view = findViewById<NestedScrollView>(R.id.read_mes_scroll_view)
        textFieldFrame = findViewById(R.id.text_input_layout)
        textFieldFrame?.addSystemWindowInsetToMargin(bottom = true)
        toolbar = findViewById(R.id.toolbar_read_messages)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        toolbar?.title = intent.getStringExtra("name")
        if (photo != ""){
            val transformation: Transformation =
                RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(0f)
                    .cornerRadiusDp(1000f)
                    .oval(false)
                    .build()
            val img = ImageView(this)
            Picasso.get()
                .load(photo)
                .centerCrop()
                .transform(transformation)
                .resize(130, 130)
                .into(img, object : Callback {
                    override fun onSuccess() {
                        toolbar?.setLogo(img.drawable)
                    }

                    override fun onError(e: java.lang.Exception?) {

                    }


                })
        }else{
            toolbar?.isTitleCentered = true
        }
        mes(page, messageID, false)
        mes_scroll_view?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (!mes_scroll_view?.canScrollVertically(-1)!!){
                if (remain > 0){
                    page++
                    mes(page, messageID, true)
                }
            }
        })



    }
}