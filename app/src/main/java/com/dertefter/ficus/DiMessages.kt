package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Retrofit
import java.io.IOException


class DiMessages : Fragment(R.layout.di_messages_fragment) {
    var messagesView: LinearLayout? = null
    var mInflater: LayoutInflater? = null
    var toolbar: Toolbar? = null
    var toolbarLayout: AppBarLayout? = null
    var selectableToolbar: Toolbar? = null
    var selectableToolbarLayout: AppBarLayout? = null
    var spinner: ProgressBar? = null
    var bottomSpinner: ProgressBar? = null
    var animation: FrameLayout? = null
    var current_value = 0
    var no_mesTextView: TextView? = null
    var isSelectMode: Boolean = false
    var mes_ids = mutableListOf<String>()
    var how_many_mes: Int = 0
    var page = 1
    var group: String = "messages"
    var hasNewPage: Boolean = false
    var fab: FloatingActionButton? = null
    var new_chat_view: LinearLayout? = null
    fun selectModeEnable(){
        toolbarLayout?.visibility = View.INVISIBLE
        selectableToolbarLayout?.visibility = View.VISIBLE
        isSelectMode = true
    }
    fun selectModeDisable(){
        toolbarLayout?.visibility = View.VISIBLE
        selectableToolbarLayout?.visibility = View.INVISIBLE
        isSelectMode = false
    }

