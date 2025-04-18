package com.lattis.ellipse.domain.interactor.emergency;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Contact;
import com.lattis.ellipse.domain.repository.ContactRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;


/**
 * Created by ssd3 on 3/2/17.
 */

public class AddEmergencyContactUseCase extends UseCase<List<Contact>> {

    public final static String TAG = AddEmergencyContactUseCase.class.getSimpleName();

    private String emergencyContactNumber;
    private ContactRepository contactRepository;
    private List<Contact> contactList;

    @Inject
    protected AddEmergencyContactUseCase(ThreadExecutor threadExecutor,
                                         PostExecutionThread postExecutionThread,
                                         ContactRepository contactRepository) {
        super(threadExecutor, postExecutionThread);
        this.contactRepository = contactRepository;
    }

    public AddEmergencyContactUseCase withContact(List<Contact> contactList) {
        this.contactList = contactList;
        return this;
    }

    @Override
    protected Observable<List<Contact>> buildUseCaseObservable() {
        return contactRepository.saveContactList(contactList);
    }



}
