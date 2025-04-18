package com.lattis.ellipse.data.network.base;

import androidx.annotation.NonNull;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmList;

public abstract class AbstractDataMapper<IN,OUT> {

    @NonNull
    public List<OUT> mapIn(@NonNull List<IN> ins) {
        List<OUT> outs = new ArrayList<>();
        for (IN object : ins) {
            outs.add(mapIn(object));
        }
        return outs;
    }

    @NonNull
    public abstract OUT mapIn(@NonNull IN in);

    @NonNull
    public List<OUT> mapIn(@NonNull IN[] ins) {
        return mapIn(Arrays.asList(ins));
    }

    @NonNull
    public List<IN> mapOut(@NonNull List<OUT> outs) {
        List<IN> objects = new ArrayList<>();
        for (OUT realObject : outs) {
            objects.add(mapOut(realObject));
        }
        return objects;
    }

    @NonNull
    public List<IN> mapOut(@NonNull OUT[] outs) {
        return mapOut(Arrays.asList(outs));
    }

    @NonNull
    public abstract IN mapOut(@NonNull OUT out);

}
