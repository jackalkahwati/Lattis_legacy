package io.lattis.domain.usecase.vehicle

import io.lattis.domain.executor.PostExecutionThread
import io.lattis.domain.executor.ThreadExecutor
import io.lattis.domain.repository.VehicleRepository
import io.lattis.domain.usecase.base.UseCase
import io.reactivex.Observable
import okhttp3.ResponseBody
import javax.inject.Inject

class ChangeBulkStatusUseCase @Inject
constructor(threadExecutor: ThreadExecutor,
            postExecutionThread: PostExecutionThread,
            private val vehicleRepository: VehicleRepository
) : UseCase<ResponseBody>(threadExecutor, postExecutionThread) {
    private var batch:String?=null
    private var status:String?=null
    private var usage:String?=null
    private var maintenance:String?=null

    fun withStatus(status:String):ChangeBulkStatusUseCase{
        this.status = status
        return this
    }
    fun withUsage(usage:String):ChangeBulkStatusUseCase{
        this.usage = usage
        return this
    }
    fun withMaintenance(maintenance:String?):ChangeBulkStatusUseCase{
        this.maintenance = maintenance
        return this
    }

    fun withBatch(batch:String):ChangeBulkStatusUseCase{
        this.batch = batch
        return this
    }

    override fun buildUseCaseObservable(): Observable<ResponseBody> {
        return vehicleRepository.changeBulkStatus(batch!!,status!!,usage!!,maintenance)
    }

}