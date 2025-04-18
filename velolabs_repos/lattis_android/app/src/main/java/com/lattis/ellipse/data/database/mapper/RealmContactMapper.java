package com.lattis.ellipse.data.database.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.database.base.AbstractRealmDataMapper;
import com.lattis.ellipse.data.database.model.RealmContact;
import com.lattis.ellipse.domain.model.Contact;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmResults;

public class RealmContactMapper extends AbstractRealmDataMapper<Contact,RealmContact> {

    @Inject
    public RealmContactMapper() {}

    @NonNull
    @Override
    public RealmContact mapIn(@NonNull Contact contact) {
        RealmContact realmContact = new RealmContact();
        realmContact.setId(contact.getId());
        realmContact.setFirstName(contact.getFirstName());
        realmContact.setLastName(contact.getLastName());
        realmContact.setPhoneNumber(contact.getPhoneNumber());
        return realmContact;
    }

    @NonNull
    @Override
    public Contact mapOut(@NonNull RealmContact realmContact) {
        Contact contact = new Contact();
        contact.setId(realmContact.getId());
        contact.setFirstName(realmContact.getFirstName());
        contact.setLastName(realmContact.getLastName());
        contact.setPhoneNumber(realmContact.getPhoneNumber());
        return contact;
    }


    @NonNull
    @Override
    public List<Contact> mapOut(@NonNull RealmResults<RealmContact> realmContacts) {
        List<Contact> contactList = new ArrayList<>();
        for (RealmContact realmContact : realmContacts) {
            contactList.add(mapOut(realmContact));
        }
        return contactList;
    }
}
