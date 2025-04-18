package com.lattis.ellipse.domain.interactor.lock.setter;

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

public class SetLockPositionUseCase extends BaseLockUseCase<Boolean> {

    private boolean locked;

    @Inject
    SetLockPositionUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           BluetoothRepository bluetoothRepository,LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread, bluetoothRepository,lockRepository);
    }

    public SetLockPositionUseCase withState(boolean locked) {
        this.locked = locked;
        return this;
    }

    public SetLockPositionUseCase forLock(Lock lock) {
        setScannedLock(lock);
        return this;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return setPosition(locked);
    }


}
