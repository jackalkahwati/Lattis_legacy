package com.lattis.ellipse.domain.interactor.emergency;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Contact;
import com.lattis.ellipse.domain.repository.ContactRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


public class GetEmergencyContactUseCase extends UseCase<List<Contact>> {

    public final static String TAG = GetEmergencyContactUseCase.class.getSimpleName();

    private String emergencyContactNumber;
    private ContactRepository contactRepository;

    @Inject
    protected GetEmergencyContactUseCase(ThreadExecutor threadExecutor,
                                         PostExecutionThread postExecutionThread,
                                         ContactRepository contactRepository) {
        super(threadExecutor, postExecutionThread);
        this.contactRepository = contactRepository;
    }


    public GetEmergencyContactUseCase withEmergencyContactNumber(String number) {
        this.emergencyContactNumber = number;
        return this;
    }



    @Override
    protected Observable<List<Contact>> buildUseCaseObservable() {
        return contactRepository.getEmergencyContacts() ;//return contactRepository.getContact();
    }



}
