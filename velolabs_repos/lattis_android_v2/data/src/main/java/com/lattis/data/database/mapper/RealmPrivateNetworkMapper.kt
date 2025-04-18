package com.lattis.data.database.mapper


import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmPrivateNetwork
import com.lattis.domain.models.PrivateNetwork
import java.util.ArrayList

import javax.inject.Inject

import io.realm.RealmResults

/**
 * Created by ssd3 on 5/8/17.
 */

class RealmPrivateNetworkMapper @Inject
constructor() : AbstractRealmDataMapper<PrivateNetwork, RealmPrivateNetwork>() {

    override fun mapIn(privateNetwork: PrivateNetwork): RealmPrivateNetwork {
        val realmPrivateNetwork = RealmPrivateNetwork()
        realmPrivateNetwork.private_fleet_user_id = privateNetwork.private_fleet_user_id
        realmPrivateNetwork.userId = privateNetwork.user_id
        realmPrivateNetwork.email = privateNetwork.email
        realmPrivateNetwork.fleet_id = privateNetwork.fleet_id
        realmPrivateNetwork.verified = privateNetwork.verified
        realmPrivateNetwork.fleet_name = privateNetwork.fleet_name
        realmPrivateNetwork.type = privateNetwork.type
        realmPrivateNetwork.logo = privateNetwork.logo
        return realmPrivateNetwork
    }

    override fun mapOut(realmPrivateNetwork: RealmPrivateNetwork): PrivateNetwork {
        val privateNetwork = PrivateNetwork()
        privateNetwork.private_fleet_user_id = realmPrivateNetwork.private_fleet_user_id
        privateNetwork.user_id = realmPrivateNetwork.userId
        privateNetwork.email = realmPrivateNetwork.email
        privateNetwork.fleet_id = realmPrivateNetwork.fleet_id
        privateNetwork.verified = realmPrivateNetwork.verified
        privateNetwork.fleet_name = realmPrivateNetwork.fleet_name
        privateNetwork.type = realmPrivateNetwork.type
        privateNetwork.logo = realmPrivateNetwork.logo
        return privateNetwork
    }


    override fun mapOut(realmPrivateNetworks: RealmResults<RealmPrivateNetwork>): List<PrivateNetwork> {
        val privateNetworks = ArrayList<PrivateNetwork>()
        for (realmPrivateNetwork in realmPrivateNetworks) {
            privateNetworks.add(mapOut(realmPrivateNetwork))
        }
        return privateNetworks
    }
}
