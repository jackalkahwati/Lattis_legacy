package com.lattis.data.repository.implementation.api

import com.lattis.data.net.apps.AppsApiClient
import com.lattis.domain.models.Help
import com.lattis.domain.repository.AppsRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class AppsRepositoryImp @Inject constructor(
    val appsApiClient: AppsApiClient
):AppsRepository {
    override fun getHelpInfo(): Observable<Help>{
        return appsApiClient.api.getHelpInfo()
            .map {
                it.help
            }
    }
}