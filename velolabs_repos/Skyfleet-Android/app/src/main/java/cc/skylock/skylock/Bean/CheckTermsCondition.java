package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 10-02-2017.
 */

public class CheckTermsCondition {

    /**
     * error : null
     * status : 200
     * payload : {"has_accepted":false}
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
         * has_accepted : false
         */

        private boolean has_accepted;

        public boolean isHas_accepted() {
            return has_accepted;
        }

        public void setHas_accepted(boolean has_accepted) {
            this.has_accepted = has_accepted;
        }
    }
}
