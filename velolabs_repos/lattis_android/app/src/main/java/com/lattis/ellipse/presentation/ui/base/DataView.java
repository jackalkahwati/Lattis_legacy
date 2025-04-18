package com.lattis.ellipse.presentation.ui.base;


public interface DataView<ActionListenerType> {

    void showLoading(boolean hideContent);

    void hideLoading();

    void setToolbarHeader(String title);

    void setToolbarDescription(String subtitle);

    void hideToolbar();

    void showError(String message, int duration);

    void showErrorWithAction(String message, ActionListenerType listener, String actionLabel);

}
