package com.lattis.data.mapper



import com.lattis.data.entity.response.user.GetUserResponse
import com.lattis.domain.models.PrivateNetwork
import com.lattis.domain.models.User

import java.util.ArrayList

import javax.inject.Inject

class UserMapper @Inject
constructor() : AbstractDataMapper<GetUserResponse, User>() {

    override fun mapIn(getUserResponse: GetUserResponse?): User {
        val user = User()

        if(getUserResponse!=null) {
            val userResponse = getUserResponse.getUserPayload()
            user.id = userResponse?.userId
            user.usersId = userResponse?.usersId
            user.username = userResponse?.username
            user.isVerified = userResponse?.isVerified
            user.maxLocks = userResponse?.maxLocks?:0
            user.title = userResponse?.title
            user.firstName = userResponse?.firstName
            user.lastName = userResponse?.lastName
            user.phoneNumber = userResponse?.phoneNumber
            user.email = userResponse?.email
            user.restToken = userResponse?.restToken
            user.refreshToken = userResponse?.refreshToken
            user.userType = userResponse?.userType

            val privateNetworksResponse = getUserResponse.privateNetworkResponse
            val privateNetworks = ArrayList<PrivateNetwork>()
            if (privateNetworksResponse != null) {
                for (privateNetworkResponse in privateNetworksResponse) {
                    if (privateNetworkResponse != null) {
                        val privateNetwork = PrivateNetwork()
                        privateNetwork.private_fleet_user_id = privateNetworkResponse.private_fleet_user_id
                        privateNetwork.user_id = privateNetworkResponse.user_id
                        privateNetwork.email = privateNetworkResponse.email
                        privateNetwork.fleet_id = privateNetworkResponse.fleet_id
                        privateNetwork.verified = privateNetworkResponse.verified
                        privateNetwork.fleet_name = privateNetworkResponse.fleet_name
                        privateNetwork.type = privateNetworkResponse.type
                        privateNetwork.logo = privateNetworkResponse.logo

                        if(privateNetworkResponse.address!=null){
                            val address = PrivateNetwork.Address()
                            address?.address_id = privateNetworkResponse.address.address_id
                            address?.address1 = privateNetworkResponse.address.address1
                            address?.address2 = privateNetworkResponse.address.address2
                            address?.city = privateNetworkResponse.address.city
                            address?.country = privateNetworkResponse.address.country
                            address?.postal_code = privateNetworkResponse.address.postal_code
                            address?.state = privateNetworkResponse.address.postal_code
                            address?.type = privateNetworkResponse.address.type
                            address?.type_id = privateNetworkResponse.address.type_id
                            privateNetwork.address = address
                        }
                        privateNetworks.add(privateNetwork)

                    }
                }
            }

            user.privateNetworks = privateNetworks
        }

        return user
    }

    override fun mapOut(user: User?): GetUserResponse? {
        return null
    }
}
