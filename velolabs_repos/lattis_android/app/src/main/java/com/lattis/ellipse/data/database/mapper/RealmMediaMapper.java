package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmMedia;
import com.lattis.ellipse.domain.model.Media;

import javax.inject.Inject;

/**
 * Created by raverat on 4/6/17.
 */

public class RealmMediaMapper extends AbstractRealmDataMapper<Media, RealmMedia> {

    @Inject
    public RealmMediaMapper() {}

    @NonNull
    @Override
    public RealmMedia mapIn(@NonNull Media media) {
        RealmMedia realmMedia = new RealmMedia();
        realmMedia.setId(media.getId());
        realmMedia.setUrl(media.getUrl());
        return realmMedia;
    }

    @NonNull
    @Override
    public Media mapOut(@NonNull RealmMedia realmMedia) {
        Media media = new Media();
        media.setId(realmMedia.getId());
        media.setUrl(realmMedia.getUrl());
        return media;
    }

}
