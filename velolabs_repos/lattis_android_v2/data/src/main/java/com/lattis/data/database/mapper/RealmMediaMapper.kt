package com.lattis.data.database.mapper

import com.lattis.data.database.base.AbstractRealmDataMapper
import com.lattis.data.database.model.RealmMedia
import com.lattis.domain.models.Media
import javax.inject.Inject

/**
 * Created by raverat on 4/6/17.
 */

class RealmMediaMapper @Inject
constructor() : AbstractRealmDataMapper<Media, RealmMedia>() {

    override fun mapIn(media: Media): RealmMedia {
        val realmMedia = RealmMedia()
        realmMedia.id = media.id
        realmMedia.url = media.url
        return realmMedia
    }

    override fun mapOut(realmMedia: RealmMedia): Media {
        val media = Media()
        media.id = realmMedia.id
        media.url = realmMedia.url
        return media
    }

}
