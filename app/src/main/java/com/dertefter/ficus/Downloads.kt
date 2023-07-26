package com.dertefter.ficus

import android.animation.ObjectAnimator
import androidx.appcompat.widget.Toolbar
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.color.DynamicColors
import java.io.File


class Downloads : AppCompatActivity() {

    fun deleteAll(){
        val files = File(filesDir.absolutePath+"/down/")
        files.deleteRecursively()
        no_files?.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(no_files, View.ALPHA, 0f, 1f).setDuration(400).start()
        downloads_view?.removeAllViews()
    }


    fun openFile(name: String, path: String){
        val uri = FileProvider.getUriForFile(Ficus.applicationContext(), BuildConfig.APPLICATION_ID + ".provider", File(path))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        var open = false
        if (".pdf" in name){
            open = true
            intent.setDataAndType(uri, "application/pdf")
        } else if (".jpg" in name || ".jpeg" in name || ".png" in name){
            open = true
            intent.setDataAndType(uri, "image/*")
        } else if (".docx" in name || ".doc" in name){
            open = true
            intent.setDataAndType(uri, "application/msword")
        } else if (".ppt" in name || ".pptx" in name){
            open = true
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (".xls" in name || ".xlsx" in name){
            open = true
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (".txt" in name){
            open = true
            intent.setDataAndType(uri, "text/plain")
        } else if (".mp3" in name){
            open = true
            intent.setDataAndType(uri, "audio/*")
        } else if (".mp4" in name){
            open = true
            intent.setDataAndType(uri, "video/*")
        } else if (".zip" in name || ".rar" in name){
            open = true
            intent.setDataAndType(uri, "application/zip")
        } else if (".apk" in name){
            open = true
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        if (open){
            try {
                startActivity(intent)
            }  catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Нечем открыть файл", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Тип файла не поддерживается", Toast.LENGTH_SHORT).show()
        }

    }

    var downloads_view: LinearLayout? = null
    var toolbar: Toolbar? = null
    var no_files: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        val theme = intent.getStringExtra("fromDi")
        if (AppPreferences.monet == true){
            DynamicColors.applyToActivityIfAvailable(this)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.downloads_screen)
        no_files = findViewById(R.id.no_files)
        downloads_view = findViewById(R.id.files_list)
        toolbar = findViewById(R.id.toolbar_downloads)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        toolbar?.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.del -> {
                    deleteAll()
                    true
                }
                else -> false
            }
        })

        val files = File(filesDir.absolutePath+"/down/").listFiles()
        if (files != null){
            for (file in files!!){
                Log.e("file", file.name)
                val item = layoutInflater.inflate(R.layout.di_item_downloaded, null)
                item.findViewById<TextView>(R.id.file_name).text = file.name
                item.setOnClickListener {
                    openFile(file.name, file.absolutePath)
                }
                downloads_view!!.addView(item)
                no_files?.visibility = View.GONE

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
}

