package com.lattis.domain.usecase.user

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.UserRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.PrivateNetwork
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class AddPrivateNetworkUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val userRepository: UserRepository
) : UseCase<List<PrivateNetwork>>(threadExecutor, postExecutionThread) {
    private var email: String? = null
    fun withEmail(email: String?): AddPrivateNetworkUseCase {
        this.email = email
        return this
    }

    override fun buildUseCaseObservable(): Observable<List<PrivateNetwork>> {
        return userRepository.addPrivateNetworkEmail(email!!)
    }

}