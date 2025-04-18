package com.lattis.lattis.executor

import com.lattis.domain.executor.PostExecutionThread
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UiThread @Inject constructor() : PostExecutionThread {
    override val scheduler: Scheduler = AndroidSchedulers.mainThread()
}