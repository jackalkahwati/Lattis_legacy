package io.lattis.ellipse.sdk.mapper;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
