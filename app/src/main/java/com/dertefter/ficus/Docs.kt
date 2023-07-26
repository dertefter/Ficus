package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import org.w3c.dom.Text
import retrofit2.Retrofit

class Docs : AppCompatActivity() {

    var spinner: ProgressBar? = null
    var toolbar: Toolbar? = null
    var docSelection: MaterialAutoCompleteTextView? = null
    var additionalInfo:TextInputEditText? = null
    var additionInfoLayout: TextInputLayout? = null
    var chaim: Button? = null
    var chaim_cancel: Button? = null
    var documents: LinearLayout? = null
    var selectedValue = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        setContentView(R.layout.docs_screen)
        docSelection = findViewById(R.id.docSelection)
        additionalInfo = findViewById(R.id.additionalInfo)
        additionInfoLayout = findViewById(R.id.additionInfoLayout)
        chaim = findViewById(R.id.chaim)
        chaim_cancel = findViewById(R.id.chaim_cancel)
        chaim_cancel?.setOnClickListener {
            selectedValue = ""
            docSelection?.setText("")
            additionalInfo?.setText("")
            additionalInfo?.hint = ""
            ObjectAnimator.ofFloat(additionInfoLayout, "alpha", 0f).apply {
                duration = 300
                start()
                doOnEnd {
                    additionInfoLayout?.visibility = View.GONE

                }
            }

        }
        documents = findViewById(R.id.documents)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        spinner = findViewById(R.id.spinner)
        spinner?.visibility = View.VISIBLE

