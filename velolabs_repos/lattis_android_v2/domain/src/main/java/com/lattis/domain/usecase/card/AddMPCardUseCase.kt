package com.lattis.domain.usecase.card

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class AddMPCardUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val cardRepository: CardRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    var fleet_id:Int?=null
    var token:String?=null

    fun withFleetId(fleet_id:Int):AddMPCardUseCase{
        this.fleet_id = fleet_id
        return this
    }
    fun withToken(token:String):AddMPCardUseCase{
        this.token = token
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return cardRepository.addMPCard(token!!,fleet_id)
    }
}