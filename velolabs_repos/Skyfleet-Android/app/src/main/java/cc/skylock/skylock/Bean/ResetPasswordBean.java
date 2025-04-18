package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 08-10-2016.
 */

public class ResetPasswordBean {
    /**
     * password : New Password entered by the User
     * password_hint : 6 digit secret code sent through Text
     */

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
