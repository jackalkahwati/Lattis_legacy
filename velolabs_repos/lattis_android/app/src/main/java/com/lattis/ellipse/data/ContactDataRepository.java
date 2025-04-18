package com.lattis.ellipse.data;

import android.util.Log;

import com.lattis.ellipse.data.database.ContactRealmDataStore;
import com.lattis.ellipse.domain.model.Contact;
import com.lattis.ellipse.domain.repository.ContactRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ContactDataRepository implements ContactRepository {

    private ContactRealmDataStore contactRealmDataStore;

    @Inject
    public ContactDataRepository(ContactRealmDataStore contactRealmDataStore) {
        this.contactRealmDataStore = contactRealmDataStore;
    }

    @Override
    public Observable<Contact> getContact(String contactId) {
        return contactRealmDataStore.getContact(contactId);
    }


    @Override
    public Observable<List<Contact>> saveContactList(List<Contact> contactList) {
        Log.e("ContactDataRepository","saveContact");
        return contactRealmDataStore.saveContactList(contactList);
    }

    @Override
    public Observable<List<Contact>> getEmergencyContacts() {
        return contactRealmDataStore.getEmergencyContacts();
    }

    @Override
    public Observable<List<Contact>> saveEmergencyContacts(List<Contact> contacts) {
        return contactRealmDataStore.createOrUpdateEmergencyContacts(contacts);
    }

    @Override
    public Observable<Boolean> removeEmergencyContact(Contact contact) {
        return contactRealmDataStore.removeEmergencyContact(contact);
    }
}
