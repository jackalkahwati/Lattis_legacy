package com.lattis.domain.usecase.base

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor

import java.util.concurrent.TimeUnit

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers

abstract class UseCase<ReturnType> protected constructor(protected val threadExecutor: ThreadExecutor,
                                                         protected val postExecutionThread: PostExecutionThread) {

    /**
     * Builds an [Observable] which will be used when executing the current [UseCase].
     */
    protected abstract fun buildUseCaseObservable(): Observable<ReturnType>

    /**
     * Executes the current use case.
     *
     * @param disposableObserver The guy who will be listen to the observable build
     * with [.buildUseCaseObservable].
     */
    open fun execute(disposableObserver: DisposableObserver<ReturnType>): Disposable {
        return this.buildUseCaseObservable()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler, true)
                .subscribeWith(disposableObserver)
    }

    open fun executeInMainThread(disposableObserver: DisposableObserver<ReturnType>): Disposable {
        return this.buildUseCaseObservable()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(postExecutionThread.scheduler, true)
                .subscribeWith(disposableObserver)
    }

    protected var lockVendor = LockVendor.ELLIPSE
    enum class LockVendor{
        ELLIPSE,
        AXA,
        TAPKEY,
        SAS,
        PSLOCK
    }

}
