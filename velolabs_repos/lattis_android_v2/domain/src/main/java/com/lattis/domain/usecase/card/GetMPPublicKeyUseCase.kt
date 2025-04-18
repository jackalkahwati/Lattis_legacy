package com.lattis.domain.usecase.card

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.MPPublicKey
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetMPPublicKeyUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val cardRepository: CardRepository
) : UseCase<MPPublicKey>(threadExecutor, postExecutionThread) {
    var fleet_id:Int=0

    fun withFleetId(fleet_id:Int):GetMPPublicKeyUseCase{
        this.fleet_id = fleet_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<MPPublicKey>{
        return cardRepository.getMPPublicKey(fleet_id)
    }
}