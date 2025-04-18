package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 20-09-2016.
 */
public class UpdateUserDetails {


    /**
     * properties : {"user_id":1,"first_name":"Billy","last_name":"Kid","member_type":"some_type","title":"captain","gender":"male","address1":"1832 Canal Dr.","city":"Atwater","state":"Ca","zip_or_postal_code":"95301","country":"USA"}
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
         * user_id : 1
         * first_name : Billy
         * last_name : Kid
         * member_type : some_type
         * title : captain
         * gender : male
         * address1 : 1832 Canal Dr.
         * city : Atwater
         * state : Ca
         * zip_or_postal_code : 95301
         * country : USA
         */

        private int user_id;
        private String first_name;
        private String last_name;
        private String member_type;
        private String title;
        private String gender;
        private String address1;
        private String city;
        private String state;
        private String zip_or_postal_code;
        private String country;
        private String email;
        private String country_code;

        public String getCountry_code() {
            return country_code;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        private String phone_number;


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getUser_id() {
            return user_id;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

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

        public String getMember_type() {
            return member_type;
        }

        public void setMember_type(String member_type) {
            this.member_type = member_type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getAddress1() {
            return address1;
        }

        public void setAddress1(String address1) {
            this.address1 = address1;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZip_or_postal_code() {
            return zip_or_postal_code;
        }

        public void setZip_or_postal_code(String zip_or_postal_code) {
            this.zip_or_postal_code = zip_or_postal_code;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}

