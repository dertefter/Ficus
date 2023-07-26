package com.dertefter.ficus

import ImageGetter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit

class ReadNewsActivity : AppCompatActivity() {
    var toolbar: MaterialToolbar? = null
    var spinner: ProgressBar? = null
    var appbar: AppBarLayout? = null
    var text: TextView? = null
    var tag: TextView? = null
    var title = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.read_news_layout)
        appbar = findViewById(R.id.appbar)
        text = findViewById(R.id.read_news_text)
        tag = findViewById(R.id.tag_news)
        spinner = findViewById(R.id.spinner_read_news)
        toolbar = findViewById(R.id.toolbar_read_news)
        appbar?.addSystemWindowInsetToPadding(top = true)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        val tag_: String = intent.getStringExtra("tag").toString()
        tag?.text = tag_
        //set different title on toolbar when collapsing
        val collapsingToolbarLayout = (toolbar?.parent as CollapsingToolbarLayout)
        collapsingToolbarLayout.title = " "
        appbar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                //  Collapsed
                collapsingToolbarLayout.title = tag_
                ObjectAnimator.ofFloat(tag?.parent, "alpha", 0f).start()
            } else {
                //Expanded
                collapsingToolbarLayout.title = title
                ObjectAnimator.ofFloat(tag?.parent, "alpha", 1f).start()
            }
        })
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val link_id: String = intent.getStringExtra("link").toString()
            .replace("https://www.nstu.ru//", "")
            .replace("/news_more?idnews=", "")
            .replace("news", "")
            .replace("announcements", "")
            .replace("interviews", "")
        Log.e("id", link_id)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.readNews(link_id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val s = doc.body().select("div.row")[1]
                        val t = doc.body().select("div.page-title").first().text().toString()
                        title = t
                        (toolbar?.parent as CollapsingToolbarLayout).title = title
                        displayHtml(s.toString())

                        spinner?.visibility = View.GONE
                    }
                }
            } catch (e: Exception){
                val inta = Intent(this@ReadNewsActivity, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            } catch (e: Throwable){
                val inta = Intent(this@ReadNewsActivity, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            }

        }
    }




    private fun displayHtml(html: String) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, text!!)

        // Using Html framework to parse html
        val styledText=HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        text?.movementMethod = getInstance()

        // setting the text after formatting html and downloading and setting images
        text?.text = styledText
    }

    private fun View.addSystemWindowInsetToPadding(
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
}