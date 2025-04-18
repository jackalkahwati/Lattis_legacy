package cc.skylock.skylock.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddFriendEllipseActivity;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 11-10-2016.
 */

public class EnterShareCodeFragment extends Fragment {
    CardView cardView_connectNow;
    TextView textView_label_one, textView_cv_label_one, textView_content;
    EditText editText_shareCode;
    String enterCode;
    AddFriendEllipseActivity mAddFriendEllipseActivity;
    /**
     * status_code : share code
     */
    ImageView imageView_close;
    public static EnterShareCodeFragment newInstance() {

        EnterShareCodeFragment f = new EnterShareCodeFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_share_code, container, false);
        mAddFriendEllipseActivity = (AddFriendEllipseActivity) getActivity();
        imageView_close = (ImageView) v.findViewById(R.id.iv_close);
        cardView_connectNow = (CardView) v.findViewById(R.id.cv_connect_now);
        textView_label_one = (TextView) v.findViewById(R.id.textView_label_code);
        textView_cv_label_one = (TextView) v.findViewById(R.id.textView_signup_user);
        editText_shareCode = (EditText) v.findViewById(R.id.et_enetr_code);
        textView_content = (TextView) v.findViewById(R.id.textView_content);
        textView_label_one.setTypeface(UtilHelper.getTypface(mAddFriendEllipseActivity));
        textView_cv_label_one.setTypeface(UtilHelper.getTypface(mAddFriendEllipseActivity));
        editText_shareCode.setTypeface(UtilHelper.getTypface(mAddFriendEllipseActivity));
        textView_content.setTypeface(UtilHelper.getTypface(mAddFriendEllipseActivity));
        cardView_connectNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enterCode = editText_shareCode.getText().toString().trim();
                if (enterCode != null && enterCode.length() == 6) {
                    mAddFriendEllipseActivity.acceptSharingCall(enterCode);
                } else {
                    Toast.makeText(getActivity(), "Please enter the valid code", Toast.LENGTH_LONG).show();
                }

            }
        });
        imageView_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), AddLockActivity.class).putExtra("ADD_LOCK", "HOME"));
                getActivity().finish();
            }
        });
        return v;
    }



}
