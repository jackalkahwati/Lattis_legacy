package com.lattis.lattis.presentation.base

import android.view.View

interface BaseView : DataView<View.OnClickListener?> {
    fun hideKeyboard()
}