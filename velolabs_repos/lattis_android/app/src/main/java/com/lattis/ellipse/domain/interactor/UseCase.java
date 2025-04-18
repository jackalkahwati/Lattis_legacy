package com.lattis.ellipse.domain.interactor;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public abstract class UseCase<ReturnType> {

    protected final ThreadExecutor threadExecutor;
    protected final PostExecutionThread postExecutionThread;

    protected UseCase(ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
    }

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
     */
    protected abstract Observable<ReturnType> buildUseCaseObservable();

    /**
     * Executes the current use case.
     *
     * @param disposableObserver The guy who will be listen to the observable build
     *                          with {@link #buildUseCaseObservable()}.
     */
    @SuppressWarnings("unchecked")
    public Disposable execute(DisposableObserver<ReturnType> disposableObserver) {
        return this.buildUseCaseObservable()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.getScheduler(), true)
                .subscribeWith(disposableObserver);
    }

    @SuppressWarnings("unchecked")
    public Disposable executeInMainThread(DisposableObserver<ReturnType> disposableObserver) {
        return this.buildUseCaseObservable()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(postExecutionThread.getScheduler(), true)
                .subscribeWith(disposableObserver);
    }

}
