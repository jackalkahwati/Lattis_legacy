package com.lattis.ellipse.domain.interactor.lock.scanner;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.ScannedLock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/26/17.
 */

public class ScanForLockUseCase extends BaseLockUseCase<ScannedLock> {

    @Inject
    ScanForLockUseCase(ThreadExecutor threadExecutor,
                       PostExecutionThread postExecutionThread,
                       BluetoothRepository bluetoothRepository,
                       LockRepository lockRepository
                       ) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }

    @Override
    protected Observable<ScannedLock> buildUseCaseObservable() {
        return getBluetoothRepository().startScan(35000);
    }
}
