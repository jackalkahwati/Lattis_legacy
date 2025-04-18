package com.lattis.domain.usecase.activetripservice

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.UpdateTripData
import com.lattis.domain.repository.ActiveTripRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Lock
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable


class StartActiveTripUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            val activeTripRepository: ActiveTripRepository
) : UseCase<UpdateTripData>(threadExecutor, postExecutionThread) {
    private var trip_id = 0
    private var title:String?=null

    fun withTripId(trip_id: Int): StartActiveTripUseCase {
        this.trip_id = trip_id
        return this
    }

    fun withTitle(title: String): StartActiveTripUseCase {
        this.title = title
        return this
    }

    override fun buildUseCaseObservable(): Observable<UpdateTripData> {
        return activeTripRepository.startActiveTripService(trip_id,title!!)
    }
}
