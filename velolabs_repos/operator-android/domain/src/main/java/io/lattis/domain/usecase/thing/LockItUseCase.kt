package io.lattis.domain.usecase.thing

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.ThingRepository
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import okhttp3.ResponseBody
import javax.inject.Inject

class LockItUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val thingRepository: ThingRepository
) : UseCase<ResponseBody>(threadExecutor, postExecutionThread) {
    private var thing_id:Int=0

    fun withThingId(thing_id:Int):LockItUseCase{
        this.thing_id = thing_id
        return this
    }


    override fun buildUseCaseObservable(): Observable<ResponseBody> {
        return thingRepository.lockIt(thing_id)
    }

}