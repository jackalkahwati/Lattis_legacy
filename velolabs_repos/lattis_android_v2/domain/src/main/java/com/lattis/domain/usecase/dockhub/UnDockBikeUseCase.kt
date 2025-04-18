package com.lattis.domain.usecase.dockhub

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.DockHub
import com.lattis.domain.repository.DockHubRepository
import com.lattis.domain.repository.ParkingRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.parking.GetDockHubUseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UnDockBikeUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val dockHubRepository: DockHubRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    var uuid:String?=null
    var hub_type:String?=null

    fun withUUID(uuid:String): UnDockBikeUseCase {
        this.uuid = uuid
        return this
    }

    fun withHubType(hub_type:String): UnDockBikeUseCase {
        this.hub_type = hub_type
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return dockHubRepository.undock(uuid!!,hub_type!!)
    }
}