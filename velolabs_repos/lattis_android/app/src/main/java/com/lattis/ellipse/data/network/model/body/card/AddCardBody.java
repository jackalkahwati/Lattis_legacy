package com.lattis.ellipse.data.network.model.body.card;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

/**
 * Created by ssd3 on 7/26/17.
 */

public class AddCardBody {

    @SerializedName("cc_no")
    private String cc_no;

    @SerializedName("exp_month")
    private int exp_month;

    @SerializedName("exp_year")
    private int exp_year;

    @SerializedName("cvc")
    private String cvc;


    @SerializedName("intent")
    private JsonObject intent;



    public AddCardBody(String cc_no, int exp_month, int exp_year, String cvc, JSONObject intent){
        this.cc_no=cc_no;
        this.exp_month=exp_month;
        this.exp_year=exp_year;
        this.cvc=cvc;
        this.intent = (JsonObject)new JsonParser().parse(intent.toString());;
    }
}
