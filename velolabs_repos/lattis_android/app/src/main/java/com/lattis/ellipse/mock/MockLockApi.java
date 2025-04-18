package com.lattis.ellipse.mock;

import android.content.Context;

import com.lattis.ellipse.data.network.api.LockApi;
import com.lattis.ellipse.data.network.model.body.lock.SignedMessagePublicKeyBody;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;

import retrofit2.http.Body;
import retrofit2.mock.BehaviorDelegate;
import io.reactivex.Observable;

public class MockLockApi implements LockApi {

    private final Context context;
    private final BehaviorDelegate<LockApi> delegate;

    public MockLockApi(Context context, BehaviorDelegate<LockApi> delegate) {
        this.delegate = delegate;
        this.context = context;
    }


    @Override
    public Observable<SignedMessagePublicKeyResponse> getSignedMessagePublicKey(@Body SignedMessagePublicKeyBody signedMessagePublicKeyBody) {
        return null;
    }
}
