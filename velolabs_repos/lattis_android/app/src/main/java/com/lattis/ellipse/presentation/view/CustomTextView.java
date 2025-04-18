package com.lattis.ellipse.presentation.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lattis.ellipse.presentation.view.utils.TypefaceLoader;

import io.lattis.ellipse.R;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTypeFace);
        String font = a.getString(R.styleable.CustomTypeFace_typefaceAsset);

        if (font != null) {
            String path = String.format("fonts/%s", font);
            Typeface tf = TypefaceLoader.getTypeface(context, path);
            if (tf != null) {
                setTypeface(tf);
            }
        }

        a.recycle();
    }

}
