package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 05-02-2017.
 */

public class   ShareUnshareList {


    /**
     * lock_id : 60
     * mac_id : EE98457172F0
     * user_id : 55
     * users_id : +919840182541
     * public_key : 043e544c9eb1258d665c699542372c307114d894e2801a28d9dbea33f00ba3cdc414d9b97eb49508f091d0694ce12c31819008dda538d99faaaf6341a976315408
     * name : Android BLe Blue
     * share_id : 9
     * shared_to_user_id : 56
     */

    private int lock_id;
    private String mac_id;
    private int user_id;
    private String users_id;
    private String public_key;
    private String name;
    private int share_id;
    private String shared_to_user_id;

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsers_id() {
        return users_id;
    }

    public void setUsers_id(String users_id) {
        this.users_id = users_id;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getShare_id() {
        return share_id;
    }

    public void setShare_id(int share_id) {
        this.share_id = share_id;
    }

    public String getShared_to_user_id() {
        return shared_to_user_id;
    }

    public void setShared_to_user_id(String shared_to_user_id) {
        this.shared_to_user_id = shared_to_user_id;
    }


}
