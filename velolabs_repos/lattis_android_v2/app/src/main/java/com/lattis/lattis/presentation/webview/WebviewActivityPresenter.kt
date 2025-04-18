package com.lattis.lattis.presentation.webview

import android.os.Bundle
import android.text.TextUtils
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.popup.PopUpActivity
import com.lattis.lattis.presentation.webview.WebviewActivity.Companion.WEB_URL
import javax.inject.Inject

class WebviewActivityPresenter @Inject constructor(

) : ActivityPresenter<WebviewActivityView>(){



    override fun setup(arguments: Bundle?) {
        super.setup(arguments)
        if (arguments != null) {
            if (arguments.containsKey(WEB_URL) && !TextUtils.isEmpty(arguments.getString(WEB_URL)))  {
                view?.loadWebView(arguments.getString(WEB_URL)!!)
            }else{
                view?.handleError()
            }
        }
    }
}