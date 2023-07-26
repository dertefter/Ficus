package com.dertefter.ficus

import AppPreferences
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
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


class ProfileData : AppCompatActivity() {
    var spinner: ProgressBar? = null
    var pEmail: EditText? = null
    var pAdress: EditText? = null
    var pTel: EditText? = null
    var pSnils: EditText? = null
    var pVK: EditText? = null
    var pTelegram: EditText? = null
    var pLeaderID: EditText? = null
    var EmailText: String = ""
    var AdressText: String = ""
    var TelText: String = ""
    var SnilsText: String = ""
    var VKText: String = ""
    var TelegramText: String = ""
    var LeaderIDText: String = ""
    var fab: ExtendedFloatingActionButton? = null
    var editBoolean: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.profile_data_layout)
        spinner = findViewById(R.id.spinner_pr)
        spinner?.visibility = View.VISIBLE
        fab = findViewById(R.id.extended_fab)
        fab?.setOnClickListener {
            if (editBoolean) {
                saveMode()
            } else {
                editMode()
            }


        }

        fab?.shrink()
        pEmail = findViewById(R.id.pEmail)
        pEmail?.doOnTextChanged { text, start, before, count -> EmailText = text.toString() }
        pAdress = findViewById(R.id.pAdress)
        pAdress?.doOnTextChanged { text, start, before, count -> AdressText = text.toString() }
        pTel = findViewById(R.id.pTel)
        pTel?.doOnTextChanged { text, start, before, count ->
            TelText = text.toString()
        }
        pSnils = findViewById(R.id.pSnils)
        pSnils?.doOnTextChanged { text, start, before, count -> SnilsText = text.toString() }
        pVK = findViewById(R.id.pVK)
        pVK?.doOnTextChanged { text, start, before, count -> VKText = text.toString() }
        pTelegram = findViewById(R.id.pTelegram)
        pTelegram?.doOnTextChanged { text, start, before, count -> TelegramText = text.toString() }
        pLeaderID = findViewById(R.id.pLeaderID)
        pLeaderID?.doOnTextChanged { text, start, before, count -> LeaderIDText = text.toString() }
        setEdit(false)

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

        val url1 = "https://ciu.nstu.ru/student_study/personal/contact_info/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url1)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.Study()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pretty = response.body()?.string().toString()
                        var doc = Jsoup.parse(pretty).select("main.page-content").first()
                        EmailText = doc.getElementsByAttributeValue("name", "n_email").attr("value")
                        AdressText = doc.getElementsByAttributeValue("name", "n_address").attr("value")
                        TelText = doc.getElementsByAttributeValue("name", "n_phone").attr("value")
                        SnilsText = doc.getElementsByAttributeValue("name", "n_snils").attr("value")
                        VKText = doc.getElementsByAttributeValue("name", "n_vk").attr("value")
                        TelegramText = doc.getElementsByAttributeValue("name", "n_tg").attr("value")
                        LeaderIDText = doc.getElementsByAttributeValue("name", "n_leader").attr("value")
                        setText()
                        fab?.extend()
                        spinner?.visibility = View.INVISIBLE

                    } else {

                        Log.e("RETROFIT_ERROR", response.code().toString())

                    }
                }
            } catch (e: Exception){
                val inta = Intent(this@ProfileData, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            } catch (e: Throwable){
                val inta = Intent(this@ProfileData, NetworkErrorActivity::class.java)
                inta.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(inta)
            }

        }


    }

    fun setEdit(b: Boolean) {
        pEmail?.isEnabled = b
        pAdress?.isEnabled = b
        pTel?.isEnabled = b
        pSnils?.isEnabled = b
        pVK?.isEnabled = b
        pTelegram?.isEnabled = b
        pLeaderID?.isEnabled = b

    }

    fun setText() {
        pEmail?.setText(EmailText)
        pAdress?.setText(AdressText)
        pTel?.setText(TelText)
        pSnils?.setText(SnilsText)
        pVK?.setText(VKText)
        pTelegram?.setText(TelegramText)
        pLeaderID?.setText(LeaderIDText)
    }

    fun editMode() {
        fab?.shrink()
        fab?.text = "Сохранить"
        fab?.icon = getDrawable(R.drawable.done)
        editBoolean = true
        setEdit(true)


    }

    fun saveMode() {
        setEdit(false)
        spinner?.visibility = View.VISIBLE
        fab?.icon = getDrawable(R.drawable.edit)
        fab?.text = "Редактировать"
        editBoolean = false
        urlEncoded()

    }


    fun urlEncoded() {
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

        // Create Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(" https://ciu.nstu.ru/student_study/personal/contact_info/")
            .client(client)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create HashMap with fields
        val params = HashMap<String?, String?>()
        params["save"] = "1"
        params["what"] = "0"
        params["n_email"] = EmailText
        params["n_address"] = AdressText
        params["n_phone"] = TelText
        params["n_snils"] = SnilsText
        params["n_vk"] = VKText
        params["n_tg"] = TelegramText
        params["n_leader"] = LeaderIDText



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Snackbar.make(
                            findViewById(R.id.pr_data_frame),
                            "Изменения сохранены!",
                            Snackbar.LENGTH_SHORT
                        ).setTextColor(getColor(R.color.md_theme_dark_inverseSurface))
                            .show()
                        spinner?.visibility = View.INVISIBLE

                    }
                }
            }  catch (e: Throwable) {
                Snackbar.make(
                    findViewById(R.id.pr_data_frame),
                    "Ошибка! Попробуйте позже...",
                    Snackbar.LENGTH_SHORT
                ).setTextColor(getColor(R.color.md_theme_dark_inverseSurface))
                    .show()
                spinner?.visibility = View.INVISIBLE
                editBoolean = true
                setEdit(true)
                fab?.text = "Сохранить"
            }


        }
    }

}