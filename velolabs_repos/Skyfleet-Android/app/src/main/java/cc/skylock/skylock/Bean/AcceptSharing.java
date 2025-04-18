package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 11-10-2016.
 */

public class AcceptSharing {


    /**
     * error : null
     * status : 200
     * payload : {"name":"Arun's Ellipse","mac_id":"EE98457172F0"}
     */

    private Object error;
    private int status;
    private PayloadEntity payload;

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PayloadEntity getPayload() {
        return payload;
    }

    public void setPayload(PayloadEntity payload) {
        this.payload = payload;
    }

    public static class PayloadEntity {
        /**
         * name : Arun's Ellipse
         * mac_id : EE98457172F0
         */

        private String name;
        private String mac_id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMac_id() {
            return mac_id;
        }

        public void setMac_id(String mac_id) {
            this.mac_id = mac_id;
        }
    }
}
