package com.lattis.ellipse.domain.model;

public enum Alert {

    OFF,THEFT,CRASH;

    private String lockId;

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getLockId() {
        return lockId;
    }

    public static Alert forValue(String value){
        for(Alert alert:values()){
            if(alert.name().equals(value)){
                return alert;
            }
        }
        return null;
    }
}
