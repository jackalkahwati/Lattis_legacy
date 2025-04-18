package io.lattis.domain.usecase.thing

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.ThingRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import okhttp3.ResponseBody
import javax.inject.Inject

class HeadTailLightUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val thingRepository: ThingRepository
) : UseCase<ResponseBody>(threadExecutor, postExecutionThread) {
    private var headLight:Int?=null
    private var tailight:Int?=null
    private var thing_id:Int=0

    fun withThingId(thing_id:Int):HeadTailLightUseCase{
        this.thing_id = thing_id
        return this
    }

    fun withHeadLight(headLight:Int?):HeadTailLightUseCase{
        this.headLight = headLight
        return this
    }

    fun withTailLight(tailight:Int?):HeadTailLightUseCase{
        this.tailight = tailight
        return this
    }


    override fun buildUseCaseObservable(): Observable<ResponseBody> {
        return thingRepository.lightItOnOffFlicker(thing_id,headLight,tailight)
    }

}