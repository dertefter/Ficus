package com.dertefter.ficus

import AppPreferences
import ImageGetter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.io.*


class DiReadCourse : AppCompatActivity() {

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String, item: View):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
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
                Snackbar.make(item, "Файл сохранён", Snackbar.LENGTH_SHORT)
                    .setAction("в Загрузки") {
                        val downloadsIntent = Intent(
                            Ficus.applicationContext(),
                            Downloads::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        downloadsIntent.putExtra("fromDi", "yes")
                        Ficus.applicationContext().startActivity(downloadsIntent)
                    }
                    .show()
                //item.findViewById<ImageView>(R.id.im_2).visibility = View.VISIBLE
                //item.findViewById<ProgressBar>(R.id.im_0).visibility = View.GONE
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

    fun saveFileVid(body: ResponseBody?, pathWhereYouWantToSaveFile: String, item: View):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }

            return pathWhereYouWantToSaveFile
        } catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            runOnUiThread {
                val vv: VideoView = item.findViewById(R.id.video)
                val mediaController = MediaController(vv.context)
                mediaController.setAnchorView(vv)
                vv.setMediaController(mediaController)
                item.findViewById<VideoView>(R.id.video).setVideoURI(Uri.parse(pathWhereYouWantToSaveFile))
                item.findViewById<VideoView>(R.id.video).start()
            }
        }
        return ""
    }

    fun downloadFile(link: String, _name: String, item: View){
        //item.findViewById<ImageView>(R.id.im_1).visibility = View.GONE
        //item.findViewById<ProgressBar>(R.id.im_0).visibility = View.VISIBLE
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl(link)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.downloadCourseFile().body()
            val filesList = File(filesDir.absolutePath).listFiles()
            if (filesList != null) {
                if ("down" !in filesList.toString()){
                    File(filesDir.absolutePath+"/down").mkdir()
                }
            }else{
                File(filesDir.absolutePath+"/down").mkdir()
            }


            val pathWhereYouWantToSaveFile = filesDir.absolutePath + "/down/" + _name
            saveFile(response, pathWhereYouWantToSaveFile, item)



        }

    }

    fun playVideo(link: String, item: View){
        item.findViewById<ImageButton>(R.id.play).visibility = View.GONE
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl(link)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.downloadCourseFile().body()
            val filesList = File(cacheDir.absolutePath).listFiles()
            if (filesList != null) {
                if ("vid" !in filesList.toString()){
                    File(cacheDir.absolutePath+"/vid").mkdir()
                }
            }else{
                File(cacheDir.absolutePath+"/vid").mkdir()
            }


            val pathWhereYouWantToSaveFile = cacheDir.absolutePath + "/vid/" + "vid.mp4"
            saveFileVid(response, pathWhereYouWantToSaveFile, item)



        }

    }

    private fun displayHtml(html: String, text : TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, text)

        // Using Html framework to parse html
        var styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_LEGACY,
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

    fun View.hideSoftInput() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
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

    fun submitPassword(id: String, password: String){
        Log.e("i", "submitPassword: $id $password")
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/didesk/course/curspass/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val map = HashMap<String?, String?>()
        map["course_id"] = id
        map["course_pass"] = password
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.postForm(map)
            withContext(Dispatchers.Main){
                if (response.isSuccessful){
                    //hide keyboard

                    loadCourse(link_, true)
                }
            }
        }
    }

    fun loadCourse(id: String, isHome: Boolean){
        courseView?.visibility = View.GONE
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl(id)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.readCourse()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try{
                            courseView?.visibility = View.VISIBLE
                            ObjectAnimator.ofFloat(courseView, "alpha", 0f, 1f).apply {
                                duration = 400
                                start()
                            }
                            val pretty = response.body()?.string().toString()
                            val doc: Document = Jsoup.parse(pretty)
                            if (doc.select("div.access-form") != null && doc.select("div.access-form").toString() != ""){
                                lockView?.visibility = View.VISIBLE
                                hideNav()
                            }else{
                                lockView?.visibility = View.GONE
                                if (isHome){
                                    nav?.menu?.clear()
                                    showNav()
                                    val courseTitle = doc.select("h1.page-title").first()
                                    if (courseTitle != null){
                                        title?.text = courseTitle.text()
                                    }else{
                                        title?.text = "Курс"
                                    }
                                    val getMenu = doc.select("div.dd-main-menu")
                                    val menuItems = getMenu.select("a")
                                    for (i in menuItems){
                                        if (i.text() != "home") {
                                            val menuItem = nav?.menu?.add(i.text())
                                            val link = "https://dispace.edu.nstu.ru/" + i.attr("href") + "/"
                                            Log.e("link", link)
                                            menuItem?.setOnMenuItemClickListener {
                                                hideNav()
                                                loadCourse(link, false)
                                                true
                                            }
                                        }

                                    }
                                }

                                val _infoBlock = doc.select("div.info-block").first()
                                if (_infoBlock != null) {
                                    if (!infoBlock?.text.toString().contains("Оглавление")){
                                        infoBlock?.text = _infoBlock.text()
                                        infoBlock?.visibility = View.VISIBLE
                                    }else{
                                        infoBlock?.visibility = View.GONE
                                    }

                                }else{
                                    infoBlock?.visibility = View.GONE
                                }

                                val _description = doc.select("div.des-lang").first()
                                if (_description != null) {
                                    description?.text = _description.text()
                                    description?.visibility = View.VISIBLE
                                }else{
                                    description?.visibility = View.GONE
                                }
                                courseBody?.removeAllViews()
                                val body = doc.select("div.course-container").first()
                                if (body != null){
                                    val items = body.select("div.dd-infoblock-outer")
                                    for (i in items){
                                        val header = layoutInflater.inflate(R.layout.di_course_header, null)
                                        val _header = i.select("div.nav-wrapper").first()
                                        if (_header != null){
                                            header.findViewById<TextView>(R.id.header).text = _header.text()
                                        }else{
                                            header.findViewById<TextView>(R.id.header).visibility = View.GONE
                                        }
                                        val _info = i.select("div.dd-infoblock-content").first()
                                        if (_info != null && _info.text() != ""){
                                            displayHtml(_info.toString().replace("/files/didesk/", "https://dispace.edu.nstu.ru//files/didesk/"), header.findViewById<TextView>(R.id.info))
                                        }
                                        courseBody?.addView(header)
                                        val attachments = i.select("div.btn-group-part")
                                        if (attachments != null && attachments.size != 0){
                                            for (a in attachments){
                                                val fileItem = layoutInflater.inflate(R.layout.di_file2, null)
                                                if (a.select("a").first() == null || a.select("a").first().hasAttr("href") == false){
                                                    continue
                                                }
                                                var fileLink = a.select("a").first().attr("href")
                                                fileLink = "https://dispace.edu.nstu.ru/$fileLink/"
                                                val fileName = a.select("button").first().attr("data-tooltip").toString().replace("Скачать ", "").split("|")[0]
                                                fileItem.findViewById<TextView>(R.id.file_name).text = fileName
                                                fileItem.setOnClickListener {
                                                    downloadFile(fileLink, fileName, fileItem)
                                                }
                                                courseBody?.addView(fileItem)
                                            }
                                        }
                                        val video = i.select("video")
                                        Log.e("videos", video.toString())
                                        for (v in video){
                                            val videoItem = layoutInflater.inflate(R.layout.di_video, null)
                                            val playButton = videoItem.findViewById<ImageButton>(R.id.play)
                                            var link = v.select("source").first().attr("src")
                                            link = link.replace("/files/didesk/", "https://dispace.edu.nstu.ru//files/didesk/") + "/"
                                            courseBody?.addView(videoItem)
                                            playButton.setOnClickListener{
                                                playVideo(link, videoItem)
                                            }

                                        }

                                    }
                                }
                            }


                        } catch (e: Exception){
                            Log.e("loadCourseError", e.stackTraceToString())

                        }



                    }
                }
            } catch (e: Exception){
                Log.e("di_read_course", e.stackTraceToString())
            } catch (e: Throwable){
                Log.e("di_read_course", e.stackTraceToString())
            } catch (e: IOException){
                Log.e("di_read_course", e.stackTraceToString())
            }

        }
    }

    fun showNav(){
        ObjectAnimator.ofFloat(nav, "translationX", 0f).apply {
            duration = 300
            start()
        }
    }
    fun hideNav(){
        ObjectAnimator.ofFloat(nav, "translationX", -nav?.width?.toFloat()!!).apply {
            duration = 300
            start()
        }
    }

    var nav: NavigationView? = null
    var toolbar: MaterialToolbar? = null
    var toolbarLayout: AppBarLayout? = null
    var title: TextView? = null
    var infoBlock: TextView? = null
    var closeNav: ImageButton? = null
    var description: TextView? = null
    var courseView: LinearLayout? = null
    var baseLink: String = ""
    var courseBody: LinearLayout? = null
    var lockView: LinearLayout? = null
    var password: TextInputEditText? = null
    var submit: Button? = null
    var link_: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.di_read_course_layout)
        submit = findViewById(R.id.course_submit)

        password = findViewById(R.id.course_password)
        password?.doOnTextChanged { text, start, before, count ->
            submit?.isEnabled = text.toString().length > 0
        }
        lockView = findViewById(R.id.lock_view)
        lockView?.visibility = View.GONE
        courseBody = findViewById(R.id.course_body)
        courseView = findViewById(R.id.courseView)
        courseView?.addSystemWindowInsetToPadding(bottom = true)
        infoBlock = findViewById(R.id.info_block)
        title = findViewById(R.id.course_title)
        description = findViewById(R.id.description)
        closeNav = findViewById(R.id.close_nav)
        closeNav?.setOnClickListener {
            hideNav()
        }
        closeNav?.addSystemWindowInsetToMargin(top = true)
        nav = findViewById(R.id.nav_course)
        toolbar = findViewById(R.id.toolbar_read_course)
        toolbarLayout = findViewById(R.id.read_course_toolbar_layout)
        toolbar?.setNavigationOnClickListener {
            showNav()
        }
        toolbarLayout?.addSystemWindowInsetToMargin(top = true)
        val id = intent.getStringExtra("id")
        submit?.setOnClickListener {
            submitPassword(id!!, password?.text.toString())
            password?.hideSoftInput()
        }
        hideNav()
        Log.e("id",id.toString())
        val link = "https://dispace.edu.nstu.ru/didesk/course/show/$id/"
        link_ = link
        loadCourse(link, true)





    }
}