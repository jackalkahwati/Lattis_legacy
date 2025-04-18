package com.lattis.domain.repository

import io.reactivex.rxjava3.core.Observable

interface DockHubRepository {
    fun undock(uuid:String,hub_type:String): Observable<Boolean>
}