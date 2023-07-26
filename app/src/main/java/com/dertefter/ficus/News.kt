package com.dertefter.ficus

import AppPreferences
import ImageGetter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit


class News : Fragment(R.layout.fragment_news) {
    var newsId: Int = 1
    var allScrollView: NestedScrollView? = null
    var allNews: LinearLayout? = null
    var topBar: ProgressBar? = null
    var bottomBar: ProgressBar? = null
    var appBarLayout: AppBarLayout? = null
    var fab: ExtendedFloatingActionButton? = null
    fun getNewsById(id: Int, isUp: Boolean){
        bottomBar?.visibility = View.VISIBLE
        allScrollView?.visibility = View.VISIBLE
        val mInflater = LayoutInflater.from(activity)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getNews(newsId.toString())
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main){
                        topBar?.visibility = View.GONE
                        if (!isUp) {
                            ObjectAnimator.ofFloat(fab, "translationY", 0f).start()
                        }
                        else{
                            ObjectAnimator.ofFloat(fab, "translationY", 1000f).start()
                        }
                        if (allNews?.size!! > 28 || isUp){
                            allNews?.removeAllViews()
                            allScrollView?.scrollTo(0, 0)
                            appBarLayout?.scrollTo(0, 0)

                            allNews?.removeAllViews()

                            val blinkScreen = ObjectAnimator.ofFloat(allNews, "alpha", 0f, 1f)
                            blinkScreen.duration = 750
                            blinkScreen.start()


                        }
                    }


                    val jsonObject = JSONObject(response.body()!!.string())
                    val pretty = jsonObject.getString("items")
                    val doc: Document = Jsoup.parse(pretty.replace("\n", "").replace("\t", ""))
                    val news_items = doc.body().select("a")
                    for (it in news_items){
                        val imageUrl = "https://www.nstu.ru/" + it.attr("style").toString().replace("background-image: url(", "").replace(");", "").replace("//", "/")
                        val item: View = mInflater.inflate(R.layout.item_news, null, false)
                        val title = it.select("div.main-events__item-title").text().toString()
                        val tag = it.select("div.main-events__item-tags").text().toString()
                        val date = it.select("div.main-events__item-date").text().toString()
                        item.findViewById<TextView>(R.id.title_news).text = title!!
                        item.findViewById<TextView>(R.id.tag_news).text = tag!!
                        item.findViewById<TextView>(R.id.date).text = date!!
                        withContext(Dispatchers.Main){
                            if (imageUrl != "https://www.nstu.ru/" && imageUrl != "https://www.nstu.ru//img/media/pattern_main_events.png"){
                                Log.e("ficus.news", imageUrl)
                                Picasso.get().load(imageUrl).resize(700, 500).centerCrop().into(item.findViewById<ImageView>(R.id.background_news))
                            }else{
                                item.findViewById<ImageView>(R.id.background_news).visibility = View.GONE
                            }
                        }

                        val link = "https://www.nstu.ru/" + it.attr("href")
                        item.findViewById<TextView>(R.id.news_id).text = link
                        item.setOnClickListener{
                            val inta = Intent(
                                Ficus.applicationContext(),
                                ReadNewsActivity::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            inta.putExtra("link", link)
                            inta.putExtra("tag", tag)
                            inta.putExtra("background", imageUrl)
                            Ficus.applicationContext().startActivity(inta)
                        }
                        withContext(Dispatchers.Main){
                            if (!link.contains("foto_video")){
                                allNews?.addView(item)
                                ObjectAnimator.ofFloat(item, "alpha", 0f, 1f).start()
                            }
                        }



                    }



                }
            } catch (e: Exception){
                Log.e("ficus.news", e.stackTraceToString())
            } catch (e: Throwable){
                Log.e("ficus.news", e.stackTraceToString())
            }

        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allNews = view.findViewById(R.id.allnews_page)
        appBarLayout = view.findViewById(R.id.newsAppBarLayout)
        view.findViewById<Toolbar>(R.id.toolbar_news).addSystemWindowInsetToMargin(top = true)
        fab = view.findViewById(R.id.upword_news)
        fab?.setOnClickListener {
            newsId = 1
            topBar?.visibility = View.VISIBLE
            allScrollView?.scrollTo(0, 0)
            appBarLayout?.scrollTo(0, 0)
            getNewsById(newsId, true)
        }
        topBar = view.findViewById(R.id.top_news_bar)
        allScrollView = view.findViewById(R.id.newsScrollView)
        getNewsById(newsId, true)
        allScrollView?.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            run {
                if (allScrollView?.canScrollVertically(1) == false) {
                    newsId++
                    getNewsById(newsId, false)
                }
                else if (allScrollView?.canScrollVertically(-1) == false && newsId > 1 && allNews?.size != 0) {
                    newsId = 1
                    topBar?.visibility = View.VISIBLE
                    allScrollView?.scrollTo(0, 0)
                    appBarLayout?.scrollTo(0, 0)
                    getNewsById(newsId, true)
                }
            }
        }

        
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

    fun View.addSystemWindowInsetToMargin(
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

    private fun displayHtml(html: String, v: TextView?) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, v!!)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        v?.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        v?.text = styledText
    }

}