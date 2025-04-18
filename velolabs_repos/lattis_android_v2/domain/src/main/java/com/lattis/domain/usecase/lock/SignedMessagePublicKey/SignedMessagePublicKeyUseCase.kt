package com.lattis.domain.usecase.lock.SignedMessagePublicKey

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.SignedMessageAndPublicKey
import com.lattis.domain.repository.AxaLockRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.usecase.lock.connect.ConnectToLockUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SignedMessagePublicKeyUseCase @Inject
constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val lockRepository: LockRepository,
    private val axaLockRepository: AxaLockRepository
) : UseCase<SignedMessageAndPublicKey>(threadExecutor, postExecutionThread) {
    private var bike_id = 0
    private lateinit var macId: String
    private var fleetId:Int =0

    fun withLockVendor(vendor:LockVendor): SignedMessagePublicKeyUseCase {
        this.lockVendor = vendor
        return this
    }


    fun withBikeId(bike_id: Int): SignedMessagePublicKeyUseCase {
        this.bike_id = bike_id
        return this
    }

    fun withMacId(macId: String): SignedMessagePublicKeyUseCase {
        this.macId = macId
        return this
    }

    fun withFleetId(fleetId: Int): SignedMessagePublicKeyUseCase {
        this.fleetId = fleetId
        return this
    }

    override fun buildUseCaseObservable(): Observable<SignedMessageAndPublicKey> {
        return when(lockVendor){
            LockVendor.ELLIPSE->{
                lockRepository.getSignedMessagePublicKey(bike_id, macId!!)
            }
            LockVendor.AXA->{
                axaLockRepository.getAxaKey(macId.replace("AXA:","")).map {
                    SignedMessageAndPublicKey(it.ekey,it.passkey)
                }
            }

            LockVendor.TAPKEY->{
                lockRepository.getTapkeyAccess(fleetId, macId!!)
            }

            LockVendor.PSLOCK,LockVendor.SAS -> {
                Observable.create<SignedMessageAndPublicKey> { emitter ->
                    Observable.timer(500,TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{emitter.onNext(SignedMessageAndPublicKey("",""))}
                }
            }
        }
    }

}