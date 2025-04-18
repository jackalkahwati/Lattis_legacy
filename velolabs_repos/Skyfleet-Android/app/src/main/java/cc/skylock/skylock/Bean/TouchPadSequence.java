package cc.skylock.skylock.Bean;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Velo Labs Android on 10-01-2017.
 */

public class TouchPadSequence {

    /**
     * mac_id : Mac id of Ellipse
     * touch_pad : Touch Pad Sequence
     */

    private String mac_id;
    private String[] pin_code;

    public String[] getPin_code() {
        return pin_code;
    }

    public void setPin_code(String[] pin_code) {
        this.pin_code = pin_code;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }


}
