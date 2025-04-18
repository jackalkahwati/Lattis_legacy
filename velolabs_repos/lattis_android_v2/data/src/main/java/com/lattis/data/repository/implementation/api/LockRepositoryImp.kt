package com.lattis.data.repository.implementation.api

import com.lattis.data.database.store.LockRealmDataStore
import com.lattis.data.entity.body.lock.SignedMessagePublicKeyBody
import com.lattis.data.entity.response.lock.TapkeyAccessResponse
import com.lattis.data.net.lock.LockApiClient
import com.lattis.domain.models.SignedMessageAndPublicKey
import com.lattis.domain.repository.BluetoothRepository
import com.lattis.domain.repository.LockRepository
import com.lattis.domain.models.Lock
import io.lattis.ellipse.sdk.model.KeyCache
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class LockRepositoryImp @Inject constructor(
    val lockRealmDataStore: LockRealmDataStore,
    val bluetoothRepository: BluetoothRepository,
    val lockApiClient: LockApiClient
):LockRepository{

    override fun getSignedMessagePublicKey(
        bike_id: Int,
        mac_id: String
    ): Observable<SignedMessageAndPublicKey> {
        return bluetoothRepository.getKeys(mac_id)
            .observeOn(Schedulers.io())
            .flatMap {keyCaches ->
                if (keyCaches == null || keyCaches?.macId == null || keyCaches?.publicKey == null && keyCaches?.signedMessage == null) {
                     lockApiClient.api.getSignedMessagePublicKey(SignedMessagePublicKeyBody(bike_id))
                        .flatMap{ signedMessagePublicKeyResponse ->
                            val keyCache = KeyCache()
                            keyCache.publicKey =
                                signedMessagePublicKeyResponse.signedMessagePublicKeyPayloadResponse?.public_key
                            keyCache.signedMessage =
                                signedMessagePublicKeyResponse.signedMessagePublicKeyPayloadResponse?.signed_message
                            keyCache.macId = mac_id
                            keyCache.time = Date().time
                            bluetoothRepository.addKeys(com.lattis.domain.models.KeyCache(keyCache.signedMessage,keyCache.publicKey)).flatMap{
                                Observable.just(SignedMessageAndPublicKey(keyCache.signedMessage,keyCache.publicKey))

                            }
                        }
                } else {
                    Observable.just(SignedMessageAndPublicKey(keyCaches.signedMessage,keyCaches.publicKey))
                }
            }
        }





    override fun createOrUpdateLock(lock: Lock): Observable<Lock> {
        return lockRealmDataStore.createOrUpdateLock(lock)
    }

    override fun deleteLock(): Observable<Boolean> {
        return lockRealmDataStore.deleteLock()
    }

    override fun getLock(): Observable<Lock> {
        return lockRealmDataStore.getLock()
    }

    override fun getTapkeyAccess(
        fleet_id: Int,
        mac_id: String
    ): Observable<SignedMessageAndPublicKey> {
        return lockApiClient.api.getTapkeyAccess(mac_id,fleet_id).map {
            SignedMessageAndPublicKey(it.tapkeyAccess?.token,it.tapkeyAccess?.physical_lock_id)
        }
    }
}