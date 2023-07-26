package com.dertefter.ficus.wearable

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView

class CustomNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr), NestedScrollingParent3 {

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(target, dx, dy, consumed, type)
        val childCount = childCount
        val visibleRect = Rect()
        getGlobalVisibleRect(visibleRect)
        Log.e("nes", "messs")
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childRect = Rect()
            child.getGlobalVisibleRect(childRect)
            if (!childRect.intersect(visibleRect)) {
                ObjectAnimator.ofFloat(child, "alpha", 0.5f).start()
                ObjectAnimator.ofFloat(child, "scaleX", 0.8f).start()
                ObjectAnimator.ofFloat(child, "scaleY", 0.8f).start()
            }else{
                ObjectAnimator.ofFloat(child, "alpha", 1f).start()
                ObjectAnimator.ofFloat(child, "scaleX", 1f).start()
                ObjectAnimator.ofFloat(child, "scaleY", 1f).start()
            }
        }

    }
}
