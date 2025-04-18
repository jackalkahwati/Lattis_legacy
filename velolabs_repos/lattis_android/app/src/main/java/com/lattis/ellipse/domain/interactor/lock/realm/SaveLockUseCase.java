package com.lattis.ellipse.domain.interactor.lock.realm;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/28/17.
 */

public class SaveLockUseCase extends UseCase<Lock>

{

    private LockRepository lockRepository;
    private Lock lock;

    @Inject
    public SaveLockUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread);
        this.lockRepository = lockRepository;
    }

    public SaveLockUseCase withLock(Lock lock) {
        this.lock = lock;
        return this;
    }


    @Override
    protected Observable<Lock> buildUseCaseObservable() {
        return this.lockRepository.createOrUpdateLock(lock);
    }
}

