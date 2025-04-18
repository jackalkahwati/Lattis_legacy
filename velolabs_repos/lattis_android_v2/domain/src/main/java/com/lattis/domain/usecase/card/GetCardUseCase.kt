package com.lattis.domain.usecase.card


import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.Card
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable

/**
 * Created by ssd3 on 7/26/17.
 */

class GetCardUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val cardRepository: CardRepository
) : UseCase<List<Card>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<Card>> {
        return cardRepository.getCard()
    }

}
