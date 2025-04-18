package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmUser
import com.lattis.domain.models.User
import javax.inject.Inject

class RealmUserMapper @Inject constructor() :
    AbstractRealmDataMapper<User, RealmUser>() {
    override fun mapIn(user: User): RealmUser {
        val realmUser = RealmUser()
        realmUser.id = user.id
        realmUser.firstName=user.firstName
        realmUser.lastName = (user.lastName)
        realmUser.email = (user.email)
        realmUser.phoneNumber=(user.phoneNumber)
        realmUser.registrationId =(user.registrationId)
        realmUser.isUsingFacebook = (user.isUsingFacebook)
        realmUser.imageURI=(user.imageUri)
        return realmUser
    }

    override fun mapOut(realmUser: RealmUser): User {
        val user = User()
        user.id = realmUser.id
        user.firstName=realmUser.firstName
        user.lastName = (realmUser.lastName)
        user.email = (realmUser.email)
        user.phoneNumber=(realmUser.phoneNumber)
        user.registrationId =(realmUser.registrationId)
        user.isUsingFacebook = (realmUser.isUsingFacebook)
        user.imageUri= realmUser.imageURI
        return user
    }
}