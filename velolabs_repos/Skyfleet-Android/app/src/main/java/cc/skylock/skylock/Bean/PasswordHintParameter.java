package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-01-2017.
 */

public class PasswordHintParameter {
    String confirmation_code;

    public String getPassword_hint() {
        return confirmation_code;
    }

    public void setPassword_hint(String password_hint) {
        this.confirmation_code = password_hint;
    }
}
