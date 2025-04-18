package com.lattis.domain.usecase.membership

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Memberships
import com.lattis.domain.repository.MembershipRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetMembershipsAndSubscriptionsUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val membershipRepository: MembershipRepository
) : UseCase<Memberships>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Memberships> {
        return membershipRepository.getMembershipsAndSubscriptions()
    }
}