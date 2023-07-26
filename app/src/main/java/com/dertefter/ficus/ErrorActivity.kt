package com.dertefter.ficus

import AppPreferences
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ErrorActivity : AppCompatActivity() {
    var retry_button: Button? = null
    var log_out: Button? = null
    override fun onBackPressed() {
        super.onBackPressed()
    }

    fun logOut() {
        AppPreferences.name = null
        AppPreferences.group = null
        AppPreferences.fullName = null
        AppPreferences.login = null
        AppPreferences.password = null
        val inta = Intent(Ficus.applicationContext(), Login::class.java)
        startActivity(inta)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_error_activity)
        log_out = findViewById(R.id.logout_auth)
        if (AppPreferences.name == null) {
            log_out?.visibility = View.INVISIBLE
        }

        log_out?.setOnClickListener {
            logOut()
        }
        retry_button = findViewById(R.id.retry_button)
        retry_button?.setOnClickListener {
            var inta = Intent(Ficus.applicationContext(), Auth::class.java)
            startActivity(inta)
        }
    }
}