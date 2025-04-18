package com.lattis.lattis.presentation.base.activity

import android.os.Bundle
import com.lattis.lattis.presentation.base.BasePresenter
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.utils.FirebaseUtil
import java.io.File

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
    fun logCustomException(e: Throwable?) {
        FirebaseUtil.instance?.logInTimber(e)
    }

    fun deleteCurrentPhotoPath(){
        if(currentPhotoPath!=null){
            val file = File(currentPhotoPath)
            if(file.exists())file.delete()
            currentPhotoPath=null
        }
    }
}