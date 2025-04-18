package com.lattis.ellipse.data.network.store;

import com.lattis.ellipse.data.network.api.LockApi;
import com.lattis.ellipse.data.network.model.body.lock.SignedMessagePublicKeyBody;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by ssd3 on 4/27/17.
 */

public class LockNetwordDataStore {

    private LockApi lockApi;

    @Inject
    public LockNetwordDataStore(LockApi lockApi) {
        this.lockApi = lockApi;
    }

    public Observable<SignedMessagePublicKeyResponse> getSignedMessagePublicKey(int bike_id){
        return this.lockApi.getSignedMessagePublicKey(new SignedMessagePublicKeyBody(bike_id));
    }
}
