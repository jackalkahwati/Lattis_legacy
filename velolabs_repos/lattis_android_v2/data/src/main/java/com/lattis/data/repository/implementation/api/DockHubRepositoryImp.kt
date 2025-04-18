package com.lattis.data.repository.implementation.api

import com.lattis.data.entity.body.dockhub.UndockBody
import com.lattis.data.net.dockhub.DockHubApiClient
import com.lattis.domain.repository.DockHubRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DockHubRepositoryImp @Inject constructor(
    val dockHubApiClient: DockHubApiClient
) : DockHubRepository{
    override fun undock(uuid: String,hub_type:String): Observable<Boolean> {
        return dockHubApiClient.api.undock(uuid, UndockBody( hub_type))
            .map {
                true
            }
    }
}