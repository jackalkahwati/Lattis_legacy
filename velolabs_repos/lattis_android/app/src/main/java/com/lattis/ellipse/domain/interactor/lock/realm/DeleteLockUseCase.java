package com.lattis.ellipse.domain.interactor.lock.realm;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 7/25/17.
 */

public class DeleteLockUseCase extends UseCase<Boolean>

{

    private LockRepository lockRepository;

    @Inject
    public DeleteLockUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread);
        this.lockRepository = lockRepository;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.lockRepository.deleteLock();
    }
}

