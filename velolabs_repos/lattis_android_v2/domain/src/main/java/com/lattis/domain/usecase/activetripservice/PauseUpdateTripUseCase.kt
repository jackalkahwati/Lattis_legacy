package com.lattis.domain.usecase.activetripservice

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ActiveTripRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class PauseUpdateTripUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            val activeTripRepository: ActiveTripRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var active:Boolean=false

    fun withActive(active:Boolean): PauseUpdateTripUseCase {
        this.active = active
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return activeTripRepository.pauseUpdateTrip(active)
    }
}