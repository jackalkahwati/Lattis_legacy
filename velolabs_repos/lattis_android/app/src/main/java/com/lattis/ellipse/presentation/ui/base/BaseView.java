package com.lattis.ellipse.presentation.ui.base;

import androidx.annotation.StringRes;
import android.view.View;

public interface BaseView extends DataView<View.OnClickListener> {

    void showToastMessage(String message);

    void showToastMessage(String message, int duration);

    void showSnackBar(String message, View.OnClickListener action, @StringRes int actionLabel);

    void showSnackBar(String message, View.OnClickListener action, String actionLabel);

    void hideKeyboard();

}
