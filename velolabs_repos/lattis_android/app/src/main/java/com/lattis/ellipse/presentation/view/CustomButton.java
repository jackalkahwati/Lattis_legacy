package com.lattis.ellipse.presentation.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.lattis.ellipse.presentation.view.utils.TypefaceLoader;

import io.lattis.ellipse.R;

public class CustomButton extends AppCompatButton {

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
