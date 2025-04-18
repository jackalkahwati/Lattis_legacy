package com.lattis.domain.usecase.maintenance

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.MaintenanceRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ReportTheftUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var bikeId = 0
    private var tripId = -1
    override fun buildUseCaseObservable(): Observable<Boolean> {
        return maintenanceRepository.reportTheft(bikeId, tripId)
    }

    fun withBikeId(bikeId: Int): ReportTheftUseCase {
        this.bikeId = bikeId
        return this
    }

    fun withTripId(tripId: Int): ReportTheftUseCase {
        this.tripId = tripId
        return this
    }

}