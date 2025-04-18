package com.lattis.ellipse.data;

import android.content.Context;

import com.lattis.ellipse.domain.model.Time;
import com.lattis.ellipse.domain.repository.TimerRepository;

import javax.inject.Inject;

import io.reactivex.subjects.PublishSubject;


/**
 * Created by ssd3 on 3/30/17.
 */

public class TimerDataRepository implements TimerRepository {

    private PublishSubject<Time> subject = PublishSubject.create();
    private Context context;


    @Inject
    TimerDataRepository(Context context){
        this.context = context;
    }



}
