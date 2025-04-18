package com.lattis.ellipse.domain.repository;

import com.lattis.ellipse.domain.model.Contact;

import io.reactivex.Observable;

public interface EmergencyRepository {

    Observable<Void> sendEmergencyAlert(Contact contact);
}
