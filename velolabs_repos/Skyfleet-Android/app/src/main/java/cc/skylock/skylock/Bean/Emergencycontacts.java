package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 23-01-2016.
 */
public class Emergencycontacts {
    int id;
    String userName;
    String mobileNumber;

    public Emergencycontacts(int id, String userName, String mobileNumber) {
        this.id = id;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
