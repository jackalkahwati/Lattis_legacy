package cc.skylock.skylock.Bean;

/**
 * Created by admin on 15/08/16.
 */
public class ShareLockResponse {

    /**
     * error : null
     * hello : why
     * payload : {"share_code":"866766"}
     * status : 201
     */

    private String error;
    private String hello;
    /**
     * share_code : 866766
     */

    private PayloadEntity payload;
    private int status;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
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
        private String share_code;

        public String getShare_code() {
            return share_code;
        }

        public void setShare_code(String share_code) {
            this.share_code = share_code;
        }
    }
}
