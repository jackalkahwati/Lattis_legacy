package com.lattis.ellipse.domain.interactor.lock.base;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ssd3 on 4/26/17.
 */

public abstract class BaseLockUseCase<ReturnType> extends UseCase<ReturnType> {

    private final BluetoothRepository bluetoothRepository;
    private final LockRepository lockRepository;
    private String lockId;
    private Lock lock;
    private final String TAG = BaseLockUseCase.class.getName();

    protected BaseLockUseCase(ThreadExecutor threadExecutor,
                              PostExecutionThread postExecutionThread,
                              BluetoothRepository bluetoothRepository,
                              LockRepository lockRepository
                              ) {
        super(threadExecutor, postExecutionThread);
        this.bluetoothRepository = bluetoothRepository;
        this.lockRepository=lockRepository;
    }


    @Override
    public Disposable execute(DisposableObserver<ReturnType> disposableObserver) {
        return this.buildUseCaseObservable()
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.getScheduler(), true)
                .subscribeWith(disposableObserver);
    }

    @Override
    public Disposable executeInMainThread(DisposableObserver<ReturnType> disposableObserver) {
        return this.buildUseCaseObservable()
                .observeOn(postExecutionThread.getScheduler(), true)
                .subscribeWith(disposableObserver);
    }

    protected BluetoothRepository getBluetoothRepository() {
        return bluetoothRepository;
    }

    protected void setScannedLock(Lock lock) {
        this.lock = lock;
    }



    public Observable<Lock.Connection.Status> connectToLock(){
        this.lockId = lock.getLockId();
        return bluetoothRepository.connectTo(lock);
    }


    public Observable<Lock.Connection.Status> connectTo(Lock lock){
        this.lock = lock;
        return bluetoothRepository.connectTo(lock);
    }

    protected Observable<Boolean> setPosition(boolean locked) {
        //return lockRepository.getLock().flatMap(connectedLock -> bluetoothRepository.setPosition(connectedLock, locked));
        return bluetoothRepository.setPosition(lock, locked);
    }

    protected Observable<Lock.Hardware.Position> getLockPositionObservable() {
        return bluetoothRepository.observePosition(lock);
    }

    protected Observable<Lock.Connection.Status> getConnectionObservable() {
        return bluetoothRepository.observeLockConnectionState((lock));
    }

    protected Observable<Lock.Hardware.State> getHardwareStateObservable() {
        return bluetoothRepository.observeHardwareState(lock);
    }


    protected Observable<Lock.Connection.Status> connectToLastConnectedLock(){
        return bluetoothRepository.getLastConnectedLock()
//                .onErrorResumeNext(throwable -> lockRepository.getLock())
                .flatMap(this::connectTo);
    }

    protected Observable<Boolean> disconnectAllLocks(){
        return bluetoothRepository.disconnectAllLocks();
    }

    protected  Observable<String> getLockFirmwareVersion(){
        return bluetoothRepository.getLockFirmwareVersion(lock);
    }

    protected  Observable<Boolean> isLockConnected(Lock lock){
        return bluetoothRepository.isConnectedTo(lock);
    }


}
