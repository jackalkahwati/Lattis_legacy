package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-01-2017.
 */

public class CrashResponse {

    /**
     * error : null
     * status : 200
     * payload : {"crash_id":1,"date":1484814448,"message_sent":0,"x_ave":8383.393939,"y_ave":9383.393939,"z_ave":2.393939,"x_dev":8383.393939,"y_dev":8383.393939,"z_dev":83.393939,"lock_id":1,"user_id":1}
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
         * crash_id : 1
         * date : 1484814448
         * message_sent : 0
         * x_ave : 8383.393939
         * y_ave : 9383.393939
         * z_ave : 2.393939
         * x_dev : 8383.393939
         * y_dev : 8383.393939
         * z_dev : 83.393939
         * lock_id : 1
         * user_id : 1
         */

        private int crash_id;
        private int date;
        private int message_sent;
        private double x_ave;
        private double y_ave;
        private double z_ave;
        private double x_dev;
        private double y_dev;
        private double z_dev;
        private int lock_id;
        private int user_id;

        public int getCrash_id() {
            return crash_id;
        }

        public void setCrash_id(int crash_id) {
            this.crash_id = crash_id;
        }

        public int getDate() {
            return date;
        }

        public void setDate(int date) {
            this.date = date;
        }

        public int getMessage_sent() {
            return message_sent;
        }

        public void setMessage_sent(int message_sent) {
            this.message_sent = message_sent;
        }

        public double getX_ave() {
            return x_ave;
        }

        public void setX_ave(double x_ave) {
            this.x_ave = x_ave;
        }

        public double getY_ave() {
            return y_ave;
        }

        public void setY_ave(double y_ave) {
            this.y_ave = y_ave;
        }

        public double getZ_ave() {
            return z_ave;
        }

        public void setZ_ave(double z_ave) {
            this.z_ave = z_ave;
        }

        public double getX_dev() {
            return x_dev;
        }

        public void setX_dev(double x_dev) {
            this.x_dev = x_dev;
        }

        public double getY_dev() {
            return y_dev;
        }

        public void setY_dev(double y_dev) {
            this.y_dev = y_dev;
        }

        public double getZ_dev() {
            return z_dev;
        }

        public void setZ_dev(double z_dev) {
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
