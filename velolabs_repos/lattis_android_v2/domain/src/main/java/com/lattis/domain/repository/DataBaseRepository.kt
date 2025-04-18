package com.lattis.domain.repository

import io.reactivex.rxjava3.core.Observable

interface DataBaseRepository {

    fun createDataBase(): Observable<Boolean>

    fun deleteDataBase(): Observable<Boolean>
}
