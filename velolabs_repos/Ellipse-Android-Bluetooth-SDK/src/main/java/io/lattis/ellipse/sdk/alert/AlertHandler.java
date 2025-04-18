package io.lattis.ellipse.sdk.alert;

import java.util.concurrent.ArrayBlockingQueue;

import io.lattis.ellipse.sdk.model.AccelerometerData;

abstract class AlertHandler {

    private final ArrayBlockingQueue<AccelerometerData> sampleData;

    AlertHandler() {
        this.sampleData = new ArrayBlockingQueue<>(getSampleCapacity());
    }

    public abstract int getSampleCapacity();

    public abstract boolean isInAlert();

    public void add(AccelerometerData accelerometerData) {
        if(sampleData.remainingCapacity() == 0){
            sampleData.remove();
        }
        sampleData.add(accelerometerData);
    }

    ArrayBlockingQueue<AccelerometerData> getSampleData() {
        return sampleData;
    }

    void clearSampleData(){
        sampleData.clear();
    }
}
