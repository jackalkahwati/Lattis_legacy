package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-01-2017.
 */

public class ConfirmTheftParameter {

    /**
     * theft_id : 2
     * is_confirmed : false
     */

    private int theft_id;
    private boolean is_confirmed;

    public int getTheft_id() {
        return theft_id;
    }

    public void setTheft_id(int theft_id) {
        this.theft_id = theft_id;
    }

    public boolean isIs_confirmed() {
        return is_confirmed;
    }

    public void setIs_confirmed(boolean is_confirmed) {
        this.is_confirmed = is_confirmed;
    }
}
