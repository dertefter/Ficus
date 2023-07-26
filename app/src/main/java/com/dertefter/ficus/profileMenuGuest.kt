package com.dertefter.ficus

import AppPreferences
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest


class profileMenuGuest : Fragment(R.layout.profile_menu_guest) {
    var nameText: TextView? = null
    var wifiButton: MaterialCardView? = null
    var booksButton: MaterialCardView? = null
    var workButton: MaterialCardView? = null
    var personsButton: MaterialCardView? = null
    var profileDataButton: MaterialCardView? = null
    var tg: MaterialCardView? = null
    var dispace_button: MaterialCardView? = null
    var hide_tg: Button? = null
    var ad: BannerAdView? = null
    fun toLogin(){
        val intent = Intent(activity, Login::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun authAlert(){
        MaterialAlertDialogBuilder(ContextThemeWrapper(context, androidx.appcompat.R.attr.alertDialogStyle))
            .setTitle("Авторизация")
            .setMessage("Для продолжения необходимо авторизоваться в приложении под своим логином")
            .setNegativeButton("Отмена") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("Войти") { dialog, which ->
                toLogin()
            }
            .show()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hide_tg = view.findViewById(R.id.hide_tg)

        ad = view.findViewById(R.id.ya_banner)
        ad?.setAdUnitId("R-M-2110341-3")
        ad?.setAdSize(AdSize.stickySize(600))
        val adRequest: AdRequest = AdRequest.Builder().build()
        ad?.loadAd(adRequest)
        dispace_button = view.findViewById(R.id.dispace_button)
        dispace_button?.setOnClickListener {
            authAlert()
        }
        personsButton = view.findViewById(R.id.persons_button)
        personsButton?.setOnClickListener {
            val inta = Intent(
                Ficus.applicationContext(),
                Persons::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(inta)
        }
        tg = view.findViewById(R.id.telegram_button)
        tg?.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nstumobile_dev/"))
            startActivity(browserIntent)
        }

        if (AppPreferences.show_tg == false){
            tg?.visibility = View.GONE
        }
        hide_tg?.setOnClickListener {
            tg?.visibility = View.GONE
            AppPreferences.show_tg = false
        }

        profileDataButton = view.findViewById(R.id.profile_data_button)
        personsButton = view.findViewById(R.id.persons_button)
        personsButton?.setOnClickListener {
            val inta = Intent(
                Ficus.applicationContext(),
                Persons::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Ficus.applicationContext().startActivity(inta)
        }
        booksButton = view.findViewById(R.id.books_button)
        wifiButton = view.findViewById(R.id.wifi_button)
        workButton = view.findViewById(R.id.work_button)
        workButton?.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("http://om.nstu.ru/"))
            startActivity(browserIntent)
        }
        booksButton?.setOnClickListener {
            authAlert()
        }
        profileDataButton?.setOnClickListener {
            toLogin()

        }




        wifiButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                wifiNnstu()
            }
        })
        nameText = view.findViewById(R.id.name)
        if (AppPreferences.group != null){
            nameText?.text = AppPreferences.group!!.uppercase()
        }

    }

    fun wifiNnstu() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://wifi.nstu.ru/"))
        startActivity(browserIntent)
    }

}