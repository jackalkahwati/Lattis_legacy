package com.lattis.domain.usecase.card

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DeleteCardUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val cardRepository: CardRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var cardId = 0
    fun setCardId(cardId: Int): DeleteCardUseCase {
        this.cardId = cardId
        return this
    }
    override fun buildUseCaseObservable(): Observable<Boolean> {
        return cardRepository.deleteCard(cardId)
    }

}