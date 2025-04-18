package com.lattis.data.database.store


import com.lattis.data.database.base.RealmObservable
import com.lattis.data.database.mapper.RealmMediaMapper
import com.lattis.data.database.model.RealmMedia
import com.lattis.domain.models.Media
import javax.inject.Inject

import io.reactivex.rxjava3.core.Observable
import io.realm.RealmConfiguration
import io.reactivex.rxjava3.functions.Function

/**
 * Created by raverat on 4/6/17.
 */

class MediaRealmDataStore @Inject
constructor(private val mediaMapper: RealmMediaMapper) {

    fun getMedia(id: String): Observable<Media> {
        return RealmObservable.`object`<RealmMedia>(
                Function { realm ->
                    val realmMedia = realm.where(RealmMedia::class.java).equalTo("id", id).findFirst()
                    if (realmMedia != null) realmMedia else RealmMedia()
                })
                .map { realmMedia -> mediaMapper.mapOut(realmMedia) }
    }

    fun createOrUpdateUser(media: Media): Observable<Media> {
        return RealmObservable.`object`<RealmMedia>(
                Function{ realm -> realm.copyToRealmOrUpdate(mediaMapper.mapIn(media)) })
                .map { realmMedia -> mediaMapper.mapOut(realmMedia) }
    }

}
