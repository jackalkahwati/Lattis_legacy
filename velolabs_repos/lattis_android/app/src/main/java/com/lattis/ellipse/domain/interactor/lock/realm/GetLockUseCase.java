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

public class GetLockUseCase extends UseCase<Lock>

{

    private LockRepository lockRepository;

    @Inject
    public GetLockUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread);
        this.lockRepository = lockRepository;
    }

    @Override
    protected Observable<Lock> buildUseCaseObservable() {
        return this.lockRepository.getLock();
    }
}
