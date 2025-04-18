package com.lattis.domain.usecase.membership

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Memberships
import com.lattis.domain.repository.MembershipRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UnSubscribeToMembershipUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val membershipRepository: MembershipRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var membership_id:Int?=null

    fun withMembershipId(membership_id:Int):UnSubscribeToMembershipUseCase{
        this.membership_id = membership_id
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return membershipRepository.unsubscribe(membership_id!!)
    }
}

