package com.lattis.domain.usecase.saveaddress

import com.lattis.domain.executor.PostExecutionThread
import com.lattis.domain.executor.ThreadExecutor
import com.lattis.domain.repository.SavedAddressRepository
import com.lattis.domain.usecase.base.UseCase
import com.lattis.domain.models.SavedAddress
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SavedAddressUseCase @Inject constructor(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread,
    private val savedAddressRepository: SavedAddressRepository
) : UseCase<Boolean>(threadExecutor, postExecutionThread) {
    private var savedAddresses: List<SavedAddress>? = null
    fun withSavedAddress(savedAddresses: List<SavedAddress>?): SavedAddressUseCase {
        this.savedAddresses = savedAddresses
        return this
    }

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return savedAddressRepository.saveAddresses(savedAddresses!!)
    }

}