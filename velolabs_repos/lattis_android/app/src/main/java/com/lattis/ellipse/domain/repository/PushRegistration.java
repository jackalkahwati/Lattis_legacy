package com.lattis.ellipse.domain.repository;

import io.reactivex.Observable;

public interface PushRegistration {

    Observable<String> registerToPush();

    Observable<Boolean> unregisterFromPush();

}
