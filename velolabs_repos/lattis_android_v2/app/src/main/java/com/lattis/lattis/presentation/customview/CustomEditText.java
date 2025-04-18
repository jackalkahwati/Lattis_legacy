package com.lattis.lattis.presentation.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

import com.lattis.lattis.presentation.utils.TypefaceLoader;

import io.lattis.lattis.R;

public class CustomEditText extends AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
