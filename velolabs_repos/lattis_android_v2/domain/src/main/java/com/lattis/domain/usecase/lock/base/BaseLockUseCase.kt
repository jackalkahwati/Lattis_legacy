package com.lattis.domain.usecase.lock.base

import android.util.Log
import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Lock
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.repository.SaSOrPSLockRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers

abstract class BaseLockUseCase<ReturnType> protected constructor(
    threadExecutor: ThreadExecutor?,
    postExecutionThread: PostExecutionThread?,
    protected val bluetoothRepository: BluetoothRepository,
    private val lockRepository: LockRepository,
    private val saSOrPSLockRepository: SaSOrPSLockRepository
) : UseCase<ReturnType>(threadExecutor!!, postExecutionThread!!) {
    private var lock: Lock? = null

    private val TAG = BaseLockUseCase::class.java.name
    override fun execute(disposableObserver: DisposableObserver<ReturnType>): Disposable {
        return buildUseCaseObservable()
            .subscribeOn(Schedulers.from(threadExecutor))
            .observeOn(postExecutionThread.scheduler, true)
            .subscribeWith(disposableObserver)
    }

    override fun executeInMainThread(disposableObserver: DisposableObserver<ReturnType>): Disposable {
        return buildUseCaseObservable()
            .observeOn(postExecutionThread.scheduler, true)
            .subscribeWith(disposableObserver)
    }

    protected fun setScannedLock(lock: Lock) {
        this.lock = lock
    }

    fun connectToLock(vendor: LockVendor): Observable<Lock.Connection.Status> {
        return when (vendor) {
            LockVendor.ELLIPSE -> {
                 bluetoothRepository.connectTo(lock!!)
            }

            LockVendor.AXA -> {
                bluetoothRepository.connectToAxa(lock!!)
            }

            LockVendor.TAPKEY -> {
                bluetoothRepository.connectToTapkey(lock!!)
            }
            LockVendor.PSLOCK, LockVendor.SAS->{
                bluetoothRepository.connectToSaSOrPSLock(lock!!,vendor)
            }
        }
    }

    fun connectTo(vendor: LockVendor, lock: Lock): Observable<Lock.Connection.Status> {
        this.lock = lock
        return connectToLock(vendor)
    }

    protected fun setPosition(vendor: LockVendor,locked: Boolean,fleetId:Int?): Observable<Boolean> { //return lockRepository.getLock().flatMap(connectedLock -> bluetoothRepository.setPosition(connectedLock, locked));
        return when(vendor){
            LockVendor.ELLIPSE ->{
                bluetoothRepository.setPosition(lock!!, locked)
            }

            LockVendor.AXA->{
                bluetoothRepository.setPositionForAxa(lock!!,locked)
            }

            LockVendor.TAPKEY->{
                bluetoothRepository.setPositionForTapkey(lock!!,locked)
            }
            LockVendor.PSLOCK, LockVendor.SAS->{
                 bluetoothRepository.getNonceTokenForSasOrPSLock(lock!!,vendor).flatMap { nonce ->
                     val device_id = if(lock?.macId!=null)lock?.macId else if(lock?.macAddress!=null) lock?.macAddress else null
//                     val device_id = "50B0456022"
//                     val device_id = "60DD985F96"
                     if(device_id!=null && fleetId!=null) {

                         return@flatMap saSOrPSLockRepository.getUnlockToken(fleetId,device_id!!, nonce)
                             .flatMap {
                                 lock?.signedMessage = it.saSOrPSLockUnlockToken?.token
                                 bluetoothRepository.setPositionForSaSOrPSLock(
                                     lock!!,
                                     locked,
                                     vendor
                                 )
                             }
                     }else{
                         return@flatMap Observable.just(false)
                     }
                 }
            }
        }
    }

    protected fun lockPositionObservable(vendor: LockVendor): Observable<Lock.Hardware.Position> {

        return when(vendor){
            LockVendor.ELLIPSE ->{
                bluetoothRepository.observePosition(lock!!)
            }

            LockVendor.AXA->{
                bluetoothRepository.observeAxaPosition(lock!!)
            }

            LockVendor.TAPKEY->{
                bluetoothRepository.observeTapkeyPosition(lock!!)
            }
            LockVendor.PSLOCK, LockVendor.SAS->{
                bluetoothRepository.observeSaSOrPSLockPosition(lock!!,vendor)
            }
        }

    }

    protected val connectionObservable: Observable<Lock.Connection.Status>
        protected get() = bluetoothRepository.observeLockConnectionState(lock!!)

    protected fun hardwareStateObservable(vendor: LockVendor): Observable<Lock.Hardware.State>{
        return when(vendor){
            LockVendor.ELLIPSE ->{
                bluetoothRepository.observeHardwareState(lock!!)
            }

            LockVendor.AXA->{
                bluetoothRepository.observeAxaHardwareState(lock!!)
            }

            LockVendor.TAPKEY->{
                bluetoothRepository.observeTapkeyHardwareState(lock!!)
            }
            LockVendor.PSLOCK, LockVendor.SAS->{
                bluetoothRepository.observeSaSOrPSLockHardwareState(lock!!,vendor)
            }
        }
    }

    protected fun connectToLastConnectedLock(vendor: LockVendor): Observable<Lock.Connection.Status> {
        return when(vendor){
            LockVendor.ELLIPSE ->{
                bluetoothRepository.getLastConnectedLock()
            }

            LockVendor.AXA->{
                bluetoothRepository.getLastConnectedAxaLock()
            }

            LockVendor.TAPKEY->{
                bluetoothRepository.getLastConnectedTapkeyLock()
            }

            LockVendor.PSLOCK, LockVendor.SAS->{
                bluetoothRepository.getLastConnectedSaSOrPSLock(vendor)
            }
        }.flatMap { lock: Lock -> connectTo(vendor,lock!!) }
    }

    protected fun disconnectAllLocks(vendor: LockVendor): Observable<Boolean> {
        return return when(vendor){
            LockVendor.ELLIPSE ->{
                bluetoothRepository.disconnectAllLocks()
            }

            LockVendor.AXA->{
                bluetoothRepository.disconnectAllAxaLocks()
            }

            LockVendor.TAPKEY->{
                bluetoothRepository.disconnectAllTapkeyLocks()
            }
            LockVendor.PSLOCK, LockVendor.SAS->{
                bluetoothRepository.disconnectAllSaSOrPSLocks(vendor)
            }
        }
    }

    protected val lockFirmwareVersion: Observable<String>
        protected get() = bluetoothRepository.getLockFirmwareVersion(lock!!)

    protected fun isLockConnected(lock: Lock): Observable<Boolean> {
        return bluetoothRepository.isConnectedTo(lock!!)
    }

}