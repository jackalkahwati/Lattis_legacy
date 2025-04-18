package io.lattis.ellipse.sdk.model;

public enum Alert {

    OFF, THEFT, CRASH;

    private Class activity;

    private String lockId;

    private AccelerometerData accelerometerData;

    public Alert forLockId(String lockId) {
        this.lockId = lockId;
        return this;
    }

    public Alert forActivity(Class activity) {
        this.activity = activity;
        return this;
    }

    public Alert withAccelerometerData(AccelerometerData accelerometerData) {
        this.accelerometerData = accelerometerData;
        return this;
    }

    public String getLockId() {
        return lockId;
    }


    public AccelerometerData getAccelerometerData() {
        return accelerometerData;
    }

    public Class getActivity() {
        return activity;
    }
}
