package com.lattis.ellipse.data.network.model.body.card;

import com.google.gson.annotations.SerializedName;


public class DeleteCardBody {

    @SerializedName("id")
    private int cardId;

    public DeleteCardBody(int cardId) {
        this.cardId = cardId;
    }


}
