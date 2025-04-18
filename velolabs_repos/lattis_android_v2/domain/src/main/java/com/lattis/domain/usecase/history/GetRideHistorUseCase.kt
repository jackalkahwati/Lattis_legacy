package com.lattis.domain.usecase.history

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.RideRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.RideHistory
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetRideHistorUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val rideRepository: RideRepository
) : UseCase<RideHistory>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(): Observable<RideHistory> {
        return rideRepository.getRideHistory()
    }
}