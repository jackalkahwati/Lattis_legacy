package com.lattis.domain.usecase.saveaddress

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.SavedAddressRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.SavedAddress
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetSavedAddressUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val savedAddressRepository: SavedAddressRepository
) : UseCase<List<SavedAddress>>(threadExecutor, postExecutionThread) {
    private val savedAddress: SavedAddress? = null
    override fun buildUseCaseObservable(): Observable<List<SavedAddress>> {
        return savedAddressRepository.savedAddresses!!
    }

}