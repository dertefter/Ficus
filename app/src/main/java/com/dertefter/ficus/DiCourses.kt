package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import retrofit2.Retrofit


class DiCourses : Fragment(R.layout.di_courses_fragment) {
    var search_toolbar: MaterialCardView? = null
    var toolbar: Toolbar? = null
    var spinner: ProgressBar? = null
    var coursesView: LinearLayout? = null
    var search_field: EditText? = null
    var searchButton: Button? = null
    var favCoursesView: LinearLayout? = null
    var courses_Scroll_view: NestedScrollView? = null
    var searchAppBarLayout: AppBarLayout? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        search_toolbar = view.findViewById(R.id.search_toolbar)
        toolbar = view.findViewById(R.id.toolbar_courses)
        courses_Scroll_view = view.findViewById(R.id.courses_Scroll_view)
        searchAppBarLayout = view.findViewById(R.id.searchAppBarLayout)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        coursesView = view.findViewById(R.id.courses_view)
        favCoursesView = view.findViewById(R.id.fav_courses_view)
        toolbar?.setOnMenuItemClickListener{
        when(it.itemId){
                R.id.search -> {
                    search_toolbar?.visibility = View.VISIBLE
                    favCoursesView?.visibility = View.GONE
                    coursesView?.visibility = View.VISIBLE
                    toolbar?.menu?.findItem(R.id.search)?.isVisible = false
                    toolbar?.menu?.findItem(R.id.fav)?.isVisible = true
                    toolbar?.title = "Поиск курсов"
                    ObjectAnimator.ofFloat(courses_Scroll_view, "alpha", 0f, 1f)?.setDuration(250)?.start()
                    ObjectAnimator.ofFloat(searchAppBarLayout, "alpha", 0f, 1f)?.setDuration(180)?.start()
                    true
                }
                R.id.fav ->{
                    toolbar?.title = "Избранное"
                    search_toolbar?.visibility = View.GONE
                    favCoursesView?.visibility = View.VISIBLE
                    coursesView?.visibility = View.GONE
                    toolbar?.menu?.findItem(R.id.search)?.isVisible = true
                    toolbar?.menu?.findItem(R.id.fav)?.isVisible = false
                    ObjectAnimator.ofFloat(searchAppBarLayout, "alpha", 0f, 1f)?.setDuration(180)?.start()
                    ObjectAnimator.ofFloat(courses_Scroll_view, "alpha", 0f, 1f)?.setDuration(250)?.start()
                    true
                }
                else -> false
            }
        }
        search("", "1")
        favUpdate("1")

