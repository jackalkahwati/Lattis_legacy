package com.lattis.ellipse.domain.interactor.lock.SignedMessagePublicKey;

import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.repository.LockRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/27/17.
 */

public class SignedMessagePublicKeyUseCase extends UseCase<SignedMessagePublicKeyResponse> {

    private LockRepository lockRepository;
    private int bike_id;
    private String macId;


    @Inject
    public SignedMessagePublicKeyUseCase(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread,
                                         LockRepository lockRepository) {
        super(threadExecutor, postExecutionThread);
        this.lockRepository = lockRepository;
    }


    public SignedMessagePublicKeyUseCase withBikeId(int bike_id) {
        this.bike_id = bike_id;
        return this;
    }

    public SignedMessagePublicKeyUseCase withMacId(String macId) {
        this.macId = macId;
        return this;
    }



    @Override
    protected Observable<SignedMessagePublicKeyResponse> buildUseCaseObservable() {
        return lockRepository.getSignedMessagePublicKey(bike_id,macId);
    }

}