        getDocsInfo()

    }

    fun removeDoc(id: String){
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
            .baseUrl(" https://ciu.nstu.ru/student_study/docs/claims/edit_claim/")
            .client(client)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create HashMap with fields
        val params = HashMap<String?, String?>()
        params["act"] = "2"
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.docRemove(id, params)
                if (response.isSuccessful){
                    withContext(Dispatchers.Main){
                        updateDocs()
                    }
                }
            } catch (e: Throwable){
                withContext(Dispatchers.Main){
                    Toast.makeText(Ficus.applicationContext(), "Произошла ошибка, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun checkRemovable(v: View, id:String){
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()

        val url = "https://ciu.nstu.ru/student_study/docs/claims/edit_claim/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.docRemovable(id)
            if (response.isSuccessful){
                val pretty = response.body()?.string().toString()
                var doc = Jsoup.parse(pretty)
                val body = doc.body().toString()
                if (body.contains("Удалить заявку")){
                    withContext(Dispatchers.Main){
                        v.visibility = View.VISIBLE
                        ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).apply {
                            duration = 300
                            start()
                        }
                    }
                }
            }
        }

    }

    fun updateDocs(){
        additionInfoLayout?.visibility = View.GONE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()

        val url = "https://ciu.nstu.ru/student_study/docs/claims/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.Study()
            if (response.isSuccessful) {
                withContext(Dispatchers.Main){
                    documents?.removeAllViews()
                }
                val pretty = response.body()?.string().toString()
                var doc = Jsoup.parse(pretty)
                val table = doc.body().select("table#tutor-messages").first().select("tbody").first()
                val rows = table.select("tr")
                for (row in rows){
                    val date = row.select("td#cell-date").first().ownText()
                    val status = row.select("td#cell-1-mess").first().ownText()
                    val type = row.select("td#cell-1").first().text().toString()
                    val id = row.select("td#cell-1").first().select("a").first().attr("href").toString().split("=")[1]
                    val person = row.select("td")[3].text().toString().replace(",", "\n")
                    withContext(Dispatchers.Main){
                        val item = layoutInflater.inflate(R.layout.item_doc, null)
                        item.findViewById<TextView>(R.id.date).text = date
                        item.findViewById<TextView>(R.id.person).text = person
                        if (person == ""){
                            item.findViewById<LinearLayout>(R.id.person_layer).visibility = View.GONE
                        }
                        item.findViewById<TextView>(R.id.status).text = status
                        item.findViewById<TextView>(R.id.type).text = type
                        val button = item.findViewById<Button>(R.id.remove)
                        button.setOnClickListener {
                            removeDoc(id)

                        }
                        checkRemovable(button, id)
                        if (status.contains("Готово") != true){
                            item.findViewById<ImageView>(R.id.status_icon).setImageResource(R.drawable.schedule)
                        }
                        documents?.addView(item)
                        ObjectAnimator.ofFloat(item, "alpha", 0f, 1f).apply {
                            duration = 300
                            start()
                        }

                    }
                    Log.e("doc", "$date $status $type $id $person")
                }
            }
        }

    }

    fun sendClaim(type_claim: String, comment: String){
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
            .baseUrl("https://ciu.nstu.ru/student_study/docs/claims/")
            .client(client)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["what"] = "1"
        params["send"] = "1"
        params["type_claim"] = type_claim
        params["file_pay"] = ""
        params["file_zayav"] = ""
        params["comment"] = comment
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.postForm(params)
            if (response.isSuccessful){
                withContext(Dispatchers.Main){
                    Toast.makeText(Ficus.applicationContext(), "Заявка отправлена", Toast.LENGTH_SHORT).show()
                    updateDocs()
                    chaim_cancel?.performClick()
                }
            }else{
                withContext(Dispatchers.Main){
                    Toast.makeText(Ficus.applicationContext(), "Произошла ошибка, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        }





    }

    fun getAdditionalInfo(value: String){
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
            .baseUrl("https://ciu.nstu.ru/student_study/docs/claims/ajax_claims/")
            .client(client)
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)

        // Create HashMap with fields
        val params = HashMap<String?, String?>()
        params["ajax"] = "1"
        params["type_doc"] = value

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(
                            JsonParser.parseString(
                                response.body()
                                    ?.string()
                            )
                        )
                        val json = JSONObject(prettyJson)
                        val is_avail = json.getString("is_avail")
                        var text_comm = ""
                        if (json.has("text_comm")){
                            text_comm = json.getString("text_comm")
                        }
                        var text_doc = ""
                        if (json.has("text_doc")){
                            text_doc = json.getString("text_doc")
                        }
                        if (is_avail != "1" || text_doc != ""){
                            additionalInfo?.setText(text_doc)
                            additionalInfo?.hint = text_comm
                            additionalInfo?.isEnabled = false
                            ObjectAnimator.ofFloat(additionalInfo, "alpha", 0f, 1f).apply {
                                duration = 300
                                start()
                            }
                        }else{
                            additionalInfo?.setText("")
                            additionalInfo?.hint = text_comm
                            additionalInfo?.isEnabled = true
                            ObjectAnimator.ofFloat(additionalInfo, "alpha", 0f, 1f).apply {
                                duration = 300
                                start()
                            }
                        }



                    }
                }
            }  catch (e: Throwable) {
               Log.e("err", e.stackTraceToString())
            }


        }
    }

    fun getDocsInfo(){
        additionInfoLayout?.visibility = View.GONE
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()

        val url = "https://ciu.nstu.ru/student_study/docs/claims/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.Study()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    spinner?.visibility = View.GONE
                    val pretty = response.body()?.string().toString()
                    var doc = Jsoup.parse(pretty)
                    val types = doc.body().select("select.types").first()
                    val options = types.select("option")
                    val optionNames = mutableListOf<String>()
                    val optionValues = mutableListOf<String>()
                    for (option in options){
                        optionNames += option.text()
                        optionValues += option.attr("value")
                    }
                    docSelection?.setSimpleItems(optionNames.toTypedArray())
                    docSelection?.setOnItemClickListener { parent, view, position, id ->
                        selectedValue = optionValues[position].toString()
                        if (additionInfoLayout?.visibility == View.GONE){
                            additionInfoLayout?.visibility = View.VISIBLE
                            ObjectAnimator.ofFloat(additionInfoLayout, "alpha", 1f).apply {
                                duration = 300
                                start()
                            }
                        }
                        chaim?.setOnClickListener {
                            if (selectedValue != ""){
                                Log.e("selected", selectedValue.toString())
                                sendClaim(selectedValue, additionalInfo?.text.toString())
                            }else{
                                Toast.makeText(Ficus.applicationContext(), "Выберите тип документа", Toast.LENGTH_SHORT).show()
                            }
                        }
                        getAdditionalInfo(selectedValue)
                    }
                    updateDocs()
                } else {

                    Log.e("RETROFIT_ERROR", response.code().toString())

                }
            }
        }
    }


}