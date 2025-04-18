package io.lattis.domain.usecase.fleet

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.Fleet
import io.lattis.domain.repository.FleetRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject


class GetFleetsUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val fleetRepository: FleetRepository
) : UseCase<List<Fleet>>(threadExecutor, postExecutionThread) {


    override fun buildUseCaseObservable(): Observable<List<Fleet>> {
        return fleetRepository.getFleets()
    }

}