package com.lattis.ellipse.domain.repository;

import io.reactivex.Observable;

public interface DataBaseManager {

    Observable<Void> createDataBase();

    Observable<Boolean> deleteDataBase();
}
