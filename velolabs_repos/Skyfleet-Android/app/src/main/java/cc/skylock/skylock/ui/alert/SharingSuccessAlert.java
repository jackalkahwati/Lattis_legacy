package cc.skylock.skylock.ui.alert;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddFriendEllipseActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 23-10-2016.
 */

public class SharingSuccessAlert extends DialogFragment {
    private View mRootView = null;
    private static final String SHARE_STATUS = "shareStatus";
    private String sharedLockMacId = null;
    private static final String SUCCESS_TEXT = "Success";
    private static final String FAILURE_TEXT = "Invalid code";
    private static final String FAILURE_BODY_TEXT = "Please check your invitation code and try again.  If you have not received an invitation, ask the Ellipse owner to try again.\n";
    private PrefUtil mPrefUtil;
    public static SharingSuccessAlert newInstance(String sharedLockMacId) {
        SharingSuccessAlert mSharingSuccessAlert = new SharingSuccessAlert();
        Bundle args = new Bundle();
        args.putString(SHARE_STATUS, sharedLockMacId);
        mSharingSuccessAlert.setArguments(args);
        return mSharingSuccessAlert;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.alert_sharing_success_fail, container);
        getDialog().getWindow().requestFeature(STYLE_NO_TITLE);
        Bundle arguments = getArguments();
        if (arguments != null) {
            sharedLockMacId = arguments.getString(SHARE_STATUS);
        }
        final TextView textView_status = (TextView) mRootView.findViewById(R.id.textView_sharing_status);
        final TextView textView_content = (TextView) mRootView.findViewById(R.id.textView_content);
        final TextView textView_connectNow = (TextView) mRootView.findViewById(R.id.textView_connect_now);
        final TextView textView_connectLater = (TextView) mRootView.findViewById(R.id.textView_connect_later);
        final CardView cardView_connectNow = (CardView) mRootView.findViewById(R.id.cv_connect_now);
        final CardView cardView_connectLater = (CardView) mRootView.findViewById(R.id.cv_connect_later);
        textView_status.setTypeface(UtilHelper.getTypface(getActivity()));
        textView_content.setTypeface(UtilHelper.getTypface(getActivity()));
        textView_connectNow.setTypeface(UtilHelper.getTypface(getActivity()));
        textView_connectLater.setTypeface(UtilHelper.getTypface(getActivity()));
        cardView_connectNow.setVisibility(View.GONE);
        cardView_connectLater.setVisibility(View.GONE);
        mPrefUtil = new PrefUtil(getActivity());
        if (sharedLockMacId != null) {
            final String LOCKNAME = mPrefUtil.getStringPref(sharedLockMacId,"");
            final String SUCCESS_BODY_TEXT = "Youʼve accepted the invitation and can begin sharing" +" "+ LOCKNAME +" "+ " Ellipse as soon as you are within range of it.";
            textView_status.setText(SUCCESS_TEXT);
            textView_content.setText(SUCCESS_BODY_TEXT);
            cardView_connectNow.setVisibility(View.VISIBLE);
            cardView_connectLater.setVisibility(View.VISIBLE);
        } else {
            textView_status.setText(FAILURE_TEXT);
            textView_content.setText(FAILURE_BODY_TEXT);
            cardView_connectNow.setVisibility(View.VISIBLE);
            cardView_connectLater.setVisibility(View.GONE);
            textView_connectNow.setText(R.string.try_again);
        }

        cardView_connectNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedLockMacId != null) {
                    ((AddFriendEllipseActivity) getActivity()).callConnectFragment();
                    getDialog().dismiss();
                } else {
                    getDialog().dismiss();
                }
            }
        });
        cardView_connectLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                getActivity().finish();
            }
        });
        Window window = getDialog().getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        return mRootView;
    }
}
