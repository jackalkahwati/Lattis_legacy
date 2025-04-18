package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 26-10-2016.
 */

public class PasswordResetResponse {

    /**
     * error : null
     * payload : {"verification_code":true}
     * status : 201
     */

    private Object error;
    /**
     * verification_code : true
     */

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
        private boolean verification_code;

        public boolean isVerification_code() {
            return verification_code;
        }

        public void setVerification_code(boolean verification_code) {
            this.verification_code = verification_code;
        }
    }
}
