import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.Base64
import android.util.Log
import android.widget.TextView
import com.dertefter.ficus.Auth
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.Work
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor

import okhttp3.OkHttpClient
import okhttp3.Request


// Class to download Images which extends [Html.ImageGetter]
class ImageGetter(
    private val res: Resources,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    // Function needs to overridden when extending [Html.ImageGetter] ,
    // which will download the image
    @OptIn(DelicateCoroutinesApi::class)
    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)
        val client: OkHttpClient = OkHttpClient().newBuilder().cookieJar(Ficus.getCookieJar()!!)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()

        val picasso = Picasso.Builder(Ficus.applicationContext())
            .downloader(OkHttp3Downloader(client))
            .build()
        picasso.setIndicatorsEnabled(true)
        // Coroutine Scope to download image in Background
        GlobalScope.launch(Dispatchers.IO) {
            Log.e("ImageGetter", "Downloading Image: $url")
            var bitmap: Bitmap
            if (url.contains("data:image/png;base64,")){
                val base64String = url.replace("data:image/png;base64,", "")
                val imageData = Base64.decode(base64String, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            }
            else{
                val pic = Picasso.get().load(url)
                bitmap = pic.get()
            }
            // downloading image in bitmap format using [Picasso] Library


            val drawable = BitmapDrawable(res, bitmap)

            // To make sure Images don't go out of screen , Setting width less
            // than screen width, You can change image size if you want
            val width = getScreenWidth() - 340

            // Images may stretch out if you will only resize width,
            // hence resize height to according to aspect ratio
            val aspectRatio: Float =
                (drawable.intrinsicWidth.toFloat()) / (drawable.intrinsicHeight.toFloat())
            val height = width / aspectRatio
            drawable.setBounds(10, 20, width, height.toInt())
            holder.setDrawable(drawable)
            holder.setBounds(10, 20, width, height.toInt())
            withContext(Dispatchers.Main) {
                htmlTextView.text = htmlTextView.text
            }

        }
        return holder
    }

    // Actually Putting images
    internal class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) :
        BitmapDrawable(res, bitmap) {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }
    }

    // Function to get screenWidth used above
    fun getScreenWidth() =
        Resources.getSystem().displayMetrics.widthPixels
}
