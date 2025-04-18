package cc.skylock.skylock.Bean;

import java.util.List;

/**
 * Created by Velo Labs Android on 26-09-2016.
 */
public class FirmwareMetaData {


    /**
     * error : null
     * status : 200
     * payload : ["02.22","02.25"]
     */

    private Object error;
    private int status;
    private List<String> payload;

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

    public List<String> getPayload() {
        return payload;
    }

    public void setPayload(List<String> payload) {
        this.payload = payload;
    }
}
