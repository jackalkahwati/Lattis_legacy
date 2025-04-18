package com.lattis.ellipse.data.network.model.response.card;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ssd3 on 7/26/17.
 */

public class GetCardDataResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("stripe_net_profile_id")
    private String stripe_net_profile_id;

    @SerializedName("stripe_net_payment_id")
    private String stripe_net_payment_id;

    @SerializedName("is_primary")
    private boolean is_primary;

    @SerializedName("type_card")
    private String type_card;

    @SerializedName("cc_no")
    private String cc_no;

    @SerializedName("exp_month")
    private int exp_month;

    @SerializedName("exp_year")
    private int exp_year;

    @SerializedName("fingerprint")
    private String fingerprint;

    @SerializedName("cc_type")
    private String cc_type;

    @SerializedName("created_date")
    private int created_date;

    @SerializedName("last_updated")
    private String last_updated;

    public int getId() {
        return id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getStripe_net_profile_id() {
        return stripe_net_profile_id;
    }

    public String getStripe_net_payment_id() {
        return stripe_net_payment_id;
    }

    public boolean getIs_primary() {
        return is_primary;
    }

    public String getType_card() {
        return type_card;
    }

    public String getCc_no() {
        return cc_no;
    }

    public int getExp_month() {
        return exp_month;
    }

    public int getExp_year() {
        return exp_year;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getCc_type() {
        return cc_type;
    }

    public int getCreated_date() {
        return created_date;
    }

    public String getLast_updated() {
        return last_updated;
    }






}
