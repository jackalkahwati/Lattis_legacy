package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Bike
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SearchBikeByNameUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<List<Bike>>(threadExecutor, postExecutionThread) {

    private var bike_name:String?=null

    fun withBikeName(bike_name:String?) :SearchBikeByNameUseCase{
        this.bike_name = bike_name
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<Bike>> {
        return bikeRepository.searchBikeByName(bike_name)
    }
}