package com.lattis.ellipse.domain.interactor.SavedAddress;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.SavedAddress;
import com.lattis.ellipse.domain.repository.SavedAddressRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SavedAddressUseCase extends UseCase<Boolean>

{

    private SavedAddressRepository savedAddressRepository;
    private List<SavedAddress>  savedAddresses;

    @Inject
    public SavedAddressUseCase(ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread,
                           SavedAddressRepository savedAddressRepository) {
        super(threadExecutor, postExecutionThread);
        this.savedAddressRepository = savedAddressRepository;
    }

    public SavedAddressUseCase withSavedAddress(List<SavedAddress> savedAddresses) {
        this.savedAddresses = savedAddresses;
        return this;
    }


    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return this.savedAddressRepository.saveAddresses(savedAddresses);
    }
}
