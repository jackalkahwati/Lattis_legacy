package com.lattis.ellipse.data;

import com.lattis.ellipse.data.database.LockRealmDataStore;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyPayloadResponse;
import com.lattis.ellipse.data.network.model.response.lock.SignedMessagePublicKeyResponse;
import com.lattis.ellipse.data.network.store.LockNetwordDataStore;
import com.lattis.ellipse.domain.model.Lock;
import com.lattis.ellipse.domain.repository.BluetoothRepository;
import com.lattis.ellipse.domain.repository.LockRepository;

import java.util.Date;

import javax.inject.Inject;

import io.lattis.ellipse.sdk.model.KeyCache;
import io.reactivex.Observable;
import io.reactivex.functions.Function;


public class LockDataRepository implements LockRepository{

    private LockNetwordDataStore lockNetwordDataStore;
    private final LockRealmDataStore lockRealmDataStore;
    private BluetoothRepository bluetoothRepository;


    @Inject
    public LockDataRepository(LockNetwordDataStore lockNetwordDataStore,LockRealmDataStore lockRealmDataStore, BluetoothRepository bluetoothRepository) {
        this.lockNetwordDataStore = lockNetwordDataStore;
        this.lockRealmDataStore = lockRealmDataStore;
        this.bluetoothRepository = bluetoothRepository;
    }

    @Override
    public Observable<SignedMessagePublicKeyResponse> getSignedMessagePublicKey(int bike_id, final String mac_id) {
        return bluetoothRepository.getKeys(mac_id).flatMap(new Function<KeyCache, Observable<SignedMessagePublicKeyResponse>>() {
            @Override
            public Observable<SignedMessagePublicKeyResponse> apply(KeyCache keyCaches) {
                if (keyCaches == null || keyCaches.getMacId()==null ||keyCaches.getPublicKey()==null && keyCaches.getSignedMessage()==null) {
                    return lockNetwordDataStore.getSignedMessagePublicKey(bike_id).flatMap(new Function<SignedMessagePublicKeyResponse, Observable<SignedMessagePublicKeyResponse>>() {
                           @Override
                           public Observable<SignedMessagePublicKeyResponse> apply(SignedMessagePublicKeyResponse signedMessagePublicKeyResponse) {
                               KeyCache keyCache = new KeyCache();
                               keyCache.setPublicKey(signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getPublic_key());
                               keyCache.setSignedMessage(signedMessagePublicKeyResponse.getSignedMessagePublicKeyPayloadResponse().getSigned_message());
                               keyCache.setMacId(mac_id);
                               keyCache.setTime(new Date().getTime());
                               return bluetoothRepository.addKeys(keyCache).flatMap(new Function<Boolean, Observable<SignedMessagePublicKeyResponse>>() {
                                   @Override
                                   public Observable<SignedMessagePublicKeyResponse> apply(Boolean status) {
                                       return Observable.just(signedMessagePublicKeyResponse);
                                   }
                               });
                           }
                       });
                } else {
                    SignedMessagePublicKeyResponse signedMessagePublicKeyResponse = new SignedMessagePublicKeyResponse();
                    SignedMessagePublicKeyPayloadResponse signedMessagePublicKeyPayloadResponse = new SignedMessagePublicKeyPayloadResponse();
                    signedMessagePublicKeyPayloadResponse.setPublic_key(keyCaches.getPublicKey());
                    signedMessagePublicKeyPayloadResponse.setSigned_message(keyCaches.getSignedMessage());
                    signedMessagePublicKeyResponse.setSignedMessagePublicKeyPayloadResponse(signedMessagePublicKeyPayloadResponse);
                    return Observable.just(signedMessagePublicKeyResponse);
                }
            }
        });
    }

    @Override
    public Observable<Lock> createOrUpdateLock(Lock lock) {
        return lockRealmDataStore.createOrUpdateLock(lock);
    }

    @Override
    public Observable<Boolean> deleteLock() {
        return lockRealmDataStore.deleteLock();
    }

    @Override
    public Observable<Lock> getLock() {
        return lockRealmDataStore.getLock();
    }
}
