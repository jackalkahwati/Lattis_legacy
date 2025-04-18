package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 20-09-2016.
 */
public class UpdateAccount {


    /**
     * error : null
     * payload : {"message":"Updated"}
     * status : 201
     */

    private Object error;
    /**
     * message : Updated
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
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
