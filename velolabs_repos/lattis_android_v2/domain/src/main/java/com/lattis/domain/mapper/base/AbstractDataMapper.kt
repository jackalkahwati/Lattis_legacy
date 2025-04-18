package com.lattis.domain.mapper.base


import java.util.ArrayList



abstract class AbstractDataMapper<IN, OUT> {

    open fun mapIn(ins: List<IN>): List<OUT> {
        val outs = ArrayList<OUT>()
        for (inObjec in ins) {
            outs.add(mapIn(inObjec));
        }
        return outs
    }

    abstract fun mapIn(inObject: IN?): OUT

//    open fun mapIn(ins: Array<IN>): List<OUT> {
//        return mapIn(Arrays.asList<IN>(*ins))
//    }


    open fun mapOut(outs: List<OUT>): List<IN?> {
        val objects = ArrayList<IN?>()
        for (realObject in outs) {
            objects.add(mapOut(realObject))
        }
        return objects
    }

//    open fun mapOut(outs: Array<OUT>): List<IN> {
//        return mapOut(Arrays.asList<OUT>(*outs))
//    }

    abstract fun mapOut(out: OUT?): IN?

}
