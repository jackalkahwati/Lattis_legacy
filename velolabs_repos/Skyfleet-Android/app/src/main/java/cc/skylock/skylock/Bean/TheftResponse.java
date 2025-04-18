package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 28-01-2017.
 */

public class TheftResponse {

    /**
     * error : null
     * status : 200
     * payload : {"theft_id":4,"date":1485561849,"confirmed":null,"x_ave":731,"y_ave":805,"z_ave":2786,"x_dev":819,"y_dev":2038,"z_dev":0,"lock_id":13,"user_id":24}
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
         * theft_id : 4
         * date : 1485561849
         * confirmed : null
         * x_ave : 731
         * y_ave : 805
         * z_ave : 2786
         * x_dev : 819
         * y_dev : 2038
         * z_dev : 0
         * lock_id : 13
         * user_id : 24
         */

        private int theft_id;
        private int date;
        private Object confirmed;
        private int x_ave;
        private int y_ave;
        private int z_ave;
        private int x_dev;
        private int y_dev;
        private int z_dev;
        private int lock_id;
        private int user_id;

        public int getTheft_id() {
            return theft_id;
        }

        public void setTheft_id(int theft_id) {
            this.theft_id = theft_id;
        }

        public int getDate() {
            return date;
        }

        public void setDate(int date) {
            this.date = date;
        }

        public Object getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(Object confirmed) {
            this.confirmed = confirmed;
        }

        public int getX_ave() {
            return x_ave;
        }

        public void setX_ave(int x_ave) {
            this.x_ave = x_ave;
        }

        public int getY_ave() {
            return y_ave;
        }

        public void setY_ave(int y_ave) {
            this.y_ave = y_ave;
        }

        public int getZ_ave() {
            return z_ave;
        }

        public void setZ_ave(int z_ave) {
            this.z_ave = z_ave;
        }

        public int getX_dev() {
            return x_dev;
        }

        public void setX_dev(int x_dev) {
            this.x_dev = x_dev;
        }

        public int getY_dev() {
            return y_dev;
        }

        public void setY_dev(int y_dev) {
            this.y_dev = y_dev;
        }

        public int getZ_dev() {
            return z_dev;
        }

        public void setZ_dev(int z_dev) {
            this.z_dev = z_dev;
        }

        public int getLock_id() {
            return lock_id;
        }

        public void setLock_id(int lock_id) {
            this.lock_id = lock_id;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }
    }
}
