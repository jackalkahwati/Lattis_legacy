package com.lattis.ellipse.domain.interactor.emergency;

import com.lattis.ellipse.domain.executor.PostExecutionThread;
import com.lattis.ellipse.domain.executor.ThreadExecutor;
import com.lattis.ellipse.domain.interactor.UseCase;
import com.lattis.ellipse.domain.model.Contact;
import com.lattis.ellipse.domain.repository.ContactRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class RemoveEmergencyContactUseCase extends UseCase<Boolean> {

    public final static String TAG = RemoveEmergencyContactUseCase.class.getSimpleName();

    private Contact contact;
    private ContactRepository contactRepository;

    @Inject
    protected RemoveEmergencyContactUseCase(ThreadExecutor threadExecutor,
                                         PostExecutionThread postExecutionThread,
                                         ContactRepository contactRepository) {
        super(threadExecutor, postExecutionThread);
        this.contactRepository = contactRepository;
    }


    public RemoveEmergencyContactUseCase withEmergencyContact(Contact contact) {
        this.contact = contact;
        return this;
    }



    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return contactRepository.removeEmergencyContact(contact);
    }



}
