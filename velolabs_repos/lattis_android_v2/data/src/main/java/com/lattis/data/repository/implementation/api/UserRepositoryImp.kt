package com.lattis.data.repository.implementation.api


import com.lattis.data.database.store.MediaRealmDataStore
import com.lattis.data.database.store.PrivateNetworkRealmDataStore
import com.lattis.data.database.store.UserNetworkDataStore
import com.lattis.data.database.store.UserRealmDataStore
import com.lattis.domain.models.*
import com.lattis.domain.repository.UserRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    private val userNetworkDataStore: UserNetworkDataStore,
    private var userRealmDataStore: UserRealmDataStore,
    private val privateNetworkRealmDataStore: PrivateNetworkRealmDataStore,
    private val mediaRealmDataStore: MediaRealmDataStore
    ) : UserRepository {

    override fun getUserCurrentStatus(): Observable<UserCurrentStatus> {
        return userNetworkDataStore.getUserCurrentStatus()
    }


    override fun getUser(): Observable<User> {
        return userNetworkDataStore.user
                    .flatMap{ user ->
                        val mediaId = String.format("m:u:%s:pp", user.id)
                        mediaRealmDataStore.getMedia(mediaId)
                            .flatMap { media ->
                                user.imageUri = media.url
                                Observable.just(user)
                            }
                            .onErrorResumeNext { throwable:Throwable -> Observable.just(user) }
                    }
                    .flatMap { this.saveUserLocally(it) }
                    .onErrorResumeNext {
                            throwable:Throwable ->
                        Observable.mergeDelayError(
                            userRealmDataStore.user,
                            Observable.error<User>(throwable))
                    }

    }

    override fun getLocalUser(): Observable<User> {
        return userRealmDataStore.user
            .flatMap { user->
                if(user==null || user.id==null || user.firstName==null)
                    getUser()
                else
                    Observable.just(user)
            }
            .onErrorResumeNext { throwble:Throwable->
                getUser()
            }
    }

    override fun saveUserLocally(user: User): Observable<User> {
        return userRealmDataStore.createOrUpdateUser(user).flatMap { user1 ->
            if (user.privateNetworks != null)
                user1.privateNetworks = user.privateNetworks
            Observable.just(user1)
        }
    }

    override fun saveUser(user: User): Observable<User> {
        if (user.imageUri != null) {
            val mediaId = String.format("m:u:%s:pp", user.id)
            val media = Media(mediaId, user?.imageUri)
            return mediaRealmDataStore.createOrUpdateUser(media)
                .flatMap {
                    //savePrivateNetworks(user);
                    userNetworkDataStore.saveUser(user)
                        .flatMap { user1 -> saveUserLocally(user1) }
                }

        } else {
            return userNetworkDataStore.saveUser(user).flatMap { this.saveUserLocally(it) }
        }
    }

    override fun confirmVerificationCodeForPrivateNetwork(
        userId: String,
        account_type: String,
        confirmationCode: String
    ): Observable<Boolean> {
        return userNetworkDataStore.confirmVerificationCodeForPrivateNetwork(
            userId,
            account_type,
            confirmationCode
        )
    }

    override fun addPrivateNetworkEmail(email: String): Observable<List<PrivateNetwork>> {
        return userNetworkDataStore.addPrivateNetworkEmail(email).map {addPrivateNetworkResponse ->
            if(addPrivateNetworkResponse!=null &&
                addPrivateNetworkResponse?.addPrivateNetworkDataResponse!=null &&
                    addPrivateNetworkResponse?.addPrivateNetworkDataResponse?.lattis_accounts!=null){
                addPrivateNetworkResponse?.addPrivateNetworkDataResponse?.lattis_accounts!!
            }else{
                emptyList()
            }
        }
    }

    override fun sendCodeToUpdatePhoneNumber(
        countryCode: String,
        phoneNumber: String
    ): Observable<Boolean> {
        return userNetworkDataStore.sendCodeToUpdatePhoneNumber(countryCode,phoneNumber)
    }

    override fun validateCodeForChangePhoneNumber(
        code: String,
        phoneNumber: String
    ): Observable<Boolean> {
        return userNetworkDataStore.validateCodeForChangePhoneNumber(code,phoneNumber)
    }


    override fun changePassword(
        password: String,
        newPassword: String
    ): Observable<Boolean> {
        return userNetworkDataStore.changePassword(password, newPassword)
    }

    override fun validateCodeForEmailChange(code: String, email: String): Observable<Boolean> {
        return userNetworkDataStore.validateCodeForEmailChange(code,email)
    }

    override fun sendCodeToUpdateEmail(email: String): Observable<Boolean> {
        return userNetworkDataStore.sendCodeForUpdateEmail(email!!)
    }

    override fun sendForgotPasswordCode(email: String): Observable<Boolean> {
        return userNetworkDataStore.sendForgotPasswordCode(email)
    }

    override fun confirmCodeForForgotPassword(
        email: String,
        code: String,
        password: String
    ): Observable<Boolean> {
        return userNetworkDataStore.confirmCodeForForgotPassword(email!!, code!!, password!!)
    }

    override fun getUserFleet():Observable<List<Bike.Fleet>>{
        return userNetworkDataStore.getFleets()
    }

    override fun deleteAccount():Observable<Boolean>{
        return userNetworkDataStore.deleteAccount()
    }
}