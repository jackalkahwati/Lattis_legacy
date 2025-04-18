package cc.skylock.skylock.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import cc.skylock.skylock.Bean.ProfilePassword;
import cc.skylock.skylock.Bean.ResetPasswordBean;
import cc.skylock.skylock.Bean.UpdateUserDetails;
import cc.skylock.skylock.Bean.UserRegistrationParameter;
import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.PasswordResetActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 08-10-2016.
 */

public class PasswordResetFragment extends Fragment {
    View view;
    public static PasswordResetFragment passwordResetFragment;
    CardView cardView_SavePassword;
    TextView textView_content, textView_hideShow, textView_label_savePassword;
    PasswordResetActivity mPasswordResetActivity;
    EditText editText_Password;
    String password = null;
    PrefUtil mPrefUtil;
    UpdateUserDetails.PropertiesEntity mPropertiesEntity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_password_reset, container, false);
        mPasswordResetActivity = (PasswordResetActivity) getActivity();
        editText_Password = (EditText) view.findViewById(R.id.et_password);
        cardView_SavePassword = (CardView) view.findViewById(R.id.cv_Save_pasword);
        textView_content = (TextView) view.findViewById(R.id.tv_content);
        textView_hideShow = (TextView) view.findViewById(R.id.tv_hideshow);
        textView_label_savePassword = (TextView) view.findViewById(R.id.textView_savePassword);
        textView_hideShow.setVisibility(View.GONE);
        textView_hideShow.setTypeface(UtilHelper.getTypface(mPasswordResetActivity));
        textView_hideShow.setTag("SHOW");
        editText_Password.setError(null);
        mPropertiesEntity = new UpdateUserDetails.PropertiesEntity();
        mPrefUtil = new PrefUtil(mPasswordResetActivity);
        editText_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText_Password.setTypeface(UtilHelper.getTypface(mPasswordResetActivity));
        textView_content.setTypeface(UtilHelper.getTypface(mPasswordResetActivity));
        textView_hideShow.setTypeface(UtilHelper.getTypface(mPasswordResetActivity));
        textView_label_savePassword.setTypeface(UtilHelper.getTypface(mPasswordResetActivity));
        textView_hideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String SHOW = getActivity().getResources().getString(R.string.show);
                final String HIDE = getActivity().getResources().getString(R.string.hide);
                if (textView_hideShow.getTag().equals(SHOW)) {
                    textView_hideShow.setTag(HIDE);
                    textView_hideShow.setText(HIDE);
                    editText_Password.setTransformationMethod(null);
                } else {
                    textView_hideShow.setTag(SHOW);
                    textView_hideShow.setText(SHOW);
                    editText_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        editText_Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    password = editText_Password.getText().toString().trim();
                    if (isPasswordValid(password)) {
                        textView_hideShow.setVisibility(View.VISIBLE);
                        return false;
                    } else {
                        textView_hideShow.setText("Must be 8 - 20 characters");
                        textView_hideShow.setTextColor(Color.parseColor("#F599AE"));
                        return true;
                    }


                }
                return false;
            }
        });
        editText_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Your validation code goes here
                if (s.length() != 0) {
                    textView_hideShow.setVisibility(View.VISIBLE);
                }
                if (s.length() >= 8) {
                    textView_hideShow.setTextColor(Color.parseColor("#A0C8E0"));
                    textView_hideShow.setText(textView_hideShow.getTag().toString());

                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        cardView_SavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = editText_Password.getText().toString().trim();
                validateAndProcessUserData();


            }
        });

        return view;
    }

    private void validateAndProcessUserData() {
        if (!isPasswordValid(password)) {
            textView_hideShow.setVisibility(View.GONE);
            editText_Password.setError(getString(R.string.error_invalid_password));
            return;
        }
        mPropertiesEntity.setUser_id(mPrefUtil.getIntPref(SkylockConstant.PREF_USER_ID,0));
        mPropertiesEntity.setPassword(password);
        final UpdateUserDetails mUpdateUserDetails = new UpdateUserDetails();
        mUpdateUserDetails.setProperties(mPropertiesEntity);
        mPasswordResetActivity.resetPasswordCall(mUpdateUserDetails);

    }

    private boolean isPasswordValid(String password) {
        if (password != null) {
            return password.length() >= 8;
        }
        return false;
    }


    public static PasswordResetFragment newInstance() {
        if (passwordResetFragment == null) {
            passwordResetFragment = new PasswordResetFragment();
        }
        return passwordResetFragment;
    }

}
