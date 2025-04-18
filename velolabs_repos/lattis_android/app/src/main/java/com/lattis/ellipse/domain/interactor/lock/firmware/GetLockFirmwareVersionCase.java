package com.lattis.ellipse.domain.interactor.lock.firmware;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 9/6/17.
 */

public class GetLockFirmwareVersionCase extends BaseLockUseCase<String> {

    private final String TAG = GetLockFirmwareVersionCase.class.getName();

    @Inject
    GetLockFirmwareVersionCase(ThreadExecutor threadExecutor,
                               PostExecutionThread postExecutionThread,
                               BluetoothRepository bluetoothRepository, LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }


    public GetLockFirmwareVersionCase forLock(Lock lock) {
        setScannedLock(lock);
        return this;
    }

    @Override
    protected Observable<String> buildUseCaseObservable() {
        return getLockFirmwareVersion();
    }


}

