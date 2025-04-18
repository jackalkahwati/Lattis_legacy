package com.lattis.ellipse.domain.interactor.lock.observe;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/27/17.
 */

public class ObserveLockPositionUseCase extends BaseLockUseCase<Lock.Hardware.Position> {

    @Inject
    ObserveLockPositionUseCase(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               BluetoothRepository bluetoothRepository,
                               LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread, bluetoothRepository,lockRepository);
    }

    public ObserveLockPositionUseCase forLock(Lock lock) {
        setScannedLock(lock);
        return this;
    }

    @Override
    protected Observable<Lock.Hardware.Position> buildUseCaseObservable() {
        return getLockPositionObservable();
    }


}
