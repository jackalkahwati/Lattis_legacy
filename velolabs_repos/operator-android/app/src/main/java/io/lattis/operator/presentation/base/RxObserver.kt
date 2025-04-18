package io.lattis.operator.presentation.base

import io.reactivex.observers.DisposableObserver
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
        super.onStart()
    }

    override fun onError(e: Throwable) {

    }

    override fun onComplete() {

    }

    override fun onNext(t: T) {

    }
}