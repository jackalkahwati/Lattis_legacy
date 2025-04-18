package io.lattis.domain.usecase.authentication



import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.models.User
import io.lattis.domain.repository.AccountRepository
import io.lattis.domain.repository.Authenticator
import io.lattis.domain.usecase.base.UseCase
import javax.inject.Inject
import io.reactivex.Observable

class SignInUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val authenticator: Authenticator,
            private val accountRepository: AccountRepository
            ) : UseCase<User>(threadExecutor, postExecutionThread) {

    private var email: String? = null
    private var password: String? = null

    fun withValues(email: String,
                   password: String): SignInUseCase {
        this.email = email
        this.password = password
        return this
    }

    override fun buildUseCaseObservable(): Observable<User> {
        return trySignIn()
    }

    protected fun trySignIn(): Observable<User> {
            return authenticator.signIn(
                    email,
                    password,
                    )
    }
}