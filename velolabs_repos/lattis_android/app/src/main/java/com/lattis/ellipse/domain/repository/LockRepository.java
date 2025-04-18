package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.domain.model.Lock;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/27/17.
 */

public interface LockRepository {

    Observable<SignedMessagePublicKeyResponse> getSignedMessagePublicKey(int bike_id, String macId);
    Observable<Lock> createOrUpdateLock(Lock lock);
    Observable<Lock> getLock();
    Observable<Boolean> deleteLock();
}
