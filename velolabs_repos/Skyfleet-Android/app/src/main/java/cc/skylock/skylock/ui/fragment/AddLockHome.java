package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddFriendEllipseActivity;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 18-07-2016.
 */
public class AddLockHome extends Fragment {
    CardView cardView_onboardprocess, cardView_shareOnboardprocess;
    TextView textView_description1, textView_description2, textView_cv_label, textView_description3, textView_share_label;
    Context mContext;
    RelativeLayout relativeLayout_add_Friends_lock;

    public static AddLockHome newInstance() {

        AddLockHome f = new AddLockHome();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_addlock_home, container, false);
        mContext = getActivity();
        cardView_onboardprocess = (CardView) v.findViewById(R.id.cv_onboard);
        cardView_shareOnboardprocess = (CardView) v.findViewById(R.id.cv_share);
        textView_description1 = (TextView) v.findViewById(R.id.textView_description1);
        textView_description2 = (TextView) v.findViewById(R.id.textView_description2);
        textView_cv_label = (TextView) v.findViewById(R.id.textView);
        relativeLayout_add_Friends_lock = (RelativeLayout) v.findViewById(R.id.rl_add_firends_lock);
        textView_description3 = (TextView) v.findViewById(R.id.textView_description3);
        textView_share_label = (TextView) v.findViewById(R.id.textView_existing_user);
        textView_description1.setTypeface(UtilHelper.getTypface(mContext));
        textView_description2.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label.setTypeface(UtilHelper.getTypface(mContext));
        textView_description3.setTypeface(UtilHelper.getTypface(mContext));
        textView_share_label.setTypeface(UtilHelper.getTypface(mContext));
        //relativeLayout_add_Friends_lock.setEnabled(false);
        cardView_onboardprocess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((AddLockActivity) getActivity()).selectPage(1);

            }
        });
        relativeLayout_add_Friends_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), AddFriendEllipseActivity.class));
                getActivity().finish();
            }
        });
        return v;
    }
}
