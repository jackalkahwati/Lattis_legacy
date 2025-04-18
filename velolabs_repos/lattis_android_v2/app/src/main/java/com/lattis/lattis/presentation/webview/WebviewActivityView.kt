package com.lattis.lattis.presentation.webview

import com.lattis.lattis.presentation.base.BaseView

interface WebviewActivityView : BaseView{


    fun loadWebView(url:String)
    fun handleError()
}