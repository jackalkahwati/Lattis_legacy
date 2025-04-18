package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-01-2017.
 */

public class CrashAndTheftParameter {

    /**
     * mac_id : D9D36323578D
     * accelerometer_data : {"x_ave":8383.393939,"y_ave":9383.393939,"z_ave":2.393939,"x_dev":8383.393939,"y_dev":8383.393939,"z_dev":83.393939}
     * location : {"latitude":37.336103,"longitude":-120.484391}
     */

    private String mac_id;
    private AccelerometerDataEntity accelerometer_data;
    private LocationEntity location;

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public AccelerometerDataEntity getAccelerometer_data() {
        return accelerometer_data;
    }

    public void setAccelerometer_data(AccelerometerDataEntity accelerometer_data) {
        this.accelerometer_data = accelerometer_data;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public static class AccelerometerDataEntity {
        /**
         * x_ave : 8383.393939
         * y_ave : 9383.393939
         * z_ave : 2.393939
         * x_dev : 8383.393939
         * y_dev : 8383.393939
         * z_dev : 83.393939
         */

        private double x_ave;
        private double y_ave;
        private double z_ave;
        private double x_dev;
        private double y_dev;
        private double z_dev;

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
    }

    public static class LocationEntity {
        /**
         * latitude : 37.336103
         * longitude : -120.484391
         */

        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}
