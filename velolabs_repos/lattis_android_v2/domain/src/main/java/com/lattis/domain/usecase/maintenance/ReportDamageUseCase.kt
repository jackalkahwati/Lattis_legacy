package com.lattis.domain.usecase.maintenance

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.MaintenanceRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ReportDamageUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val maintenanceRepository: MaintenanceRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var bikeId = 0
    private var tripId = 0
    private var maintenanceImage: String? = null
    private var riderNotes: String? = null
    private var category: String = ""
    override fun buildUseCaseObservable(): Observable<Boolean> {
        return maintenanceRepository.reportDamage(
            category, riderNotes,
            bikeId, maintenanceImage, tripId
        )
    }

    fun withTripId(tripId: Int): ReportDamageUseCase {
        this.tripId = tripId
        return this
    }

    fun withBikeId(bikeId: Int): ReportDamageUseCase {
        this.bikeId = bikeId
        return this
    }

    fun withMaintenanceImage(maintenance_image: String?): ReportDamageUseCase {
        maintenanceImage = maintenance_image
        return this
    }

    fun withRiderNotes(rider_notes: String?): ReportDamageUseCase {
        riderNotes = rider_notes
        return this
    }

}