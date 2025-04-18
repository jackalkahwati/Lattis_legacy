package com.lattis.domain.usecase.uploadimage

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.models.UploadImage
import com.lattis.domain.repository.UploadImageRepository
import com.lattis.domain.usecase.base.UseCase
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class UploadImageUseCase @Inject protected constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    var uploadImageRepository: UploadImageRepository
) : UseCase<UploadImage>(threadExecutor, postExecutionThread) {
    private var uploadType: String? = null
    private var filepath: String? = null
    fun withUploadType(uploadType: String): UploadImageUseCase {
        this.uploadType = uploadType
        return this
    }

    fun withFilePath(filepath: String): UploadImageUseCase {
        this.filepath = filepath
        return this
    }

    override fun buildUseCaseObservable(): Observable<UploadImage> {
        return uploadImageRepository.uploadImage(uploadType!!, filepath!!)
    }

}