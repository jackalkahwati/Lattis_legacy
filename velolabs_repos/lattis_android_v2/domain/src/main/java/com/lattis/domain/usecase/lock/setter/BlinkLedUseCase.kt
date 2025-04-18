package com.lattis.domain.usecase.lock.setter

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class BlinkLedUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val repository: BluetoothRepository
) : UseCase<Void>(threadExecutor, postExecutionThread) {
    private lateinit var macAddress: String
    fun withMacAddress(macAddress: String): BlinkLedUseCase {
        this.macAddress = macAddress
        return this
    }

    override fun buildUseCaseObservable(): Observable<Void> {
        return repository.blinkLed(macAddress)
    }

}