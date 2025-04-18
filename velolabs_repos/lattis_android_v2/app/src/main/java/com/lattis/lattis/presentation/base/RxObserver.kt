package com.lattis.lattis.presentation.ui.base

import com.lattis.lattis.presentation.base.DataView
import com.lattis.lattis.presentation.utils.FirebaseUtil
import io.reactivex.rxjava3.observers.DisposableObserver
import retrofit2.HttpException


open class RxObserver<T> : DisposableObserver<T> {
    private var dataView: DataView<*>? = null
    private var hideContent = true
    private var showLoading = true

    constructor() {}
    constructor(dataView: DataView<*>?) {
        this.dataView = dataView
    }

    constructor(dataView: DataView<*>?, showLoading: Boolean, hideContent: Boolean) {
        this.dataView = dataView
        this.showLoading = showLoading
        this.hideContent = hideContent
    }

    constructor(dataView: DataView<*>?, hideContent: Boolean) {
        this.dataView = dataView
        this.hideContent = hideContent
    }

    public override fun onStart() {
        if (dataView != null && showLoading) {
            dataView?.showLoading(hideContent)
        }
        super.onStart()
    }

    override fun onError(e: Throwable) {
        if (dataView != null) {
            dataView?.hideLoading()
        }
        if (e is HttpException) {
            if (e.localizedMessage != null &&
                e.response() != null &&
                e.response()!!.raw() != null &&
                e.response()!!.raw().request != null &&
                e.response()!!.raw().request!!.url != null &&
                e.response()!!.errorBody() != null &&
                e.response()!!.errorBody()!!.string() != null
            ) {
                val url = e.response()!!.raw().request!!.url
                val throwable = Throwable(
                    " " + url.toString() + " " + e.localizedMessage + " " + e.response()!!
                        .errorBody()!!.string()
                )
                FirebaseUtil.instance?.logInTimber(throwable)
            } else {
                FirebaseUtil.instance?.logInTimber(e)
            }
        }
    }

    override fun onComplete() {
        if (dataView != null) {
            dataView?.hideLoading()
        }
    }

    override fun onNext(t: T) {
        if (dataView != null) {
            dataView?.hideLoading()
        }
    }
}