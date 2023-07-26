package com.dertefter.ficus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*
import androidx.fragment.app.Fragment

class Profile : Fragment(R.layout.profile_fragment) {
    var toolbar: Toolbar? = null
    var profileImage: ImageView? = null
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (AppPreferences.guest == true) {

            fragmentManager?.beginTransaction()?.replace(R.id.profile_mwnu, profileMenuGuest())?.commit()
            toolbar?.menu?.clear()
        } else{
            fragmentManager?.beginTransaction()?.replace(R.id.profile_mwnu, profileMenu())?.commit()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.toolbar_pr)
        toolbar?.addSystemWindowInsetToMargin(top = true)
        toolbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings_button -> {
                    var inta = Intent(Ficus.applicationContext(), Settings::class.java)
                    startActivity(inta)
                    true
                }
                else -> false
            }
        }
        if (AppPreferences.guest == true) {

            fragmentManager?.beginTransaction()?.replace(R.id.profile_mwnu, profileMenuGuest())?.commit()
            toolbar?.menu?.clear()
        } else{
            fragmentManager?.beginTransaction()?.replace(R.id.profile_mwnu, profileMenu())?.commit()
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