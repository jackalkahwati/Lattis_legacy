package com.lattis.lattis.presentation.base.activity

import android.os.Bundle
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.ui.base.activity.BaseActivity
import kotlinx.android.synthetic.main.view_toolbar.*

/**
 * Created by Velo Labs Android on 17-04-2017.
 */
abstract class BaseCloseActivity<Presenter : ActivityPresenter<V>,V:BaseView> : BaseActivity<Presenter,V>() {
    private val CLOSE_ICON_WHITE = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun configureViews() {
        super.configureViews()
        setupAppbarCloseIcon(toolbar, CLOSE_ICON_WHITE)
    }

    override fun onResume() {
        super.onResume()
    }
}