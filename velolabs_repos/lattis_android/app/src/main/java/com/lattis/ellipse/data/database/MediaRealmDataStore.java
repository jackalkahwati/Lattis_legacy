package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmMediaMapper;
import com.lattis.ellipse.data.database.model.RealmMedia;
import com.lattis.ellipse.domain.model.Media;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.realm.RealmConfiguration;

/**
 * Created by raverat on 4/6/17.
 */

public class MediaRealmDataStore {

    private RealmMediaMapper mediaMapper;
    private RealmConfiguration realmConfiguration;

    @Inject
    public MediaRealmDataStore(RealmConfiguration realmConfiguration, RealmMediaMapper mediaMapper) {
        this.mediaMapper = mediaMapper;
        this.realmConfiguration=realmConfiguration;
    }

    public Observable<Media> getMedia(String id) {
        return RealmObservable.object(
                realm -> {
                    RealmMedia realmMedia = realm.where(RealmMedia.class).equalTo("id", id).findFirst();
                    return realmMedia != null ? realmMedia : new RealmMedia();
                })
                .map(realmMedia -> mediaMapper.mapOut(realmMedia));
    }

    public Observable<Media> createOrUpdateUser(Media media) {
        return RealmObservable.object(realm -> realm.copyToRealmOrUpdate(mediaMapper.mapIn(media)))
                .map(realmMedia -> mediaMapper.mapOut(realmMedia));
    }

}
