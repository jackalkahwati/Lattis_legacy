package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Contact;

import java.util.List;

import io.reactivex.Observable;

public interface ContactRepository {

    Observable<Contact> getContact(String contactId);

    Observable<List<Contact>> saveContactList(List<Contact> contactList);

    Observable<List<Contact>> getEmergencyContacts();

    Observable<List<Contact>> saveEmergencyContacts(List<Contact> contacts);

    Observable<Boolean> removeEmergencyContact(Contact contact);
}