        search_field = view.findViewById(R.id.findCourseField)
        searchButton = view.findViewById(R.id.findCourseButton)
        search_field?.doOnTextChanged() { text, start, before, count ->
            searchButton?.isEnabled = search_field?.text.toString().length > 2
        }
        searchButton?.setOnClickListener {
            search(search_field?.text.toString(), "1")
            search_field?.hideSoftInput()
        }


    }

    fun View.hideSoftInput() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun addFav(id: String){
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/didesk/course/proceed/")
            .client(client)
            .build()
        val params = HashMap<String?, String?>()
        params["action"] = "add_favourites"
        params["course_id"] = id
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getCourses(params)
            withContext(Dispatchers.Main) {
                if(response.isSuccessful){
                    favUpdate("1")
                    Toast.makeText(context, "Курс добавлен в избранное", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun removeFav(id: String){
        // save id t
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/didesk/course/proceed/")
            .client(client)
            .build()
        val params = HashMap<String?, String?>()
        params["action"] = "delete_favourites"
        params["course_id"] = id
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getCourses(params)
            withContext(Dispatchers.Main) {
                if(response.isSuccessful){
                    favUpdate("1")
                    Toast.makeText(context, "Курс удалён из избранных", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }


    fun search(name: String, page: String){
        coursesView?.removeAllViews()
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/didesk/index/")
            .client(client)
            .build()
        val params = HashMap<String?, String?>()
        params["faculty_id"] = "-1"
        params["kafedra_id"] = "-1"
        params["year_created"] = "-1"
        params["name"] = name
        params["author_name"] = name
        params["page"] = page
        params["search_by_author_or_course"] = "OR"
        params["is_ajax"] = "false"
        params["action"] = "list"
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getCourses(params)
                if (response.isSuccessful) {
                    try{
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        val json = JSONObject(prettyJson)
                        val courses = json.getJSONArray("courses")
                        for (i in 0 until courses.length()){
                            val name = courses.getJSONObject(i).getString("name")
                            val id = courses.getJSONObject(i).getString("id")
                            var faculty = ""
                            if (courses.getJSONObject(i).has("faculty")){
                                faculty = courses.getJSONObject(i).getString("faculty")
                            }
                            val colleague = courses.getJSONObject(i).getString("colleague")
                            val infav = courses.getJSONObject(i).getInt("infav").toString()
                            withContext(Dispatchers.Main){
                                val item = layoutInflater.inflate(R.layout.di_course_item, null)
                                if (infav == "1"){
                                    item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav_filled)
                                    item.findViewById<TextView>(R.id.infav).text = "1"
                                }else{
                                    item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav)
                                    item.findViewById<TextView>(R.id.infav).text = "0"
                                }
                                item.findViewById<ImageButton>(R.id.fav_button).setOnClickListener {
                                    if (item.findViewById<TextView>(R.id.infav).text == "0"){
                                        addFav(id)
                                        item.findViewById<TextView>(R.id.infav).text = "1"
                                        item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav_filled)
                                    }else{
                                        removeFav(id)
                                        item.findViewById<TextView>(R.id.infav).text = "0"
                                        item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav)
                                    }
                                }
                                item.findViewById<TextView>(R.id.name).text = name
                                item.findViewById<TextView>(R.id.colleague).text = colleague.replace(",", "\n")
                                item.findViewById<TextView>(R.id.id).text = id
                                item.findViewById<TextView>(R.id.faculty).text = faculty
                                item.setOnClickListener{
                                    val inta = Intent(
                                        Ficus.applicationContext(),
                                        DiReadCourse::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    inta.putExtra("id", id)
                                    Ficus.applicationContext().startActivity(inta)
                                }

                                coursesView?.addView(item)
                            }


                        }
                    } catch (e: Throwable){
                        Log.e("Ficus", e.toString())
                    }


                }
            } catch (e: Exception){
                Log.e("dicouses", e.stackTraceToString())
            } catch (e: Throwable){
                Log.e("dicouses", e.stackTraceToString())
            }

        }


    }

    fun favUpdate(page: String){
        favCoursesView?.removeAllViews()
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/didesk/index/")
            .client(client)
            .build()
        val params = HashMap<String?, String?>()
        params["faculty_id"] = "favorites"
        params["kafedra_id"] = "-1"
        params["year_created"] = "-1"
        params["name"] = ""
        params["author_name"] = ""
        params["page"] = page
        params["search_by_author_or_course"] = "OR"
        params["is_ajax"] = "true"
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getCourses(params)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try{
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            val prettyJson = gson.toJson(
                                JsonParser.parseString(
                                    response.body()
                                        ?.string()
                                )
                            )
                            val json = JSONObject(prettyJson)
                            val courses = json.getJSONArray("courses")
                            for (i in 0 until courses.length()){
                                val name = courses.getJSONObject(i).getString("name")
                                val id = courses.getJSONObject(i).getString("id")
                                var faculty = ""
                                if (courses.getJSONObject(i).has("faculty")){
                                    faculty = courses.getJSONObject(i).getString("faculty")
                                }
                                val colleague = courses.getJSONObject(i).getString("colleague")
                                val infav = courses.getJSONObject(i).getInt("infav").toString()
                                val item = layoutInflater.inflate(R.layout.di_course_item, null)
                                if (infav == "1"){
                                    item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav_filled)
                                    item.findViewById<TextView>(R.id.infav).text = "1"
                                }else{
                                    item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav)
                                    item.findViewById<TextView>(R.id.infav).text = "0"
                                }
                                item.findViewById<ImageButton>(R.id.fav_button).setOnClickListener {
                                    if (item.findViewById<TextView>(R.id.infav).text == "0"){
                                        addFav(id)
                                        item.findViewById<TextView>(R.id.infav).text = "1"
                                        item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav_filled)
                                    }else{
                                        removeFav(id)
                                        item.findViewById<TextView>(R.id.infav).text = "0"
                                        item.findViewById<ImageButton>(R.id.fav_button).setImageResource(R.drawable.fav)
                                    }
                                }
                                item.findViewById<TextView>(R.id.name).text = name
                                item.findViewById<TextView>(R.id.colleague).text = colleague
                                item.findViewById<TextView>(R.id.id).text = id
                                item.findViewById<TextView>(R.id.faculty).text = faculty
                                item.setOnClickListener{
                                    val inta = Intent(
                                        Ficus.applicationContext(),
                                        DiReadCourse::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    inta.putExtra("id", id)
                                    Ficus.applicationContext().startActivity(inta)
                                }

                                favCoursesView?.addView(item)

                            }
                        } catch (e: Throwable){
                            Log.e("Ficus", e.toString())
                        }


                    }
                }
            } catch (e: Exception){
                Log.e("dicouses", e.toString())
            } catch (e: Throwable){
                Log.e("dicouses", e.toString())
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
}


