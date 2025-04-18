package com.lattis.ellipse.presentation.ui.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.lattis.ellipse.R;

public class AlertDialogFragment extends AppCompatDialogFragment {

    public static final String TAG = AlertDialogFragment.class.getName();

    static final int NO_VALUE = -1;

    private int title = NO_VALUE;
    private int messageText = NO_VALUE;
    private int messageTextStyle = NO_VALUE;

    private int positiveText = NO_VALUE;
    private int positiveTextStyle = NO_VALUE;
    private DialogInterface.OnClickListener positiveListener;

    private int negativeText = NO_VALUE;
    private int negativeTextStyle = NO_VALUE;
    private DialogInterface.OnClickListener negativeListener;

    public AlertDialogFragment setTitle(@StringRes int title) {
        this.title = title;
        return this;
    }

    public AlertDialogFragment setMessage(@StringRes int message) {
        this.setMessage(message,NO_VALUE);
        return this;
    }

    public AlertDialogFragment setMessage(@StringRes int message, @StyleRes int style) {
        this.messageText = message;
        //this.messageTextStyle = style == NO_VALUE ? R.style.Text_Body_DarkSecondary : style;
        return this;
    }

    public AlertDialogFragment setPositiveButton(@StringRes int textId, @StyleRes int style, DialogInterface.OnClickListener listener) {
        this.positiveText = textId;
        //this.positiveTextStyle = style == NO_VALUE ? R.style.Text_Button_LightBlue : style;
        this.positiveListener = listener;
        return this;
    }

    public AlertDialogFragment setNegativeButton(@StringRes int textId, @StyleRes int style, DialogInterface.OnClickListener listener) {
        this.negativeText = textId;
        //this.negativeTextStyle = style == NO_VALUE ? R.style.Text_Button : style;
        this.negativeListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AppCompatDialog dialog = new AppCompatDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_alert_dialog_container);
        Window window = dialog.getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(),R.color.alert_background_color)));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        TextView textView = (TextView) dialog.findViewById(R.id.alertTitle);
        if(textView!=null && title != NO_VALUE){
            textView.setText(title);
        }

        TextView message = (TextView) dialog.findViewById(android.R.id.message);
        if(message != null && messageText != NO_VALUE){
            message.setText(messageText);
            setTextViewTextAppearance(message,messageTextStyle);
        }

        Button positive = (Button) dialog.findViewById(android.R.id.button1);
        if(positive != null && positiveText!=NO_VALUE){
            positive.setVisibility(View.VISIBLE);
            positive.setText(positiveText);
            setButtonTextAppearance(positive,positiveTextStyle);
            positive.setOnClickListener(v -> {
                if(positiveListener!=null){
                    positiveListener.onClick(dialog, AppCompatDialog.BUTTON_POSITIVE);
                }
            });
        } else if (positive != null) {
            positive.setVisibility(View.GONE);
        }

        Button negative = (Button) dialog.findViewById(android.R.id.button2);
        if(negative != null && negativeText != NO_VALUE){
            negative.setVisibility(View.VISIBLE);
            negative.setText(negativeText);
            setButtonTextAppearance(negative,negativeTextStyle);
            negative.setOnClickListener(v -> {
                if(negativeListener!=null){
                    negativeListener.onClick(dialog, AppCompatDialog.BUTTON_NEGATIVE);
                }
            });
        } else if (negative != null) {
            negative.setVisibility(View.GONE);
        }

        View dismiss = dialog.findViewById(R.id.touch_outside);
        if(dismiss!=null){
            dismiss.setOnClickListener(view1 -> {
                if (isVisible() && isCancelable()) {
                    dismiss();
                }
            });
        }

        return dialog;
    }


    private void setButtonTextAppearance(@NonNull Button button, @StyleRes int style){
        if(style!= NO_VALUE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
                button.setTextAppearance(style);
            } else {
                button.setTextAppearance(getActivity(),style);
            }
        }
    }

    private void setTextViewTextAppearance(@NonNull TextView textView, @StyleRes int style){
        if(style!= NO_VALUE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
                textView.setTextAppearance(style);
            } else {
                textView.setTextAppearance(getActivity(),style);
            }
        }
    }
}

