package io.lattis.domain.usecase.fleet

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Fleet
import io.lattis.domain.repository.FleetRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class SaveUserFleetUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val fleetRepository: FleetRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {

    lateinit var fleet: Fleet

    fun withFleet(fleet: Fleet):SaveUserFleetUseCase{
        this.fleet =fleet
        return this
    }


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return fleetRepository.saveUserFleet(fleet)
    }

}