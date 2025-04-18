package cc.skylock.skylock.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.PasswordResetActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 08-10-2016.
 */

public class EnterSecretCodeFragment extends Fragment {
    View view;
    public static EnterSecretCodeFragment enterSecretCodeFragment;
    CardView cardView_enterCode, cardView_submit;
    private static String phNumber = null;
    TextView textView_verificationText, textView_resend, textView_submitcode;
    EditText editText_enterCode;
    PrefUtil mPrefUtil;
    PasswordResetActivity passwordResetActivity;

    public static EnterSecretCodeFragment newInstance() {
        if (enterSecretCodeFragment == null) {
            enterSecretCodeFragment = new EnterSecretCodeFragment();
        }
        return enterSecretCodeFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_enter_secret_code, container, false);
        passwordResetActivity = (PasswordResetActivity) getActivity();
        mPrefUtil = new PrefUtil(passwordResetActivity);
        textView_verificationText = (TextView) view.findViewById(R.id.tv_verificationcontent);
        textView_submitcode = (TextView) view.findViewById(R.id.textView_submitcode);
        textView_resend = (TextView) view.findViewById(R.id.tv_resend);
        editText_enterCode = (EditText) view.findViewById(R.id.et_code);
        cardView_enterCode = (CardView) view.findViewById(R.id.cv_code);
        cardView_submit = (CardView) view.findViewById(R.id.cv_Submit);
        textView_verificationText.setTypeface(UtilHelper.getTypface(passwordResetActivity));
        textView_resend.setTypeface(UtilHelper.getTypface(passwordResetActivity));
        textView_submitcode.setTypeface(UtilHelper.getTypface(passwordResetActivity));
        editText_enterCode.setTypeface(UtilHelper.getTypface(passwordResetActivity));
        phNumber = mPrefUtil.getStringPref("KEY_TEMP_CC_MN", "");
        textView_verificationText.setText("We've sent a 6 digit reset code to " + phNumber + ". Please enter it now");
        cardView_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String secretCode = editText_enterCode.getText().toString();
                if (secretCode != null && secretCode.length() == 6) {
                    passwordResetActivity.sendSecrectCode(secretCode);
                } else {
                    Toast.makeText(getActivity(), "Invalid code", Toast.LENGTH_LONG).show();
                }
            }
        });
        textView_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_resend.setClickable(false);
                textView_resend.setTextColor(Color.parseColor("#7a7a7a"));
                final String user_mobileNumber = mPrefUtil.getStringPref(Myconstants.KEY_USER_PHONE_NUMBER, "");
                passwordResetActivity.requestCodeCall(user_mobileNumber);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView_resend.setTextColor(Color.WHITE);
                        textView_resend.setClickable(true);

                    }
                }, 1000 * 120);
            }
        });


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        editText_enterCode.setText("");
    }
}
