package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-12-2016.
 */

public class GetLockNameResponse {

    /**
     * error : null
     * payload : {"lock_name":"Ellipse-367349"}
     * status : 201
     */

    private Object error;
    private PayloadEntity payload;
    private int status;

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public PayloadEntity getPayload() {
        return payload;
    }

    public void setPayload(PayloadEntity payload) {
        this.payload = payload;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class PayloadEntity {
        /**
         * lock_name : Ellipse-367349
         */

        private String lock_name;

        public String getLock_name() {
            return lock_name;
        }

        public void setLock_name(String lock_name) {
            this.lock_name = lock_name;
        }
    }
}
