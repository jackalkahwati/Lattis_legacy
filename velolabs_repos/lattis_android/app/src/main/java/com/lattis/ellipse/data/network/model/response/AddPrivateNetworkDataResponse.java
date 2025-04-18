package com.lattis.ellipse.data.network.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddPrivateNetworkDataResponse {

    @SerializedName("lattis_account")
    private List<Lattis_Account> lattis_accounts;

    public List<Lattis_Account> getLattis_accounts() {
        return lattis_accounts;
    }


    public class Lattis_Account{
        @SerializedName("user_id")
        private int userId;

        @SerializedName("email")
        private String email;

        @SerializedName("fleet_id")
        private int fleet_id;

        @SerializedName("access")
        private int access;

        @SerializedName("verified")
        private int verified;


        public int getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public int getFleet_id() {
            return fleet_id;
        }

        public int getAccess() {
            return access;
        }

        public int getVerified() {
            return verified;
        }

    }

}
