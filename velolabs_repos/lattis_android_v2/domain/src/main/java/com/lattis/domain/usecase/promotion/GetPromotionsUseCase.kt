package com.lattis.domain.usecase.promotion

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Promotion
import com.lattis.domain.repository.PromotionRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetPromotionsUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val promotionRepository: PromotionRepository
) : UseCase<List<Promotion>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<Promotion>> {
        return promotionRepository.promotions()
    }
}