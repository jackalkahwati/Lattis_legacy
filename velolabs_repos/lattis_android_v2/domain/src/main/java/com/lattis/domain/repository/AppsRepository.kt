package com.lattis.domain.repository

import com.lattis.domain.models.Help
import io.reactivex.rxjava3.core.Observable

interface AppsRepository {

    fun getHelpInfo():Observable<Help>
}