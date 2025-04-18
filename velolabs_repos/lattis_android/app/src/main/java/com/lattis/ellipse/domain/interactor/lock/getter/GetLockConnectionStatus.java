package com.lattis.ellipse.domain.interactor.lock.getter;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetLockConnectionStatus extends BaseLockUseCase<Boolean> {

    private Lock lock;

    @Inject
    GetLockConnectionStatus(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           BluetoothRepository bluetoothRepository, LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }


    public GetLockConnectionStatus forLock(Lock lock) {
        this.lock = lock;
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return isLockConnected(lock);
    }
}
