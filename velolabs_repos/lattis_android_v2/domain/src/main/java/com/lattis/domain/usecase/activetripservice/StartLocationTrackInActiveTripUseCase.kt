package com.lattis.domain.usecase.activetripservice


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.ActiveTripRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Lock
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

class StartLocationTrackInActiveTripUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            val activeTripRepository: ActiveTripRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var lock: Lock? = null


    fun withLock(lock: Lock): StartLocationTrackInActiveTripUseCase {
        this.lock = lock
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return activeTripRepository.startLocationTracking(lock!!)
    }
}
