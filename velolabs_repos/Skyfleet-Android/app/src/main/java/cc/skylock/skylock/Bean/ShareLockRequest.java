package cc.skylock.skylock.Bean;

/**
 * Created by admin on 15/08/16.
 */
public class ShareLockRequest {

    /**
     * contact : {"first_name":"Kelly","last_name":"Bee","phone_number":"5107171635","country_code":"us"}
     * lock_id :
     */

    private ContactEntity contact;
    private String lock_id;

    public ContactEntity getContact() {
        return contact;
    }

    public void setContact(ContactEntity contact) {
        this.contact = contact;
    }

    public String getLock_id() {
        return lock_id;
    }

    public void setLock_id(String lock_id) {
        this.lock_id = lock_id;
    }

    public static class ContactEntity {
        /**
         * first_name : Kelly
         * last_name : Bee
         * phone_number : 5107171635
         * country_code : us
         */

        private String first_name;
        private String last_name;
        private String phone_number;
        private String country_code;

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
    }
}
