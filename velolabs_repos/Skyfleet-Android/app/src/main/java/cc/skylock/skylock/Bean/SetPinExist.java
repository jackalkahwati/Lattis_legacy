package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 08-02-2017.
 */

public class SetPinExist {

    /**
     * error : null
     * status : 200
     * payload : {"has_code":true}
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
         * has_code : true
         */

        private boolean has_code;

        public boolean isHas_code() {
            return has_code;
        }

        public void setHas_code(boolean has_code) {
            this.has_code = has_code;
        }
    }
}
