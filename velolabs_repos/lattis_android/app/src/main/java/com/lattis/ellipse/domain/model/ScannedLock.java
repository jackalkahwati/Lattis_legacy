package com.lattis.ellipse.domain.model;

/**
 * Created by ssd3 on 4/26/17.
 */

public class ScannedLock {

    private String macAddress;
    private String macId;
    private String name;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
