package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 05-02-2016.
 */
public class LockKeyGen {


    /**
     * error : null
     * status : 200
     * payload : {"lock_id":1,"mac_id":"D7368046EA82","user_id":1,"users_id":"9840182541","public_key":"049061c3cab0a3e48d1c8d1579c3c60313d0473c3c80bc865aa3e7496284aa6269704b9b269004fe2c62055232a758739bbc3ea6260344df51ea2ab406046e37f6","name":null}
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
         * lock_id : 1
         * mac_id : D7368046EA82
         * user_id : 1
         * users_id : 9840182541
         * public_key : 049061c3cab0a3e48d1c8d1579c3c60313d0473c3c80bc865aa3e7496284aa6269704b9b269004fe2c62055232a758739bbc3ea6260344df51ea2ab406046e37f6
         * name : null
         */

        private int lock_id;
        private String mac_id;
        private int user_id;
        private String users_id;
        private String public_key;
        private String name;

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
    }
}
