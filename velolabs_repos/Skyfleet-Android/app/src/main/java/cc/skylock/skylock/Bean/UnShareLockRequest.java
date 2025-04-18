package cc.skylock.skylock.Bean;

/**
 * Created by admin on 15/08/16.
 */
public class UnShareLockRequest {
    /**
     * lock_id : lockId
     * shared_to_user_id : userId
     */

    private String share_id;

    public String getShare_id() {
        return share_id;
    }

    public void setShare_id(String share_id) {
        this.share_id = share_id;
    }

    private String shared_to_user_id;



    public String getShared_to_user_id() {
        return shared_to_user_id;
    }

    public void setShared_to_user_id(String shared_to_user_id) {
        this.shared_to_user_id = shared_to_user_id;
    }

    /**
     * user_id : user id of user to be revoked or surrendered
     * mac_id : Mac id of Skylock
     */

}
