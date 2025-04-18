package com.lattis.domain.usecase.card

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.CardRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import org.json.JSONObject
import javax.inject.Inject

class AddCardUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val cardRepository: CardRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var cardNumber: String? = null
    private var expiry_month = 0
    private var expiry_year = 0
    private var cvc: String? = null
    private var intent: JSONObject? = null
    fun withCardNumber(cardNumber: String?): AddCardUseCase {
        this.cardNumber = cardNumber
        return this
    }

    fun withExpiryMonth(expiry_month: Int): AddCardUseCase {
        this.expiry_month = expiry_month
        return this
    }

    fun withExpiryYear(expiry_year: Int): AddCardUseCase {
        this.expiry_year = expiry_year
        return this
    }

    fun withCVC(cvc: String): AddCardUseCase {
        this.cvc = cvc
        return this
    }

    fun withIntent(intent: JSONObject?): AddCardUseCase {
        this.intent = intent
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return cardRepository.addCard(cardNumber!!, expiry_month, expiry_year, cvc!!, intent!!)
    }

}