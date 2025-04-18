package cc.skylock.skylock.Bean;

import java.util.List;

/**
 * Created by admin on 13/09/16.
 */
public class EmergenceContact {


    /**
     * crash_id : 1
     * mac_id : D9D36323578D
     * contacts : [{"first_name":"Bill","last_name":"Hader","phone_number":"+15107171635","country_code":"us"},{"first_name":"Kelly","last_name":"Bee","phone_number":"+15107171635","county_code":"us"}]
     * location : {"latitude":37.336103,"longitude":-120.484391}
     */

    private int crash_id;
    private String mac_id;
    private LocationEntity location;
    private List<ContactsEntity> contacts;

    public int getCrash_id() {
        return crash_id;
    }

    public void setCrash_id(int crash_id) {
        this.crash_id = crash_id;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public List<ContactsEntity> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactsEntity> contacts) {
        this.contacts = contacts;
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

    public static class ContactsEntity {
        /**
         * first_name : Bill
         * last_name : Hader
         * phone_number : +15107171635
         * country_code : us
         * county_code : us
         */

        private String first_name;
        private String last_name;
        private String phone_number;
        private String country_code;
        private String county_code;

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getCountry_code() {
            return country_code;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        public String getCounty_code() {
            return county_code;
        }

        public void setCounty_code(String county_code) {
            this.county_code = county_code;
        }
    }
}
