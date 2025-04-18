package com.lattis.domain.usecase.axa

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Parking
import com.lattis.domain.models.axa.AxaKey
import com.lattis.domain.repository.AxaLockRepository
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetAxaLockKeyUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val axaLockRepository: AxaLockRepository
) : UseCase<AxaKey>(threadExecutor, postExecutionThread) {
    private var lockId:String?=null

    fun withLockId(lockId: String): GetAxaLockKeyUseCase {
        this.lockId = lockId
        return this
    }

    override fun buildUseCaseObservable(): Observable<AxaKey> {
        return axaLockRepository.getAxaKey(lockId!!)
    }

}