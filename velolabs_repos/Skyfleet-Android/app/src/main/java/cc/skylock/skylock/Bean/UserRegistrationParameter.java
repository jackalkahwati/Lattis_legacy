package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 29-01-2016.
 */
public class UserRegistrationParameter {

    /**
     * users_id : phone number or Facebook id
     * user_type : facebook or ellipse
     * reg_id : ﬁrebase id
     * password :  user’s entered password
     */

    private String users_id;
    private String user_type;
    private String reg_id;
    private String password;
    private String phone_number;
    private boolean is_signing_up;

    public boolean is_signing_up() {
        return is_signing_up;
    }

    public void setIs_signing_up(boolean is_signing_up) {
        this.is_signing_up = is_signing_up;
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

    private String country_code;

    public String getUsers_id() {
        return users_id;
    }

    public void setUsers_id(String users_id) {
        this.users_id = users_id;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getReg_id() {
        return reg_id;
    }

    public void setReg_id(String reg_id) {
        this.reg_id = reg_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
