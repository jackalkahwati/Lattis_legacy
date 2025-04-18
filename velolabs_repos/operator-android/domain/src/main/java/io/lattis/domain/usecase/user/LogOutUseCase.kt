package io.lattis.domain.usecase.user

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.AccountRepository
import io.reactivex.functions.Function
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import javax.inject.Inject

class LogOutUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val accountRepository: AccountRepository

) : UseCase<Boolean>(threadExecutor, postExecutionThread) {


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return accountRepository.signOut()
            .flatMap(deleteSharedPreference)
    }


    private val deleteSharedPreference: io.reactivex.functions.Function<Boolean, Observable<Boolean>> =
        Function<Boolean, Observable<Boolean>> {
            Observable.just(true)

        }
}