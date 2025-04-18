package cc.skylock.skylock.utils;

import android.content.Context;

import com.google.gson.Gson;

import java.util.Objects;

import cc.skylock.skylock.Bean.UserRegistrationResponse;

/**
 * Created by Velo Labs Android on 31-01-2017.
 */

public class UserDetailsHelper {
    private static PrefUtil mPrefUtil;


    public static boolean isUserDetailsPresent(Context mContext) {
        mPrefUtil = new PrefUtil(mContext);

        final String userdetailsJson = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_DETAILS, "");
        if (!userdetailsJson.equals("")) {
            Gson gson = new Gson();
            final UserRegistrationResponse userRegistrationResponse = gson.fromJson(userdetailsJson, UserRegistrationResponse.class);
            if (userRegistrationResponse.getPayload() != null) {
                final String firstName = userRegistrationResponse.getPayload().getFirst_name();
                final String lastName = userRegistrationResponse.getPayload().getLast_name();
                if (!Objects.equals(firstName, "") && !Objects.equals(lastName, "")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getAuthToken(Context mContext) {
        mPrefUtil = new PrefUtil(mContext);

        final String userdetailsJson = mPrefUtil.getStringPref(SkylockConstant.PREF_USER_DETAILS, "");
        if (!userdetailsJson.equals("")) {
            final Gson gson = new Gson();
            final UserRegistrationResponse userRegistrationResponse = gson.fromJson(userdetailsJson, UserRegistrationResponse.class);
            if (userRegistrationResponse.getPayload() != null) {
                final String token = userRegistrationResponse.getPayload().getRest_token();
                if (!Objects.equals(token, "")) {
                    return token;
                }
            }
        }
        return null;
    }
}
