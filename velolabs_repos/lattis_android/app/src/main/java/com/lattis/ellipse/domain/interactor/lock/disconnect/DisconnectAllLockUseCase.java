package com.lattis.ellipse.domain.interactor.lock.disconnect;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.interactor.lock.connect.ConnectToLockUseCase;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 5/5/17.
 */

public class DisconnectAllLockUseCase extends BaseLockUseCase<Boolean> {

    private final String TAG = ConnectToLockUseCase.class.getName();

    @Inject
    DisconnectAllLockUseCase(ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread,
                         BluetoothRepository bluetoothRepository, LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return disconnectAllLocks();
    }

}
