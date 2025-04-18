package io.lattis.operator.presentation.base.fragment

import android.os.Bundle
import io.lattis.operator.presentation.base.BasePresenter
import io.lattis.operator.presentation.base.BaseView

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
}