package io.lattis.domain.usecase.thing

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.ThingRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import okhttp3.ResponseBody
import javax.inject.Inject

class SoundUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val thingRepository: ThingRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var controlType:Int?=null
    private var workMode:Int?=null
    private var thing_id:Int=0

    fun withThingId(thing_id:Int):SoundUseCase{
        this.thing_id = thing_id
        return this
    }

    fun withControlType(controlType:Int?):SoundUseCase{
        this.controlType = controlType
        return this
    }

    fun withWorkMode(workMode:Int?):SoundUseCase{
        this.workMode = workMode
        return this
    }


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return thingRepository.sound(thing_id,controlType,workMode)
    }

}