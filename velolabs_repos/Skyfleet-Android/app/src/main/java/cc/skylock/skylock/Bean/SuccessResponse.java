package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 19-01-2017.
 */

public class SuccessResponse {

    /**
     * error : null
     * status : 200
     * payload : null
     */

    private Object error;
    private int status;
    private String[] payload;

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

    public String[] getPayload() {
        return payload;
    }

    public void setPayload(String[] payload) {
        this.payload = payload;
    }
}
