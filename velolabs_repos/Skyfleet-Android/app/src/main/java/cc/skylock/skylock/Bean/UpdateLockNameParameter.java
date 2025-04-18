package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 10-08-2016.
 */
public class UpdateLockNameParameter {

    /**
     * properties : {"lock_id":12,"name":"Arun Ellipse"}
     */

    private PropertiesEntity properties;

    public PropertiesEntity getProperties() {
        return properties;
    }

    public void setProperties(PropertiesEntity properties) {
        this.properties = properties;
    }

    public static class PropertiesEntity {
        /**
         * lock_id : 12
         * name : Arun Ellipse
         */

        private int lock_id;
        private String name;

        public int getLock_id() {
            return lock_id;
        }

        public void setLock_id(int lock_id) {
            this.lock_id = lock_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
