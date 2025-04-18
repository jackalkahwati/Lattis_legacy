package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Bike
import com.lattis.domain.models.Location
import com.lattis.domain.models.Rentals
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SearchBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val bikeRepository: BikeRepository
) : UseCase<Rentals>(threadExecutor, postExecutionThread) {

    private var northEast:Location?=null
    private var southWest:Location?=null

    fun withNorthEast(location:Location) :SearchBikeUseCase{
        this.northEast = location
        return this
    }

    fun withSouthWest(location:Location) :SearchBikeUseCase{
        this.southWest = location
        return this
    }

    override fun buildUseCaseObservable(): Observable<Rentals> {
        return bikeRepository.searchBike(northEast,southWest)
    }

}