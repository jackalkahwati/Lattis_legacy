package com.lattis.ellipse.domain.interactor.lock.connect;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ConnectToLastLockedLockUseCase extends ConnectToLockUseCase {

    @Inject
    ConnectToLastLockedLockUseCase(ThreadExecutor threadExecutor,
                                   PostExecutionThread postExecutionThread,
                                   BluetoothRepository bluetoothRepository,LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }

    @Override
    protected Observable<Lock.Connection.Status> buildUseCaseObservable() {
        return connectToLastConnectedLock();
    }
}
