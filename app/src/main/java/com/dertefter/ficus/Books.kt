package com.dertefter.ficus

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.io.IOException


class Books : AppCompatActivity() {
    var spinner: ProgressBar? = null
    var toolbar: MaterialToolbar? = null
    var booksView: LinearLayout? = null
    var image_barcode: ImageView? = null
    var no_books: LinearLayout? = null

    private fun displayBitmap(value: String) {
        val widthPixels = image_barcode!!.width
        val heightPixels = image_barcode!!.height

        image_barcode!!.setImageBitmap(
            createBarcodeBitmap(
                barcodeValue = value,
                barcodeColor = getColor(android.R.color.black),
                backgroundColor = getColor(android.R.color.white),
                widthPixels = widthPixels,
                heightPixels = heightPixels
            )
        )
        //text_barcode_number.text = value
    }

    private fun createBarcodeBitmap(
        barcodeValue: String,
        @ColorInt barcodeColor: Int,
        @ColorInt backgroundColor: Int,
        widthPixels: Int,
        heightPixels: Int
    ): Bitmap {
        val bitMatrix = Code128Writer().encode(
            barcodeValue,
            BarcodeFormat.CODE_128,
            widthPixels,
            heightPixels
        )

        val pixels = IntArray(bitMatrix.width * bitMatrix.height)
        for (y in 0 until bitMatrix.height) {
            val offset = y * bitMatrix.width
            for (x in 0 until bitMatrix.width) {
                pixels[offset + x] =
                    if (bitMatrix.get(x, y)) barcodeColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(
            bitMatrix.width,
            bitMatrix.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.setPixels(
            pixels,
            0,
            bitMatrix.width,
            0,
            0,
            bitMatrix.width,
            bitMatrix.height
        )
        return bitmap
    }

    fun getBaracode(){
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Accept-Language", "ru,en;q=0.9")
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val urlKoha = "https://koha.library.nstu.ru/"
        val retrofit = Retrofit.Builder()
            .baseUrl(urlKoha)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["koha_login_context:"] = "opac"
        params["userid"] = AppPreferences.login!!
        params["password"] = AppPreferences.password!!
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.getLibraryData(params)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        spinner?.visibility = View.GONE
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val main = doc.select("div.maincontent")
                        val num = main.select("li").first().ownText()
                        Log.e("num", num)
                        displayBitmap(num)
                        ObjectAnimator.ofFloat(image_barcode, "alpha", 0f, 1f).apply {
                            duration = 400
                            start()
                        }

                    }
                }
            }
             catch (e: IOException){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Books, "Ошибка подключения", Toast.LENGTH_LONG).show()
                }

            }
             catch (e: Throwable){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Books, "Ошибка подключения", Toast.LENGTH_LONG).show()
                    Log.e("Error", e.toString())
                }
            }



        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.books_screen)
        image_barcode = findViewById(R.id.image_barcode)
        toolbar = findViewById(R.id.toolbar_books)
        no_books = findViewById(R.id.no_books)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        toolbar?.addSystemWindowInsetToMargin(top = true)
        spinner = findViewById(R.id.spinner_books)
        val mInflater = LayoutInflater.from(this)
        booksView = findViewById(R.id.booksView)
        val client = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Accept-Language", "ru,en;q=0.9")
                    .build()
                chain.proceed(authorized)
            })
            .build()
        val urlKoha = "https://koha.library.nstu.ru/"
        val retrofit = Retrofit.Builder()
            .baseUrl(urlKoha)
            .client(client)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["koha_login_context:"] = "opac"
        params["userid"] = AppPreferences.login!!
        params["password"] = AppPreferences.password!!
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = service.authBooks(params)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        spinner?.visibility = View.GONE
                        val pretty = response.body()?.string().toString()
                        val doc: Document = Jsoup.parse(pretty)
                        val tbody = doc.body().select("tbody")
                        if (tbody.select("span.biblio-title").first() != null){
                            for (book in tbody.select("tr")){
                                spinner?.visibility = View.GONE
                                val title = book.select("span.biblio-title").text().toString()
                                val subtitle = book.select("span.subtitle").text().toString()
                                val author = book.select("td.author").text().toString().replace(",", "")
                                val renew = book.select("td.renew").text().toString().replace(",", "")
                                val getback = book.select("td.date_due").text().toString().replace("/", ".")
                                val fines = book.select("td.fines").first().text().toString()


                                var item: View = mInflater.inflate(R.layout.item_book, null, false)
                                item.findViewById<TextView>(R.id.book_title).text = title
                                item.findViewById<TextView>(R.id.book_subtitle).text = subtitle
                                item.findViewById<TextView>(R.id.book_autor).text = author
                                item.findViewById<TextView>(R.id.renew).text = "Продление: $renew"
                                item.findViewById<TextView>(R.id.getback).text = getback
                                item.findViewById<TextView>(R.id.fines).text = fines
                                booksView?.addView(item)



                            }
                        }else{
                            no_books?.visibility = View.VISIBLE
                            ObjectAnimator.ofFloat(no_books, "alpha", 0f, 1f)?.setDuration(400)?.start()
                        }


                    }
                }
            }
             catch (e: IOException){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Books, "Ошибка подключения", Toast.LENGTH_LONG).show()
                }

            }
             catch (e: Throwable){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Books, "Ошибка подключения", Toast.LENGTH_LONG).show()
                    Log.e("Error", e.toString())
                }
            }



        }
        getBaracode()

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

