package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.utils.UtilHelper;


public class LockStepOne extends Fragment {
    CardView yes_CardView;
    TextView textView_Title, textView_description, textView_touchbutton;
    Context mContext;

    public static LockStepOne newInstance() {

        LockStepOne f = new LockStepOne();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_lock_1, container, false);
        mContext = getActivity();
        yes_CardView = (CardView) v.findViewById(R.id.cv_touch_button);
        textView_Title = (TextView) v.findViewById(R.id.tv_title);
        textView_description = (TextView) v.findViewById(R.id.tv_description);
        textView_touchbutton = (TextView) v.findViewById(R.id.tv_touch_button);
        textView_Title.setTypeface(UtilHelper.getTypface(mContext));
        textView_description.setTypeface(UtilHelper.getTypface(mContext));
        textView_touchbutton.setTypeface(UtilHelper.getTypface(mContext));
        yes_CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((AddLockActivity) getActivity()).selectPage(2);

            }
        });
        return v;
    }
}
