package com.lattis.domain.usecase.card

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UpdateCardExpirationUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val cardRepository: CardRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var cardId: String? = null
    private var expiry_month = 0
    private var expiry_year = 0

    fun withCardId(cardId: String): UpdateCardExpirationUseCase {
        this.cardId = cardId
        return this
    }

    fun withExpiryMonth(expiry_month: Int): UpdateCardExpirationUseCase {
        this.expiry_month = expiry_month
        return this
    }

    fun withExpiryYear(expiry_year: Int): UpdateCardExpirationUseCase {
        this.expiry_year = expiry_year
        return this
    }


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return cardRepository.updateCardExpiration(cardId!!, expiry_month, expiry_year)
    }

}