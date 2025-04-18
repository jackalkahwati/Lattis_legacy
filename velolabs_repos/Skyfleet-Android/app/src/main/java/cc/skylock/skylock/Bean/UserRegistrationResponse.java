package cc.skylock.skylock.Bean;

/**
 * Created by prabhu on 2/4/16.
 */
public class UserRegistrationResponse {


    /**
     * error : null
     * status : 200
     * payload : {"user_id":37,"users_id":"1081323775248240","rest_token":"b02a718e764bbaae8083b079aac398b353c0af179a8f0d2f50ac054048e9ec9f3855700282ebc1c28c263d86c262cb37","username":null,"user_type":"facebook","verified":1,"max_locks":null,"title":"","first_name":"Arun","last_name":"Ravichandran","phone_number":"+919840182541","email":"arunsvcet01@gmail.com","country_code":"in"}
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
         * user_id : 37
         * users_id : 1081323775248240
         * rest_token : b02a718e764bbaae8083b079aac398b353c0af179a8f0d2f50ac054048e9ec9f3855700282ebc1c28c263d86c262cb37
         * username : null
         * user_type : facebook
         * verified : 1
         * max_locks : null
         * title :
         * first_name : Arun
         * last_name : Ravichandran
         * phone_number : +919840182541
         * email : arunsvcet01@gmail.com
         * country_code : in
         */

        private int user_id;
        private String users_id;
        private String rest_token;
        private Object username;
        private String user_type;
        private int verified;
        private Object max_locks;
        private String title;
        private String first_name;
        private String last_name;
        private String phone_number;
        private String email;
        private String country_code;

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

        public String getRest_token() {
            return rest_token;
        }

        public void setRest_token(String rest_token) {
            this.rest_token = rest_token;
        }

        public Object getUsername() {
            return username;
        }

        public void setUsername(Object username) {
            this.username = username;
        }

        public String getUser_type() {
            return user_type;
        }

        public void setUser_type(String user_type) {
            this.user_type = user_type;
        }

        public int getVerified() {
            return verified;
        }

        public void setVerified(int verified) {
            this.verified = verified;
        }

        public Object getMax_locks() {
            return max_locks;
        }

        public void setMax_locks(Object max_locks) {
            this.max_locks = max_locks;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCountry_code() {
            return country_code;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }
    }
}

