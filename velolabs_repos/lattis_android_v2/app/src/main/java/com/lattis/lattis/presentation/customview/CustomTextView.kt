package com.lattis.lattis.presentation.customview

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import com.lattis.lattis.presentation.utils.TypefaceLoader
import io.lattis.lattis.R

class CustomTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    constructor(
//        context: Context,
//        attrs: AttributeSet?,
//        defStyleAttr: Int,
//        defStyleRes: Int
//    ) : super(context, attrs, defStyleAttr) {
//        init(context, attrs)
//    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomTypeFace)
        val font = a.getString(R.styleable.CustomTypeFace_typefaceAsset)
        if (font != null) {
            val path = String.format("fonts/%s", font)
            val tf: Typeface = TypefaceLoader.getTypeface(context, path)
            tf?.let { setTypeface(it) }
        }
        a.recycle()
    }
}