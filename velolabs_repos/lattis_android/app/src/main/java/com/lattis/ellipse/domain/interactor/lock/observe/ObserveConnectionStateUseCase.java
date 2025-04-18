package com.lattis.ellipse.domain.interactor.lock.observe;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveConnectionStateUseCase extends BaseLockUseCase<Lock.Connection.Status> {

    @Inject
    ObserveConnectionStateUseCase(ThreadExecutor threadExecutor,
                                  PostExecutionThread postExecutionThread,
                                  BluetoothRepository bluetoothRepository,LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }

    public ObserveConnectionStateUseCase forLock(Lock lock) {
        setScannedLock(lock);
        return this;
    }

    @Override
    protected Observable<Lock.Connection.Status> buildUseCaseObservable() {
        return getConnectionObservable();
    }
}
