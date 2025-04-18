package com.lattis.lattis.infrastructure.di.module

import com.lattis.lattis.data.network.base.ApiEndpoints
import dagger.Module
import dagger.Provides
import io.lattis.lattis.BuildConfig
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideApiEndpoints(): ApiEndpoints {
        return if (BuildConfig.FLAVOR === "lattisBeta" ||
            BuildConfig.FLAVOR === "velotransitBeta" ||
            BuildConfig.FLAVOR === "sandypedalsBeta" ||
            BuildConfig.FLAVOR === "guestbikeBeta" ||
            BuildConfig.FLAVOR === "goscootBeta" ||
            BuildConfig.FLAVOR === "giraffBeta" ||
            BuildConfig.FLAVOR === "grinBeta" ||
            BuildConfig.FLAVOR === "grinsantiagoBeta" ||
            BuildConfig.FLAVOR === "waveBeta" ||
            BuildConfig.FLAVOR === "waweBeta" ||
            BuildConfig.FLAVOR === "mountBeta" ||
            BuildConfig.FLAVOR === "monkeydonkeyBeta" ||
            BuildConfig.FLAVOR === "unlimitedbikingBeta" ||
            BuildConfig.FLAVOR === "bandwagonBeta" ||
            BuildConfig.FLAVOR === "ourbikeBeta" ||
            BuildConfig.FLAVOR === "finBeta" ||
            BuildConfig.FLAVOR === "hoobaBeta" ||
            BuildConfig.FLAVOR === "pacificBeta" ||
            BuildConfig.FLAVOR === "bladeBeta" ||
            BuildConfig.FLAVOR === "tripBeta"||
            BuildConfig.FLAVOR === "greenridersBeta" ||
            BuildConfig.FLAVOR === "twowheelrentalBeta" ||
            BuildConfig.FLAVOR === "rockveloBeta" ||
            BuildConfig.FLAVOR === "falcosmartBeta"||
            BuildConfig.FLAVOR === "thriverydeBeta"||
            BuildConfig.FLAVOR === "lockemBeta" ||
            BuildConfig.FLAVOR === "robynBeta" ||
            BuildConfig.FLAVOR === "yetiBeta" ||
            BuildConfig.FLAVOR === "overwattBeta" ||
            BuildConfig.FLAVOR === "wbsBeta"){
            ApiEndpoints.PRODUCTION
        } else if (BuildConfig.FLAVOR === "lattisDev" ||
            BuildConfig.FLAVOR === "velotransitDev" ||
            BuildConfig.FLAVOR === "sandypedalsDev" ||
            BuildConfig.FLAVOR === "guestbikeDev" ||
            BuildConfig.FLAVOR === "goscootDev" ||
            BuildConfig.FLAVOR === "giraffDev" ||
            BuildConfig.FLAVOR === "grinDev" ||
            BuildConfig.FLAVOR === "grinsantiagoDev" ||
            BuildConfig.FLAVOR === "waveDev" ||
            BuildConfig.FLAVOR === "waweDev" ||
            BuildConfig.FLAVOR === "mountDev" ||
            BuildConfig.FLAVOR === "monkeydonkeyDev" ||
            BuildConfig.FLAVOR === "unlimitedbikingDev" ||
            BuildConfig.FLAVOR === "bandwagonDev" ||
            BuildConfig.FLAVOR === "ourbikeDev" ||
            BuildConfig.FLAVOR === "finDev" ||
            BuildConfig.FLAVOR === "hoobaDev" ||
            BuildConfig.FLAVOR === "pacificDev" ||
            BuildConfig.FLAVOR === "bladeDev" ||
            BuildConfig.FLAVOR === "tripDev" ||
            BuildConfig.FLAVOR === "greenridersDev" ||
            BuildConfig.FLAVOR === "twowheelrentalDev"||
            BuildConfig.FLAVOR === "rockveloDev"||
            BuildConfig.FLAVOR === "falcosmartDev"||
            BuildConfig.FLAVOR === "thriverydeDev"||
            BuildConfig.FLAVOR === "lockemDev" ||
            BuildConfig.FLAVOR === "robynDev" ||
            BuildConfig.FLAVOR === "yetiDev" ||
            BuildConfig.FLAVOR === "overwattDev" ||
            BuildConfig.FLAVOR === "wbsDev") {
            ApiEndpoints.STAGING
        } else {
            ApiEndpoints.PRODUCTION
        }
    }
}