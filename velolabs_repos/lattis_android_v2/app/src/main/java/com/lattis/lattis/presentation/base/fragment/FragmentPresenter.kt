package com.lattis.lattis.presentation.base.fragment

import android.os.Bundle
import com.lattis.lattis.presentation.base.BasePresenter
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.utils.FirebaseUtil

abstract class FragmentPresenter<View : BaseView> : BasePresenter<View>() {
    fun onCreate(arguments: Bundle?, view: View) {
        setView(view)
        setup(arguments)
        setSetupState(true)
    }

    fun onRecreate(savedInstanceState: Bundle, view: View) {
        setView(view)
        setup(savedInstanceState)
        setSetupState(true)
    }

    fun onDestroy(isFinishing: Boolean) {
        clearView()
        if (isFinishing) {
            finish()
        }
    }

    fun logCustomException(e: Throwable?) {
        FirebaseUtil.instance?.logInTimber(e)
    }
}