package com.lattis.ellipse.data.network.model.body.alert;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.body.commun.ContactBody;
import com.lattis.ellipse.data.network.model.body.commun.LocationBody;

import java.util.List;

public class SendEmergencyAlertBody {

    @SerializedName("crash_id")
    private int crashId;
    @SerializedName("mac_id")
    private String macAddress;
    @SerializedName("location")
    private LocationBody location;
    @SerializedName("contacts")
    private List<ContactBody> emergencyContacts;

    public SendEmergencyAlertBody(int crashId, String macAddress, LocationBody location, List<ContactBody> emergencyContacts) {
        this.crashId = crashId;
        this.macAddress = macAddress;
        this.location = location;
        this.emergencyContacts = emergencyContacts;
    }
}
