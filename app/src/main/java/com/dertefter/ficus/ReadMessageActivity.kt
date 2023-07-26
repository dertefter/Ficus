package com.dertefter.ficus

import AppPreferences
import ImageGetter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

class ReadMessageActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    var text: TextView? = null
    var title: TextView? = null
    var fab: FloatingActionButton? = null
    var scrollView: NestedScrollView? = null
    var toolbarLayout: AppBarLayout? = null
    var spinner: ProgressBar? = null
    private fun displayHtml(html: String) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, text!!)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        text?.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        text?.text = styledText
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


    private fun readThis(MessageID: String) {
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

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.readMes(MessageID)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString()
                        val doc = Jsoup.parse(pretty).select("main.page-content").first()
                        val theme = doc.select("span.message_theme").first().text()
                        title?.text = theme
                        val mes = doc.select("form").select("span")[6]
                        displayHtml(mes.toString())
                        spinner?.visibility = View.GONE

                    }
                }
            } catch (e: Exception){
                val inta = Intent(this@ReadMessageActivity, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            } catch (e: Throwable){
                val inta = Intent(this@ReadMessageActivity, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            }
        }


    }

    private fun deleteThis(MessageID: String) {
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

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/mess_teacher/ajax_del_mes/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["idmes"] = MessageID
        params["what"] = "1"
        params["type"] = "1"
        params["vid_sort"] = "1"
        params["year"] = "-1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        finish()
                    }
                }
            }  catch (e: Throwable) {
                Snackbar.make(
                    findViewById(R.id.read_message_layout),
                    "Ошибка! Попробуйте позже...",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }


    }

    override fun onBackPressed() {
        Work.recriateMes()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.read_message_layout)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        scrollView = findViewById(R.id.read_mes_scroll_view)
        spinner = findViewById(R.id.spinner_read_mes)
        spinner?.visibility = View.VISIBLE
        scrollView?.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            if (i2 <= i4) {
                ObjectAnimator.ofFloat(fab, "translationX", 2000f).start()
            } else {
                ObjectAnimator.ofFloat(fab, "translationX", 0f).start()
            }
        }
        var messageID: String = intent.getStringExtra("mesid")!!
        readThis(messageID)
        toolbar = findViewById(R.id.toolbar_read_message)
        text = findViewById(R.id.read_message_text)
        title = findViewById(R.id.read_message_title)
        fab = findViewById(R.id.delete_this)
        fab?.addSystemWindowInsetToMargin(bottom = true)
        text?.addSystemWindowInsetToMargin(bottom = true)
        toolbarLayout = findViewById(R.id.read_message_toolbar_layout)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        toolbar?.title = "Просмотр сообщения"
        toolbar?.setNavigationOnClickListener {
            Work.recriateMes()
            finish()
        }

        fab?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Вы уверены?")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Удалить") { dialog, which ->
                    deleteThis(messageID)
                }
                .show()
        }


    }
}