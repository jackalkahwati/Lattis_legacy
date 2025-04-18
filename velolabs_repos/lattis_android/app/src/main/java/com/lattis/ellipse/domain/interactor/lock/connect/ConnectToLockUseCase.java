package com.lattis.ellipse.domain.interactor.lock.connect;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.lock.base.BaseLockUseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class ConnectToLockUseCase extends BaseLockUseCase<Lock.Connection.Status> {

    private final String TAG = ConnectToLockUseCase.class.getName();

    @Inject
    ConnectToLockUseCase(ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread,
                         BluetoothRepository bluetoothRepository,LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread,bluetoothRepository,lockRepository);
    }


    @Override
    protected Observable<Lock.Connection.Status> buildUseCaseObservable() {
        return super.connectToLock();
    }

    public Disposable execute(Lock lock, DisposableObserver<Lock.Connection.Status> useCaseSubscriber){
        this.setScannedLock(lock);
        return super.execute(useCaseSubscriber);
    }
}
