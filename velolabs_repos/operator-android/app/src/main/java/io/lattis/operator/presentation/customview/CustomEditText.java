package io.lattis.operator.presentation.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import io.lattis.operator.R;
import io.lattis.operator.presentation.utils.TypefaceLoader;


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
