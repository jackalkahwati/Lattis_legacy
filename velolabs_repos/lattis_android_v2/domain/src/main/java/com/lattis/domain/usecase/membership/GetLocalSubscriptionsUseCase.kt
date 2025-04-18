package com.lattis.domain.usecase.membership

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Memberships
import com.lattis.domain.models.Subscription
import com.lattis.domain.repository.MembershipRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetLocalSubscriptionsUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val membershipRepository: MembershipRepository
) : UseCase<List<Subscription>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<Subscription>> {
        return membershipRepository.getLocalSubscriptions()
    }
}