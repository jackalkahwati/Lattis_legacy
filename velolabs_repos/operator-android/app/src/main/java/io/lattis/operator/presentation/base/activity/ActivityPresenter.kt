package io.lattis.operator.presentation.base.activity

import android.os.Bundle
import io.lattis.operator.presentation.base.BasePresenter
import io.lattis.operator.presentation.base.BaseView

abstract class ActivityPresenter<View : BaseView> : BasePresenter<View>() {
    var currentPhotoPath: String?=null
    fun onCreate(arguments: Bundle?, view: View) {
        setView(view)
        setup(arguments)
        setSetupState(true)
    }

    fun onReCreate(savedInstanceState: Bundle, view: View) {
        setView(view)
        if (!isSetup) {
            setup(savedInstanceState)
            setSetupState(true)
        }
    }

    fun onDestroy(isFinishing: Boolean) {
        clearView()
        if (isFinishing) {
            finish()
        } else {
            onTempDestroy()
        }
    }

    protected fun onTempDestroy() {}
    fun onReenter() {}
}