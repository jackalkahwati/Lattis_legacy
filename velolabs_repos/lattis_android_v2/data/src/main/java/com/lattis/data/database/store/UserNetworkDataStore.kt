package com.lattis.data.database.store

import com.lattis.data.entity.body.authentication.VerificationCodeBody
import com.lattis.data.entity.body.user.*
import com.lattis.data.entity.response.user.AddPrivateNetworkResponse
import com.lattis.data.mapper.UserMapper
import com.lattis.data.net.user.UserApiClient
import com.lattis.domain.models.Bike
import com.lattis.domain.models.UserCurrentStatus
import com.lattis.domain.models.User
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Named

class UserNetworkDataStore @Inject constructor(
    private val userApiClient: UserApiClient,
    private val userMapper: UserMapper,
    @param:Named("DeviceModel") private val deviceModel: String,
    @param:Named("DeviceOS") private val deviceOS: String,
    @param:Named("DeviceLanguage") private val deviceLanguage: String
){

    fun getUserCurrentStatus(): Observable<UserCurrentStatus> {
        return userApiClient.api.getCurrentUserStatus(GetUserCurrentStatusBody(deviceModel, deviceOS,deviceLanguage))
            .map{
                var activeBooking:UserCurrentStatus.ActiveBooking?=null
                if(it!=null && it.currentUserActiveBookingStatusResponse!=null){
                    activeBooking = UserCurrentStatus.ActiveBooking(
                        it.currentUserActiveBookingStatusResponse?.bike_id,
                        it.currentUserActiveBookingStatusResponse?.port_id,
                        it.currentUserActiveBookingStatusResponse?.hub_id,
                        it.currentUserActiveBookingStatusResponse?.device_type,
                        it.currentUserActiveBookingStatusResponse?.booking_id,
                        it.currentUserActiveBookingStatusResponse?.booked_on,
                        it.currentUserActiveBookingStatusResponse?.till
                    )
                }else if(it!=null && it.currentUserStatusTripResponse!=null){

                }
                UserCurrentStatus(it.supportPhone,
                    it.onCallOperator,
                    it.currentUserStatusTripResponse?.trip_id,activeBooking,
                    it.getCurrentUserStatusPayloadResponse?.reservation,
                    it.getCurrentUserStatusPayloadResponse?.vehicle,
                    it.getCurrentUserStatusPayloadResponse?.dockHub
                )
        }
    }

    val user: Observable<User>
        get() = userApiClient.api.user
            .map { getUserResponse -> userMapper.mapIn(getUserResponse) }

    fun saveUser(user: User): Observable<User> {
        return userApiClient.api.saveUser(
                UpdateUserBody(
                UserBody(user.id, user.firstName, user.lastName, user.email)
                )
            )
            .map { getUserResponse -> userMapper.mapIn(getUserResponse) }
    }


    fun confirmVerificationCodeForPrivateNetwork(
        userId: String,
        account_type: String,
        confirmationCode: String
    ): Observable<Boolean> {
        return userApiClient.api.confirmVerificationCodeForPrivateNetwork(
            VerificationCodeBody(
                userId,
                account_type,
                confirmationCode
            )
        )
            .flatMap({ aVoid -> Observable.just(true) })
    }

    fun addPrivateNetworkEmail(email: String): Observable<AddPrivateNetworkResponse> {
        return userApiClient.api.addPrivateNetworkEmail(UpdateEmailCodeBody(email))
    }

    fun sendCodeToUpdatePhoneNumber(
        countryCode: String,
        phoneNumber: String
    ): Observable<Boolean> {
        return userApiClient.api.sendCodeForPhoneNumber(
            SendCodeUpdatePhoneNumberBody(
                countryCode,
                phoneNumber
            )
        )
            .flatMap({ aVoid -> Observable.just(true) })
    }

    fun validateCodeForChangePhoneNumber(
        code: String,
        phoneNumber: String
    ): Observable<Boolean> {
        return userApiClient.api.validateCodeForChangePhoneNumber(
            ValidateCodeForChangePhoneNumberBody(
                code,
                phoneNumber
            )
        )
            .flatMap({ aVoid -> Observable.just(true) })
    }

    fun changePassword(
        password: String,
        newPassword: String
    ): Observable<Boolean> {
        return userApiClient.api.changePassword(ChangePasswordBody(password, newPassword))
            .flatMap{aVoid -> Observable.just(true) }
    }

    fun validateCodeForEmailChange(
        code: String,
        email: String
    ): Observable<Boolean> {
        return userApiClient.api.validateCodeForChangeMail(ValidateCodeUpdateEmailBody(code, email))
            .flatMap{ aVoid -> Observable.just(true) }
    }

    fun sendCodeForUpdateEmail(email: String): Observable<Boolean> {
        return userApiClient.api.sendCodeForUpdateEmail(UpdateEmailCodeBody(email!!))
            .flatMap({ aVoid -> Observable.just(true) })
    }

    fun sendForgotPasswordCode(email: String): Observable<Boolean> {
        return userApiClient.api.sendForgotPasswordCode(SendForgotPasswordCodeBody(email)).
                map{
                    true
                }
    }

    fun confirmCodeForForgotPassword(
        email: String,
        code: String,
        password: String
    ): Observable<Boolean>{
        return userApiClient.api.confirmCodeForForgotPassword(
            ConfirmCodeForForgotPasswordBody(
                email!!,
                code!!,
                password!!
            )
        ).map{
            true
        }
    }

    fun getFleets():Observable<List<Bike.Fleet>>{
        return userApiClient.api.getUserFleet()
            .map {
                it.fleets
            }
    }

    fun deleteAccount():Observable<Boolean>{
        return userApiClient.api.deleteAccount()
            .map {
                true
            }
    }

}