    private fun deleteThis(MessageID: String) {
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

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ciu.nstu.ru/student_study/mess_teacher/ajax_del_mes/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["idmes"] = MessageID
        params["what"] = "1"
        params["type"] = "1"
        params["vid_sort"] = "1"
        params["year"] = "-1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.postForm(params)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {

                    }
                }
            }   catch (e: IOException){
                Log.e("diTimeTable", e.toString())
            } catch (e: Throwable) {

            }
        }


    }

    fun getGroupMembers(id: String){
        ObjectAnimator.ofFloat(new_chat_view, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/proceed/")
            .client(client)
            .build()
        var service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "get_students_by_group"
        params["group_id"] = id
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getDispaceMessages(params)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    new_chat_view?.removeAllViews()
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.string()
                        )
                    )
                    val json = JSONObject(prettyJson)
                    val data = json.getJSONArray("data")
                    var new_messages_count = 0
                    for(i in 0 until data.length()){
                        val _id = data.getJSONObject(i).getString("user_to_workspace_id")
                        val name = data.getJSONObject(i).getString("name")
                        val surname = data.getJSONObject(i).getString("surname")
                        val patronymic = data.getJSONObject(i).getString("patronymic")



                        val message_item = mInflater?.inflate(R.layout.di_message_item, null)
                        message_item?.findViewById<ImageView>(R.id.mes_new_indicator)?.visibility = View.INVISIBLE
                        message_item?.findViewById<TextView>(R.id.send_by)?.text = "$surname $name $patronymic"
                        message_item?.findViewById<TextView>(R.id.message_text)?.text = ""
                        message_item?.findViewById<TextView>(R.id.mes_id)?.text =  _id
                        var photo = ""
                        if (data.getJSONObject(i).has("photo")){
                            photo = data.getJSONObject(i).getString("photo")
                            val transformation: Transformation =
                                RoundedTransformationBuilder()
                                    .borderColor(Color.BLACK)
                                    .borderWidthDp(0f)
                                    .cornerRadiusDp(60f)
                                    .oval(false)
                                    .build()
                            Picasso.get().load("https://dispace.edu.nstu.ru/public/images/photos/b_IMG_$photo").resize(200, 200).centerCrop().transform(transformation).into(message_item?.findViewById<ImageView>(R.id.send_by_image))
                            photo = "https://dispace.edu.nstu.ru/public/images/photos/b_IMG_$photo"
                            message_item?.findViewById<ImageView>(R.id.send_no_image)?.visibility = View.GONE
                            message_item?.findViewById<TextView>(R.id.first_latter)?.visibility = View.GONE
                        }else{
                            message_item?.findViewById<ImageView>(R.id.send_by_image)?.visibility = View.GONE
                            message_item?.findViewById<TextView>(R.id.first_latter)?.text = surname[0].toString()
                            message_item?.findViewById<ImageView>(R.id.send_no_image)?.setImageResource(R.drawable.yava_part)
                        }
                        message_item?.isClickable = true
                        message_item?.setOnClickListener {
                            val intent = Intent(activity, DiReadMessages::class.java)
                            intent.putExtra("mesid", _id)
                            intent.putExtra("photo", photo)
                            intent.putExtra("name", "$surname $name $patronymic")
                            startActivity(intent)
                        }

                        new_chat_view?.addView(message_item)
                    }
                }
            }
        }
    }

    fun getGroups(){
        new_chat_view?.removeAllViews()
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/proceed/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "get_group_list"
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getDispaceMessages(params)
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
                        val data = json.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val group_item = layoutInflater.inflate(R.layout.di_group_item, null)
                            val group = data.getJSONObject(i)
                            val group_name = group.getString("title")
                            val group_id = group.getString("id")
                            var desc = ""
                            if (group.has("description")){
                                desc = group.getString("description")
                            }else{
                                group_item.findViewById<TextView>(R.id.group_description).visibility = View.GONE
                            }
                            group_item.setOnClickListener {
                                getGroupMembers(group_id)
                            }
                            group_item.findViewById<TextView>(R.id.group_name).text = group_name
                            group_item.findViewById<TextView>(R.id.group_description).text = desc
                            new_chat_view?.addView(group_item)
                            ObjectAnimator.ofFloat(group_item, "alpha", 0f, 1f)?.setDuration(400)?.start()

                        }
                    }
                }
            } catch (e: Throwable){
                Log.e("dimes", e.toString())
            } catch (e: Exception){
                Log.e("dimes", e.toString())
            }

        }
    }

    fun mes(group: String, page: String){
        spinner?.visibility = View.VISIBLE
        if (page == "1"){
            messagesView?.removeAllViews()
        }
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/dialog/")
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["group"] = group
        params["page"] = page
        params["search"] = ""
        params["action"] = "list"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getDispaceMessages(params)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main){
                        spinner?.visibility = View.GONE
                    }

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    Log.e("dimestest", response.body().toString())
                    val prettyJson = gson.toJson(
                        JsonParser.parseString(
                            response.body()
                                ?.string()
                        )
                    )
                    val json = JSONObject(prettyJson)
                    val messages = json.getJSONArray("list")
                    val count = messages.length()
                    how_many_mes = count
                    withContext(Dispatchers.Main){
                        if (count == 0){
                            no_mesTextView?.visibility = View.VISIBLE
                        } else {
                            no_mesTextView?.visibility = View.GONE
                        }
                    }

                    val hasNewPageStr = json.getString("hasNewPage")
                    withContext(Dispatchers.Main){
                        if (hasNewPageStr == "false"){
                            hasNewPage = false
                            bottomSpinner?.visibility = View.GONE
                        }else{
                            hasNewPage = true
                            bottomSpinner?.visibility = View.VISIBLE
                        }
                    }

                    if (messages.length() == 0){
                        withContext(Dispatchers.Main){
                            no_mesTextView?.visibility = View.VISIBLE
                        }

                    }else{
                        for(i in 0 until messages.length()){
                            val _id = messages.getJSONObject(i).getString("companion_id")
                            val name = messages.getJSONObject(i).getString("name")
                            val surname = messages.getJSONObject(i).getString("surname")
                            val patronymic = messages.getJSONObject(i).getString("patronymic")
                            var message = messages.getJSONObject(i).getString("last_msg")

                            val doc = Jsoup.parse(message)
                            message = doc.text()
                            withContext(Dispatchers.Main){
                                val message_item = mInflater?.inflate(R.layout.di_message_item, null)
                                message_item?.findViewById<ImageView>(R.id.mes_new_indicator)?.visibility = View.GONE
                                message_item?.findViewById<TextView>(R.id.send_by)?.text = "$surname $name $patronymic"
                                message_item?.findViewById<TextView>(R.id.message_text)?.text = message
                                message_item?.findViewById<TextView>(R.id.mes_id)?.text =  _id
                                var photo = ""
                                if (messages.getJSONObject(i).has("photo")){
                                    photo = messages.getJSONObject(i).getString("photo")
                                    val transformation: Transformation =
                                        RoundedTransformationBuilder()
                                            .borderColor(Color.BLACK)
                                            .borderWidthDp(0f)
                                            .cornerRadiusDp(60f)
                                            .oval(false)
                                            .build()
                                    message_item?.findViewById<ImageView>(R.id.send_by_image)?.visibility = View.VISIBLE

                                    val picasso = Picasso.get()
                                    photo = "https://dispace.edu.nstu.ru/files/images/photos/b_IMG_$photo"
                                    picasso.load(photo).resize(200, 200).centerCrop().transform(transformation).into(message_item?.findViewById<ImageView>(R.id.send_by_image))

                                    message_item?.findViewById<ImageView>(R.id.send_no_image)?.visibility = View.GONE

                                    message_item?.findViewById<TextView>(R.id.first_latter)?.visibility = View.GONE
                                }
                                else{
                                    message_item?.findViewById<ImageView>(R.id.send_by_image)?.visibility = View.GONE
                                    message_item?.findViewById<TextView>(R.id.first_latter)?.text = surname[0].toString()
                                    message_item?.findViewById<ImageView>(R.id.send_no_image)?.setImageResource(R.drawable.yava_part)
                                }
                                message_item?.isClickable = true
                                message_item?.setOnClickListener {
                                    val intent = Intent(activity, DiReadMessages::class.java)
                                    intent.putExtra("mesid", _id)
                                    intent.putExtra("photo", photo)
                                    intent.putExtra("name", "$surname $name $patronymic")
                                    startActivity(intent)
                                }

                                messagesView?.addView(message_item)
                                ObjectAnimator.ofFloat(message_item, "alpha", 0f, 1f).setDuration(260).start()
                            }

                        }
                    }


                }

            }  catch (e: Throwable) {
                Log.e("dimes", e.stackTraceToString())
            } catch (e: IOException){
                Log.e("dimes", e.stackTraceToString())
            } catch (e: Exception){
                Log.e("dimes", e.stackTraceToString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mes("dialogs", page.toString())
        getGroups()
        new_chat_view?.visibility = View.GONE
        messagesView?.visibility = View.VISIBLE
        new_chat_view?.visibility = View.GONE
        fab?.setImageResource(R.drawable.edit)
        toolbar?.title = "Диалоги"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarLayout = view.findViewById(R.id.AppBarLayout)
        messagesView = view.findViewById(R.id.messages_view)
        new_chat_view = view.findViewById(R.id.new_chat_view)
        bottomSpinner = view.findViewById(R.id.down_mes)
        val mes_Scroll = view.findViewById<NestedScrollView>(R.id.mes_scroll)
        mes_Scroll.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v?.getChildAt(0)?.getMeasuredHeight()?.minus(v?.getMeasuredHeight()!!)) {
                if (hasNewPage){
                    page++
                    mes(group, page.toString())
                }
            }
        }
        animation = view.findViewById(R.id.animationMessages)
        mInflater = LayoutInflater.from(activity)
        spinner = view.findViewById(R.id.spinner_mes)
        toolbar = view.findViewById(R.id.toolbar_messages)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        fab = view.findViewById(R.id.send_mes_fab)
        fab?.setOnClickListener {
            if (messagesView?.visibility == View.VISIBLE){
                messagesView?.visibility = View.GONE
                new_chat_view?.visibility = View.VISIBLE
                fab?.setImageResource(R.drawable.close)
                toolbar?.title = "Новое сообщение"
            }else{
                messagesView?.visibility = View.VISIBLE
                new_chat_view?.visibility = View.GONE
                fab?.setImageResource(R.drawable.edit)
                toolbar?.title = "Диалоги"
            }
        }

        no_mesTextView = view.findViewById(R.id.no_mes_text)
        selectableToolbar = view.findViewById(R.id.Stoolbar_messages)
        selectableToolbar?.addSystemWindowInsetToMargin(top = true)
        selectableToolbarLayout = view.findViewById(R.id.SAppBarLayout)
        selectableToolbar?.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.delete_many -> {

                    for (i in 0..mes_ids.size - 1){
                        deleteThis(mes_ids[i])

                    }
                    selectModeDisable()

                    true
                }
                else -> false
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


