package com.lattis.domain.usecase.apps

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.Help
import com.lattis.domain.repository.AppsRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetHelpInfoUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val appsRepository: AppsRepository
) : UseCase<Help>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Help> {
        return appsRepository.getHelpInfo()
    }
}