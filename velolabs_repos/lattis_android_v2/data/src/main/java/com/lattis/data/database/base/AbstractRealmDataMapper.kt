package com.lattis.data.database.base

import java.util.ArrayList

import io.realm.RealmModel
import io.realm.RealmResults

abstract class AbstractRealmDataMapper<IN, OUT : RealmModel> {


    fun mapIn(objects: List<IN>): List<OUT> {
        val realmObjects = ArrayList<OUT>()
        for (`object` in objects) {
            realmObjects.add(mapIn(`object`))
        }
        return realmObjects
    }

    abstract fun mapIn(`object`: IN): OUT

    fun mapOut(realmObjectList: List<OUT>): List<IN> {
        val objects = ArrayList<IN>()
        for (realObject in realmObjectList) {
            objects.add(mapOut(realObject))
        }
        return objects
    }

    open fun mapOut(realmObjectList: RealmResults<OUT>): List<IN> {
        val objects = ArrayList<IN>()
        for (realObject in realmObjectList) {
            objects.add(mapOut(realObject))
        }
        return objects
    }

    abstract fun mapOut(realmObject: OUT): IN
}
