package com.lattis.ellipse.data.database;

import com.lattis.ellipse.data.database.base.RealmObservable;
import com.lattis.ellipse.data.database.mapper.RealmContactMapper;
import com.lattis.ellipse.data.database.model.RealmContact;
import com.lattis.ellipse.domain.model.Contact;

import java.util.List;

import javax.inject.Inject;

import io.realm.RealmConfiguration;
import io.reactivex.Observable;

public class ContactRealmDataStore {

    private RealmContactMapper contactMapper;
    private RealmConfiguration realmConfiguration;

    @Inject
    public ContactRealmDataStore(RealmContactMapper contactMapper,
                                 RealmConfiguration realmConfiguration) {
        this.contactMapper = contactMapper;
        this.realmConfiguration = realmConfiguration;
    }

    public Observable<Contact> getContact(String id) {
        return RealmObservable.object(
                realm -> realm.where(RealmContact.class)
                        .equalTo(RealmContact.COLUMN_NAME_USER_ID, id)
                        .findFirst())
                .map(realmContact -> contactMapper.mapOut(realmContact));
    }

    public Observable<List<Contact>> saveContactList(List<Contact> contactList) {
        return RealmObservable.list(
                realm -> realm.copyToRealmOrUpdate(contactMapper.mapIn(contactList)))
                .map(realmContacts -> contactMapper.mapOut(realmContacts));
    }

    public Observable<List<Contact>> getEmergencyContacts() {
        return RealmObservable.results(
                realm -> realm.where(RealmContact.class)
                        .findAll())
                .map(realmContacts -> contactMapper.mapOut(realmContacts));
    }

    public Observable<List<Contact>> createOrUpdateEmergencyContacts(List<Contact> contacts) {
        return null;
    }

    public Observable<Boolean> removeEmergencyContact(Contact contact){
       return RealmObservable.deleteObject(
               realm -> {
           RealmContact realmContact = realm.where(RealmContact.class)
                   .equalTo(RealmContact.COLUMN_NAME_USER_ID, contact.getId())
                   .findFirst();
           if (realmContact != null) {
               realmContact.deleteFromRealm();
               return true;
           }
           return false;
       });
    }
}
