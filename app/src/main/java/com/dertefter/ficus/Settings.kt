package com.dertefter.ficus

import AppPreferences
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class Settings : AppCompatActivity() {
    override fun onBackPressed() {
        finish()
    }

    fun logOut() {
        AppPreferences.name = null
        AppPreferences.group = null
        AppPreferences.fullName = null
        AppPreferences.login = null
        AppPreferences.password = null
        AppPreferences.faculty = null
        AppPreferences.faculty_image = null
        val inta = Intent(Ficus.applicationContext(), Login::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(inta)

    }

    fun iconSet(icon: String) {
        if (icon == "nstu" && AppPreferences.nstu_icon != true) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Требуется перезапуск")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Перезапустить") { dialog, which ->
                    AppPreferences.nstu_icon = true
                    packageManager.setComponentEnabledSetting(
                        ComponentName(
                            this@Settings,
                            com.dertefter.ficus.TwoLauncherAlias::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    packageManager.setComponentEnabledSetting(
                        ComponentName(
                            this@Settings,
                            com.dertefter.ficus.OneLauncherAlias::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                .show()
        }
        if (icon == "ficus" && AppPreferences.nstu_icon != false) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Требуется перезапуск")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Перезапустить") { dialog, which ->
                    AppPreferences.nstu_icon = false
                    packageManager.setComponentEnabledSetting(
                        ComponentName(
                            this@Settings,
                            com.dertefter.ficus.OneLauncherAlias::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    packageManager.setComponentEnabledSetting(
                        ComponentName(
                            this@Settings,
                            com.dertefter.ficus.TwoLauncherAlias::class.java
                        ),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                .show()
        }
    }

    fun adaptiveTheme(isAdaptive: Boolean){
        if (isAdaptive){

            AppPreferences.monet = true
            Work.restart()
            recreate()
        }else{
            AppPreferences.monet = false
            Work.restart()
            recreate()
        }
    }

    var ficusIconButton: MaterialCardView? = null
    var nstuIconButton: MaterialCardView? = null
    var standartThemeButton: MaterialCardView? = null
    var adaptiveThemeButton: MaterialCardView? = null
    var logOutButton: MaterialCardView? = null
    var dynamicContainer: LinearLayout? = null
    var standartSelection: ImageView? = null
    var adaptiveSelection: ImageView? = null
    var versionCode: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.settings_screen)
        versionCode = findViewById(R.id.app_version)
        versionCode?.text = "Версия приложения: ${BuildConfig.VERSION_NAME}" + " (${BuildConfig.VERSION_CODE})"
        ficusIconButton = findViewById(R.id.ficus_icon_button)
        nstuIconButton = findViewById(R.id.nstu_icon_button)
        standartThemeButton = findViewById(R.id.standart_theme_button)
        adaptiveThemeButton = findViewById(R.id.adaptive_theme_button)
        standartSelection = findViewById(R.id.standart_selection)
        adaptiveSelection = findViewById(R.id.adaptive_selection)
        logOutButton = findViewById(R.id.log_out_button)
        logOutButton?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Выйти") { dialog, which ->
                    logOut()
                }
                .show()
        }
        if (AppPreferences.nstu_icon == true){
            ficusIconButton?.strokeWidth = 0
            nstuIconButton?.strokeWidth = 8
        }else{
            ficusIconButton?.strokeWidth = 8
            nstuIconButton?.strokeWidth = 0
        }

        ficusIconButton?.setOnClickListener {
            iconSet("ficus")
        }
        nstuIconButton?.setOnClickListener {
            iconSet("nstu")
        }

        if (!DynamicColors.isDynamicColorAvailable()){
            dynamicContainer?.alpha = 0.5f
        }else{
            dynamicContainer?.alpha = 1f
            standartThemeButton?.setOnClickListener {
                adaptiveTheme(false)
            }
            adaptiveThemeButton?.setOnClickListener {
                adaptiveTheme(true)
            }
        }

        if (AppPreferences.monet == true){
            standartSelection?.visibility = View.INVISIBLE
            adaptiveSelection?.visibility = View.VISIBLE
        }else{
            standartSelection?.visibility = View.VISIBLE
            adaptiveSelection?.visibility = View.INVISIBLE
        }

        /*
        materialSwitch = findViewById(R.id.monet_switch)
        iconSwitch = findViewById(R.id.icon_switch)
        if (AppPreferences.nstu_icon == true){
            iconSwitch?.isChecked = true
        }else{
            iconSwitch?.isChecked = false
        }
        iconSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPreferences.nstu_icon = isChecked
            MaterialAlertDialogBuilder(this)
                .setTitle("Требуется перезапуск")
                .setNegativeButton("Отмена") { dialog, which ->
                    iconSwitch?.isChecked = false
                    dialog.cancel()
                }
                .setPositiveButton("Перезапустить") { dialog, which ->
                    if (AppPreferences.nstu_icon == true){
                        packageManager.setComponentEnabledSetting(ComponentName(this@Settings, com.dertefter.ficus.TwoLauncherAlias::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP)
                        packageManager.setComponentEnabledSetting(ComponentName(this@Settings, com.dertefter.ficus.OneLauncherAlias::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP)
                    } else {
                        packageManager.setComponentEnabledSetting(ComponentName(this@Settings, com.dertefter.ficus.OneLauncherAlias::class.java), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP)
                        packageManager.setComponentEnabledSetting(ComponentName(this@Settings, com.dertefter.ficus.TwoLauncherAlias::class.java), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP)
                    }

                }
                .show()
        }
        if (!DynamicColors.isDynamicColorAvailable()){
            materialSwitch?.isEnabled = false
        }
        materialSwitch?.isChecked = AppPreferences.monet == true

        materialSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
            AppPreferences.monet = isChecked
            MaterialAlertDialogBuilder(this)
                .setTitle("Требуется перезапуск")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Перезапустить") { dialog, which ->
                    val intent = Intent(Ficus.applicationContext(), Auth::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    Auth.applicationContext().startActivity(intent)
                    if (Auth.applicationContext() is Activity) {
                        (Auth.applicationContext() as Activity).finish()
                    }
                    Runtime.getRuntime().exit(0)
                }
                .show()
        }
        logoutButton = findViewById(R.id.logout_button)
        logoutButton?.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setNegativeButton("Отмена") { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton("Выйти") { dialog, which ->
                    logOut()
                }
                .show()
        }

         */
    }
}