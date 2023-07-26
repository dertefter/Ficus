package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.review.ReviewManagerFactory

class Guest : AppCompatActivity() {
    var flFragment: FrameLayout? = null
    var bnav: BottomNavigationView? = null

    val transition_duration: Long = 270
    var backPressedTime: Long = 0
    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Нажмите ещё раз, чтобы выйти", Toast.LENGTH_LONG).show()
        }
        backPressedTime = System.currentTimeMillis()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_guest)
        flFragment = findViewById(R.id.flFragment)
        AppPreferences.setup(Ficus.applicationContext())

        val timetableFragment = timeTableGuest()
        val profileFragment = Profile()
        val newsFragment = News()
        bnav = findViewById(R.id.bottomNavigationView)
        bnav?.menu?.forEach {
            val view = bnav?.findViewById<View>(it.itemId)
            view?.setOnLongClickListener {
                true
            }

        }



        var update = savedInstanceState?.getBoolean("update")
        if (update == null || update == true) {
            setCurrentFragment(timetableFragment)
            setCurrentFragment(newsFragment)
            setCurrentFragment(profileFragment)
            showFragment(timetableFragment)

        }

        bnav?.setOnNavigationItemSelectedListener {
            if (bnav?.selectedItemId != it.itemId){
                blink()
            }
            when (it.itemId) {

                R.id.timetable_nav -> {
                    showFragment(timetableFragment)
                    hideFragment(newsFragment)
                    hideFragment(profileFragment)
                    true
                }
                R.id.news_nav -> {
                    showFragment(newsFragment)
                    hideFragment(timetableFragment)
                    hideFragment(profileFragment)

                    true
                }
                R.id.profile_nav -> {
                    hideFragment(newsFragment)
                    hideFragment(timetableFragment)
                    showFragment(profileFragment)

                    true
                }

                else -> false
            }
        }

        inAppReview()
    }

    private fun showFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        show(fragment)
        commit()
    }

    private fun hideFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        hide(fragment)
        commit()
    }

    private fun blink(){
        val a = ObjectAnimator.ofFloat(flFragment, "alpha", 0f, 1f)
        a.duration = transition_duration
        a.start()
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            add(R.id.flFragment, fragment)
            hide(fragment)
            commit()
        }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("update", false)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val bnav: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bnav.setOnNavigationItemSelectedListener {
            if (bnav.selectedItemId != it.itemId){
                blink()
            }
            when (it.itemId) {
                R.id.timetable_nav -> {
                    showFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))

                }
                R.id.news_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    showFragment(supportFragmentManager.fragments.get(1))
                    hideFragment(supportFragmentManager.fragments.get(2))

                }
                R.id.profile_nav -> {
                    hideFragment(supportFragmentManager.fragments.get(0))
                    hideFragment(supportFragmentManager.fragments.get(1))
                    showFragment(supportFragmentManager.fragments.get(2))

                }
            }
            true
        }


    }


}