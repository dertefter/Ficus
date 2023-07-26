package com.dertefter.ficus

import AppPreferences
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.io.IOException
import java.io.ObjectInput


class Messages : Fragment(R.layout.messages_fragment) {


    var messagesView1: LinearLayout? = null
    var messagesView2: LinearLayout? = null
    var mInflater: LayoutInflater? = null
    var toolbar: Toolbar? = null
    var toolbarLayout: AppBarLayout? = null
    var selectableToolbar: Toolbar? = null
    var selectableToolbarLayout: AppBarLayout? = null
    var spinner: ProgressBar? = null
    var animation: FrameLayout? = null
    var current_value = 1
    var no_mesTextView: TextView? = null
    var isSelectMode: Boolean = false
    var mes_ids = mutableListOf<String>()
    var how_many_mes: Int = 0
    var tabLayout: TabLayout? = null



    @ColorInt
    fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun getLinkForImage(name: String, message_item: View) {
        val array = name.replace("ё", "e").split(" ")
        val searchNameValue = array[0]
        val indexSur = array[1].split(".")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.nstu.ru/")
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.findPerson(searchNameValue)
                if (response.isSuccessful) {
                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val result = doc.select("div.search-result")
                    val results = result.select("div.search-result__item")
                    for (i in results){

                        val person_name = i.select("a").first().text()
                        val person_array = person_name.split(" ")
                        //Log.e("error_load", person_array.toString())
                        try{
                            if ((results.size == 1) || person_array[1][0].toString() == indexSur[0] && person_array[2][0].toString() == indexSur[1]){
                                val img = i.select("img").first()

                                val transformation: Transformation =
                                    RoundedTransformationBuilder()
                                        .borderColor(Color.BLACK)
                                        .borderWidthDp(0f)
                                        .cornerRadiusDp(60f)
                                        .oval(false)
                                        .build()

                                withContext(Dispatchers.Main){
                                    if (img != null){
                                        val profile_pic= "https://www.nstu.ru/" + img.attr("src")
                                        Picasso.get().load(profile_pic).resize(200, 200).centerCrop().transform(transformation).into(message_item.findViewById<ImageView>(R.id.latter_background))
                                        message_item.findViewById<ImageView>(R.id.latter_background)?.imageTintMode = null
                                        message_item.findViewById<TextView>(R.id.first_latter).visibility = View.GONE
                                    }
                                    message_item.visibility = View.VISIBLE
                                    ObjectAnimator.ofFloat(message_item.findViewById<ImageView>(R.id.latter_background), "alpha", 0f, 1f).apply {
                                        duration = 300
                                        start()
                                    }


                                }



                            }
                        } catch (e: Exception){
                            Log.e("error_load",e.stackTraceToString())
                        }

                    }
                }
            }catch (e: Exception){
                Log.e("mes", e.stackTraceToString())
            }catch (e: Throwable){
                Log.e("mes", e.stackTraceToString())
            }catch (e: IOException){
                Log.e("mes", e.stackTraceToString())
            }

        }
    }

    fun mes(value: Int) {
        current_value = value
        if (value == 1){
            messagesView1?.removeAllViews()
            messagesView2?.visibility = View.GONE
            messagesView1?.visibility = View.VISIBLE
        }
        else{
            messagesView2?.removeAllViews()
            messagesView1?.visibility = View.GONE
            messagesView2?.visibility = View.VISIBLE
        }
        animation?.visibility = View.GONE
        spinner?.visibility = View.VISIBLE

        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            })
            .build()

        val url1 = "https://ciu.nstu.ru/student_study/"
        var retrofit = Retrofit.Builder()
            .baseUrl(url1)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.messages("-1")
                if (response.isSuccessful) {
                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val new_tab1 = doc.select("span#block1").first().text().toString()
                    val new_tab2 = doc.select("span#block2").first().text().toString()
                    Log.e("yabs", new_tab1 + " " + new_tab2)
                    if (new_tab1 != ""){
                        val num1 = new_tab1.split(" ")[0].toInt()
                        val badge = tabLayout?.getTabAt(0)?.orCreateBadge
                        badge?.isVisible = true
                        badge?.number = num1
                    }else{
                        val badge = tabLayout?.getTabAt(0)?.orCreateBadge
                        badge?.isVisible = false
                    }
                    if (new_tab2 != ""){
                        val num2 = new_tab2.split(" ")[0].toInt()
                        val badge = tabLayout?.getTabAt(1)?.orCreateBadge
                        badge?.isVisible = true
                        badge?.number = num2
                    }else{
                        val badge = tabLayout?.getTabAt(1)?.orCreateBadge
                        badge?.isVisible = false
                    }
                    val s = doc.body().select("main.page-content")
                    if (doc.select("span.num_of_msg").toString() != ""){
                        val num = doc.select("span.num_of_msg").first().text().toString()
                        if (num.length != 0){
                            val numInt = num.toInt()
                            val bnav: BottomNavigationView? = activity?.findViewById(R.id.bottomNavigationView)
                            var badge = bnav?.getOrCreateBadge(R.id.messages_nav)
                            badge?.isVisible = true
                            badge?.number = numInt

                        }else{
                            val bnav: BottomNavigationView? = activity?.findViewById(R.id.bottomNavigationView)
                            bnav?.removeBadge(R.id.messages_nav)
                        }
                    }else{
                        val bnav: BottomNavigationView? = activity?.findViewById(R.id.bottomNavigationView)
                        bnav?.removeBadge(R.id.messages_nav)
                    }

                    val tbody = s.select("div#new_table$value").first()

                    val messages = tbody?.select("div.pad")
                    withContext(Dispatchers.IO) {
                        withContext(Dispatchers.Main){
                            spinner?.visibility = View.GONE
                        }

                        if (messages != null) {
                            withContext(Dispatchers.Main){
                                if (messages.size != 0) {
                                    animation?.visibility = View.GONE
                                } else {
                                    animation?.visibility = View.VISIBLE
                                    ObjectAnimator.ofFloat(animation, "alpha", 0f, 1f).apply {
                                        duration = 300
                                        start()
                                    }
                                }
                            }

                            for (i in messages) {
                                val message_item: View =
                                    mInflater!!.inflate(R.layout.message, null, false)
                                var is_new = false
                                if (i.hasClass("new_message_header")){
                                    is_new = true
                                }
                                val send_by = i.select("div.col-2.col-sm-6").first().text().toString()
                                val mes_text = i.select("div.col-8.col-sm-6").first().text().toString()
                                val mes_id = i.select("div.col-8.col-sm-6").first().attr("onclick").toString()
                                    .replace("openWin2('https://ciu.nstu.ru/student_study/mess_teacher/view?id=","")
                                    .replace("');return false;","")
                                if (is_new){
                                    message_item.findViewById<ImageView>(R.id.mes_new_indicator).visibility = View.VISIBLE
                                }
                                message_item.findViewById<TextView>(R.id.send_by).text = send_by
                                message_item.findViewById<TextView>(R.id.message_text).text = mes_text
                                message_item.findViewById<TextView>(R.id.mes_id).text = mes_id
                                message_item.findViewById<ImageView>(R.id.latter_background)?.setImageResource(R.drawable.yava_part)
                                message_item.findViewById<TextView>(R.id.first_latter).text = message_item.findViewById<TextView>(R.id.send_by).text[0].toString()
                                message_item.isClickable = true
                                message_item.setOnClickListener {
                                    if (!isSelectMode){
                                        val inta = Intent(
                                            Ficus.applicationContext(),
                                            ReadMessageActivity::class.java
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        inta.putExtra("mesid", mes_id)
                                        Ficus.applicationContext().startActivity(inta)
                                    }
                                    else{
                                        if (mes_ids.contains(message_item.findViewById<TextView>(R.id.mes_id).text.toString())){
                                            message_item.findViewById<MaterialCardView>(R.id.CardItem)?.setCardBackgroundColor(context?.getColorFromAttr(
                                                com.google.android.material.R.attr.colorSurface)!!)
                                            mes_ids.remove(message_item.findViewById<TextView>(R.id.mes_id).text.toString())
                                            selectableToolbar?.title = mes_ids.size.toString() + " выбрано"
                                            if (mes_ids.size == 0){
                                                selectModeDisable()

                                            }
                                        }else{
                                            message_item.findViewById<MaterialCardView>(R.id.CardItem)?.setCardBackgroundColor(context?.getColorFromAttr(
                                                com.google.android.material.R.attr.colorSurfaceVariant)!!)
                                            how_many_mes++

                                            mes_ids.add(message_item.findViewById<TextView>(R.id.mes_id).text.toString())
                                            selectableToolbar?.title = mes_ids.size.toString() + " выбрано"

                                        }

                                    }
                                }
                                message_item.setOnLongClickListener {
                                    if (!isSelectMode){
                                        isSelectMode = true
                                        selectModeEnable()
                                        how_many_mes++
                                        mes_ids.add(message_item.findViewById<TextView>(R.id.mes_id).text.toString())
                                        selectableToolbar?.title = (mes_ids.size).toString() + " выбрано"
                                        message_item.findViewById<MaterialCardView>(R.id.CardItem)?.setCardBackgroundColor(context?.getColorFromAttr(
                                            com.google.android.material.R.attr.colorSurfaceVariant)!!)
                                    }
                                    true
                                }
                                withContext(Dispatchers.Main) {
                                    if (value == 1){
                                        messagesView1?.addView(message_item)
                                        getLinkForImage(send_by, message_item)
                                    }else{
                                        Log.e("msg", send_by.toString())
                                        messagesView2?.addView(message_item)
                                        getLinkForImage(send_by, message_item)
                                    }

                                }


                            }
                        }
                    }



                } else {
                    val context: Context = Ficus.applicationContext()
                    var inta = Intent(
                        context,
                        NetworkErrorActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(inta)

                }

            }
             catch (e: Exception){
                Log.e("ficus.mes", e.stackTraceToString())
            }
             catch (e: Throwable){
                Log.e("ficus.mes", e.stackTraceToString())
            }

        }
    }

    fun selectModeEnable(){
        toolbarLayout?.visibility = View.GONE
        selectableToolbarLayout?.visibility = View.VISIBLE
        isSelectMode = true
    }
    fun selectModeDisable(){
        toolbarLayout?.visibility = View.VISIBLE
        selectableToolbarLayout?.visibility = View.GONE
        isSelectMode = false
        mes_ids = mutableListOf<String>()
    }

    private fun deleteThis(MessageID: String) {
        how_many_mes = 0
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
            }  catch (e: Throwable) {
                Log.e("ficus.mes.delete", e.toString())
            } catch (e: Exception){
                Log.e("ficus.mes.delete", e.toString())
            }
        }


    }

    override fun onResume() {
        super.onResume()
        mes(current_value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.mes_tabs)
        tabLayout?.isInEditMode()
        toolbarLayout = view.findViewById(R.id.AppBarLayout)
        messagesView1 = view.findViewById(R.id.messages_view1)
        messagesView2 = view.findViewById(R.id.messages_view2)
        animation = view.findViewById(R.id.animationMessages)
        mInflater = LayoutInflater.from(activity)
        spinner = view.findViewById(R.id.spinner_mes)
        toolbar = view.findViewById(R.id.toolbar_messages)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        no_mesTextView = view.findViewById(R.id.no_mes_text)
        selectableToolbar = view.findViewById(R.id.Stoolbar_messages)
        selectableToolbar?.addSystemWindowInsetToMargin(top = true)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                current_value = tab?.position!! + 1
                Log.e("va", current_value.toString())
                mes(current_value)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
        selectableToolbarLayout = view.findViewById(R.id.SAppBarLayout)
        selectableToolbar?.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.delete_many -> {

                    for (i in 0..mes_ids.size - 1){
                        deleteThis(mes_ids[i])

                    }
                    selectModeDisable()
                    mes(current_value)
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