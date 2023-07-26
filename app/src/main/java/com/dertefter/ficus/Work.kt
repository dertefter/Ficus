package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import java.util.Random


class Work : AppCompatActivity(){

    init {
        instance = this
    }

    companion object {
        private var instance: Work? = null

        fun restart() {
            instance!!.recreate()
        }
        fun recriateMes(){
            instance!!.supportFragmentManager.fragments[3].onResume()
        }
    }


    var rail: NavigationRailView? = null
    var dirail: NavigationRailView? = null
    var bnav: BottomNavigationView? = null
    var dibnav: BottomNavigationView? = null
    val transition_duration: Long = 270
    var backPressedTime: Long = 0
    var selected_item: Int? = null
    var selected_item_di: Int? = null





    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Нажмите ещё раз, чтобы выйти", Toast.LENGTH_LONG).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    fun dispaceActivation(s: Bundle?){
        Log.e("S", s.toString())
        if (s == null){
            var diCourses = DiCourses()
            var diMessages = DiMessages()
            var diProfile = DiProfile()
            var diTimeTable = DiTimeTable()
            var client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val original: Request = chain.request()
                    val authorized: Request = original.newBuilder()
                        .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                        .build()
                    chain.proceed(authorized)
                }).build()

            val url1 = " https://dispace.edu.nstu.ru/user/"
            var retrofit = Retrofit.Builder()
                .baseUrl(url1)
                .client(client)
                .build()
            val service = retrofit.create(APIService::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    val response = service.authDispace("openam", "auth")
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful){
                            findViewById<MaterialCardView>(R.id.dispace_button).alpha = 1f
                            DisetCurrentFragment(diCourses)
                            DisetCurrentFragment(diTimeTable)
                            DisetCurrentFragment(diMessages)
                            DisetCurrentFragment(diProfile)
                            showFragment(diProfile)

                            dibnav?.setOnNavigationItemSelectedListener {
                                if (dibnav?.selectedItemId != it.itemId){
                                    blink()
                                }
                                if (dibnav?.visibility == View.VISIBLE){
                                    dirail?.selectedItemId = it.itemId
                                }
                                selected_item_di = it.itemId
                                when (it.itemId) {

                                    R.id.timetable_nav -> {
                                        showFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        hideFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.messages_nav -> {
                                        hideFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        showFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.courses_nav -> {
                                        hideFragment(diTimeTable)
                                        showFragment(diCourses)
                                        hideFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.profile_nav -> {
                                        hideFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        hideFragment(diMessages)
                                        showFragment(diProfile)

                                        true
                                    }
                                    else -> false
                                }
                            }
                            dibnav?.selectedItemId = R.id.profile_nav

                            dirail?.setOnItemSelectedListener {
                                if (dirail?.selectedItemId != it.itemId){
                                    blink()
                                }
                                if (dirail?.visibility == View.VISIBLE){
                                    dibnav?.selectedItemId = it.itemId
                                }
                                selected_item_di = it.itemId
                                when (it.itemId) {

                                    R.id.timetable_nav -> {
                                        showFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        hideFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.messages_nav -> {
                                        hideFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        showFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.courses_nav -> {
                                        hideFragment(diTimeTable)
                                        showFragment(diCourses)
                                        hideFragment(diMessages)
                                        hideFragment(diProfile)

                                        true
                                    }
                                    R.id.profile_nav -> {
                                        hideFragment(diTimeTable)
                                        hideFragment(diCourses)
                                        hideFragment(diMessages)
                                        showFragment(diProfile)

                                        true
                                    }
                                    else -> false
                                }
                            }
                            dirail?.selectedItemId = R.id.profile_nav
                        }
                        else{
                            Log.e("error", response.code().toString())
                        }
                    }
                } catch (e: Exception){
                    Log.e("dispace", e.stackTraceToString())

                } catch (e: Throwable){
                    Log.e("dispace", e.stackTraceToString())

                }

            }
        }else{
            dibnav?.setOnNavigationItemSelectedListener {
                if (dibnav?.selectedItemId != it.itemId){
                    blink()
                }
                if (dibnav?.visibility == View.VISIBLE){
                    dirail?.selectedItemId = it.itemId
                }
                when (it.itemId) {

                    R.id.timetable_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        showFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.messages_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        showFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.courses_nav -> {
                        showFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.profile_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        showFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    else -> false
                }
            }
            dirail?.setOnItemSelectedListener {
                if (dibnav?.selectedItemId != it.itemId){
                    blink()
                }
                if (dirail?.visibility == View.VISIBLE){
                    dibnav?.selectedItemId = it.itemId
                }
                when (it.itemId) {

                    R.id.timetable_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        showFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.messages_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        showFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.courses_nav -> {
                        showFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        hideFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    R.id.profile_nav -> {
                        hideFragment(supportFragmentManager.fragments.get(4))
                        hideFragment(supportFragmentManager.fragments.get(5))
                        hideFragment(supportFragmentManager.fragments.get(6))
                        showFragment(supportFragmentManager.fragments.get(7))

                        true
                    }
                    else -> false
                }
            }
        }

    }


    var fllkFragment: FrameLayout? = null
    var DiflFragment: FrameLayout? = null

    // Отправка данных с мобильного приложения
    fun sendDataToWearable() {
        try{
            Log.e("wearable", "try to sync")
            val dataClient = Wearable.getDataClient(Ficus.applicationContext())
            val putDataMapRequest = PutDataMapRequest.create("/ficus_login")
            val dataMap = putDataMapRequest.dataMap
            dataMap.putString("login", AppPreferences.login)
            dataMap.putString("password", AppPreferences.password)
            val randomValue = Random().nextInt()
            Log.e("random", randomValue.toString())
            dataMap.putString("random", randomValue.toString())
            val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
            val putDataTask = dataClient.putDataItem(putDataRequest)
            putDataTask.addOnCompleteListener { task ->
                Log.e("HANDHELD", "Save & prepare data to wearable -onComplete listener")
                if (task.isSuccessful && task.result != null) {
                    val dataMapItem = DataMapItem.fromDataItem(task.result!!).dataMap
                }
            }
        }catch (e: Exception){
            Log.e("wearable", e.stackTraceToString())
        }
        catch (e: com.google.android.gms.tasks.RuntimeExecutionException){
            Log.e("wearable", e.stackTraceToString())
        }
        catch (e: com.google.android.gms.common.api.ApiException){
            Log.e("wearable", e.stackTraceToString())
        }


    }


    private lateinit var dataClient: DataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataClient = Wearable.getDataClient(applicationContext)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_work)
        sendDataToWearable()





        AppPreferences.setup(Ficus.applicationContext())
        DiflFragment = findViewById(R.id.DiflFragment)
        fllkFragment = findViewById(R.id.fllkFragment)
        if (AppPreferences.di == true){
        }
        if (selected_item != null){
            Log.e("selected_item", selected_item.toString())
        }else{
            Log.e("selected_item", "null")
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        Log.e("screen", height.toString() + " " + width.toString())

        bnav = findViewById(R.id.bottomNavigationView)
        dibnav = findViewById(R.id.DibottomNavigationView)
        rail = findViewById(R.id.rail)
        dirail = findViewById(R.id.di_rail)

        Log.e("nav_rail:", rail?.selectedItemId.toString() )
        Log.e("nav_bnav:", bnav?.selectedItemId.toString() )

        if (width >= height){
            rail?.visibility = View.VISIBLE
            bnav?.visibility = View.GONE
            dirail?.visibility = View.VISIBLE
            dibnav?.visibility = View.GONE
        }else{
            rail?.visibility = View.GONE
            bnav?.visibility = View.VISIBLE
            dirail?.visibility = View.GONE
            dibnav?.visibility = View.VISIBLE
        }


        val timetableFragment = timeTable()
        val messagesFragment = Messages()
        val profileFragment = Profile()
        val newsFragment = News()
        if (savedInstanceState == null){

            setCurrentFragment(timetableFragment)
            setCurrentFragment(newsFragment)
            setCurrentFragment(messagesFragment)
            setCurrentFragment(profileFragment)
            showFragment(timetableFragment)
        }
        dispaceActivation(savedInstanceState)
        bnav?.setOnNavigationItemSelectedListener {
            if (bnav?.selectedItemId != it.itemId){
                blink()
            }
            if (bnav?.visibility == View.VISIBLE){
                rail?.selectedItemId = it.itemId
            }

            when (it.itemId) {
                R.id.timetable_nav -> {
                    showFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }
                R.id.news_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    showFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }
                R.id.messages_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    showFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }

                R.id.profile_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    showFragment(supportFragmentManager.fragments.get(3))
                }
            }
            true
        }

        rail?.setOnItemSelectedListener {
            if (rail?.visibility == View.VISIBLE){
                bnav?.selectedItemId = it.itemId
            }

            if (rail?.selectedItemId != it.itemId){
                blink()
            }
            when (it.itemId) {
                R.id.timetable_nav -> {
                    val fragment = supportFragmentManager.findFragmentByTag("SupportLifecycleFragmentImpl")
                    if (fragment != null) {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                    }
                    Log.e("fragments", supportFragmentManager.fragments.toString())
                    showFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }
                R.id.news_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    showFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }
                R.id.messages_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    showFragment(supportFragmentManager.fragments.get(2))
                    hideFragment(supportFragmentManager.fragments.get(3))
                }

                R.id.profile_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))
                    showFragment(supportFragmentManager.fragments.get(3))
                }
            }
            true
        }

        bnav?.menu?.forEach {
            val view = bnav?.findViewById<View>(it.itemId)
            view?.setOnLongClickListener {
                true
            }

        }

        dibnav?.menu?.forEach {
            val view = dibnav?.findViewById<View>(it.itemId)
            view?.setOnLongClickListener {
                true
            }

        }

        rail?.menu?.forEach {
            val view = rail?.findViewById<View>(it.itemId)
            view?.setOnLongClickListener {
                true
            }

        }

        dirail?.menu?.forEach {
            val view = dirail?.findViewById<View>(it.itemId)
            view?.setOnLongClickListener {
                true
            }

        }
        inAppReview()
    }


    private fun inAppReview() {
        AppPreferences.review = true
        val reviewManager = ReviewManagerFactory.create(this)
        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            } else {
                Log.d("Error: ", request.exception.toString())
                // There was some problem, continue regardless of the result.
            }
        }
    }


    private fun showFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        Log.e("show", fragment.toString())
        show(fragment)
        commit()
    }

    private fun hideFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        hide(fragment)
        commit()
    }

    private fun blink(){

        val a = ObjectAnimator.ofFloat(fllkFragment, "alpha", 0f, 1f)
        a.duration = transition_duration
        a.start()
        val b = ObjectAnimator.ofFloat(DiflFragment, "alpha", 0f, 1f)
        b.duration = transition_duration
        b.start()
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fllkFragment, fragment)
            hide(fragment)
            commit()
        }

    private fun DisetCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            add(R.id.DiflFragment, fragment)
            hide(fragment)
            commit()
        }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("update", false)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

}