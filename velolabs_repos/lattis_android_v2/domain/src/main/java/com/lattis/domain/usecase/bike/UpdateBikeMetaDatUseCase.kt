package com.lattis.domain.usecase.bike

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BikeRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UpdateBikeMetaDatUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val bikeRepository: BikeRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    private var firmware_version: String? = null
    private var shackle_jam: Boolean? = null
    private var bike_battery_level = -1
    private var lock_battery_level = -1
    private var bike_id = 0

    fun withFirmWare(firmware_version: String?): UpdateBikeMetaDatUseCase {
        this.firmware_version = firmware_version
        return this
    }

    fun withShackleJamStatus(shackle_jam: Boolean?): UpdateBikeMetaDatUseCase {
        this.shackle_jam = shackle_jam
        return this
    }

    fun withLockBattery(lock_battery_level: Int?): UpdateBikeMetaDatUseCase {
        if (lock_battery_level != null) {
            this.lock_battery_level = lock_battery_level
        }
        return this
    }

    fun withBikeBattery(bike_battery_level: Int): UpdateBikeMetaDatUseCase {
        this.bike_battery_level = bike_battery_level
        return this
    }

    fun forBike(bike_id: Int): UpdateBikeMetaDatUseCase {
        this.bike_id = bike_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return bikeRepository.updateBikeMetaData(
            bike_id,
            bike_battery_level,
            lock_battery_level,
            firmware_version,
            shackle_jam!!
        )
    }

}