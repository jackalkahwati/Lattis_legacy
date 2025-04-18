package cc.skylock.skylock.Bean;

import com.facebook.Profile;

/**
 * Created by Velo Labs Android on 24-01-2016.
 */
public class Facebook {
   public static Profile profile;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }


    public Facebook(Profile profile) {
        this.profile = profile;
    }
}
