package com.lattis.domain.usecase.logout

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.AccountRepository
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.DataBaseRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function
import javax.inject.Inject
import javax.inject.Named

class LogOutUseCase @Inject internal constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val accountRepository: AccountRepository,
    private val bluetoothRepository: BluetoothRepository,
    private val mDataBaseRepository: DataBaseRepository

) : UseCase<Boolean>(threadExecutor, postExecutionThread) {


    override fun buildUseCaseObservable(): Observable<Boolean> {
        return accountRepository.signOut()
            .flatMap(deleteDatabase)
            .flatMap(disconnectAllLocks)
            .flatMap(deleteSharedPreference)
    }

    private val deleteDatabase: Function<Boolean, Observable<Boolean>> =
            Function<Boolean, Observable<Boolean>> {
                mDataBaseRepository.deleteDataBase()
        }
    private val disconnectAllLocks: Function<Boolean, Observable<Boolean>> =
            Function<Boolean, Observable<Boolean>> {
                bluetoothRepository.disconnectAllLocks()
        }


    private val deleteSharedPreference: Function<Boolean, Observable<Boolean>> =
            Function<Boolean, Observable<Boolean>> {
                Observable.just(true)

        }



}