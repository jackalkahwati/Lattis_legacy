package com.lattis.ellipse.data.database.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ssd3 on 7/26/17.
 */

public class RealmCard extends RealmObject {

    @PrimaryKey
    private int id;
    private int user_id;
    private String stripe_net_profile_id;
    private String stripe_net_payment_id;
    private boolean is_primary;
    private String type_card;
    private String cc_no;
    private int exp_month;
    private int exp_year;
    private String fingerprint;
    private String cc_type;
    private int created_date;
    private String last_updated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getStripe_net_profile_id() {
        return stripe_net_profile_id;
    }

    public void setStripe_net_profile_id(String stripe_net_profile_id) {
        this.stripe_net_profile_id = stripe_net_profile_id;
    }

    public String getStripe_net_payment_id() {
        return stripe_net_payment_id;
    }

    public void setStripe_net_payment_id(String stripe_net_payment_id) {
        this.stripe_net_payment_id = stripe_net_payment_id;
    }

    public boolean getIs_primary() {
        return is_primary;
    }

    public void setIs_primary(boolean is_primary) {
        this.is_primary = is_primary;
    }

    public String getType_card() {
        return type_card;
    }

    public void setType_card(String type_card) {
        this.type_card = type_card;
    }

    public String getCc_no() {
        return cc_no;
    }

    public void setCc_no(String cc_no) {
        this.cc_no = cc_no;
    }

    public int getExp_month() {
        return exp_month;
    }

    public void setExp_month(int exp_month) {
        this.exp_month = exp_month;
    }

    public int getExp_year() {
        return exp_year;
    }

    public void setExp_year(int exp_year) {
        this.exp_year = exp_year;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getCc_type() {
        return cc_type;
    }

    public void setCc_type(String cc_type) {
        this.cc_type = cc_type;
    }

    public int getCreated_date() {
        return created_date;
    }

    public void setCreated_date(int created_date) {
        this.created_date = created_date;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }
}
