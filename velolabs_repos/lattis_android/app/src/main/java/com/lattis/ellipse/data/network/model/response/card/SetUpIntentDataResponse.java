package com.lattis.ellipse.data.network.model.response.card;

import com.google.gson.annotations.SerializedName;
import com.lattis.ellipse.data.network.model.response.AbstractApiResponse;
import com.lattis.ellipse.data.network.model.response.bike.FindBikeDataResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SetUpIntentDataResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("application")
    private String application;

    @SerializedName("cancellation_reason")
    private String cancellation_reason;

    @SerializedName("client_secret")
    private String client_secret;

    @SerializedName("created")
    private long created;

    @SerializedName("customer")
    private String customer;

    @SerializedName("description")
    private String description;

    @SerializedName("last_setup_error")
    private String last_setup_error;

    @SerializedName("livemode")
    private boolean livemode;

    @SerializedName("next_action")
    private String next_action;

    @SerializedName("on_behalf_of")
    private String on_behalf_of;

    @SerializedName("payment_method")
    private String payment_method;

    @SerializedName("status")
    private String status;

    @SerializedName("usage")
    private String usage;

    @SerializedName("payment_method_types")
    private List<String> payment_method_types;


    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public String getApplication() {
        return application;
    }

    public String getCancellation_reason() {
        return cancellation_reason;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public long getCreated() {
        return created;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDescription() {
        return description;
    }

    public String getLast_setup_error() {
        return last_setup_error;
    }

    public boolean isLivemode() {
        return livemode;
    }

    public String getNext_action() {
        return next_action;
    }

    public String getOn_behalf_of() {
        return on_behalf_of;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public String getStatus() {
        return status;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getPayment_method_types() {
        return payment_method_types;
    }

}
