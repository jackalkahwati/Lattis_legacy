package io.lattis.operator.presentation.customview

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import io.lattis.operator.presentation.utils.TypefaceLoader
import io.lattis.operator.R

class CustomTextInputEditText : TextInputEditText {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs
    ) {
        init(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet
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