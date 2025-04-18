package com.lattis.ellipse.presentation.ui.base.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Created by Velo Labs Android on 17-04-2017.
 */

public abstract class BaseBackWhiteArrowActivity<Presenter extends ActivityPresenter> extends BaseActivity<Presenter> {

    private final int ARROW_WITH_HALF_WHITE = 02;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setupAppbarCloseIcon(toolbar, ARROW_WITH_HALF_WHITE);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }


}
