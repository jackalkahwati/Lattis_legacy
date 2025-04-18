package com.lattis.ellipse.data.network.api;

import com.lattis.ellipse.data.network.model.body.lock.SignedMessagePublicKeyBody;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import io.reactivex.Observable;

public interface LockApi {


    @POST("locks/signed-message-and-public-key-for-trip")
    Observable<SignedMessagePublicKeyResponse> getSignedMessagePublicKey(@Body SignedMessagePublicKeyBody signedMessagePublicKeyBody);


}
