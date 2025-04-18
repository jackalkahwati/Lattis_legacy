package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.utils.UtilHelper;


public class LockStepThree extends Fragment {
    CardView cardView_Yes, cardView_No;
    EditText lockName_EditText;
    TextView textView_content1, textView_content2, textView_content3, textView_content4, textView_yes, textview_no;
    Context mContext;
    String lockName = null;

    public static LockStepThree newInstance() {

        LockStepThree f = new LockStepThree();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_lock_3, container, false);
        mContext = getActivity();
        cardView_Yes = (CardView) v.findViewById(R.id.cv_yes_button);
        lockName_EditText = (EditText) v.findViewById(R.id.et_name);
        cardView_No = (CardView) v.findViewById(R.id.cv_no_button);
        textView_content1 = (TextView) v.findViewById(R.id.tvBluetooth1);
        textView_content2 = (TextView) v.findViewById(R.id.tvBluetooth3);
        textView_content3 = (TextView) v.findViewById(R.id.tvBluetooth4);
        textView_content4 = (TextView) v.findViewById(R.id.tvBluetooth5);
        textView_yes = (TextView) v.findViewById(R.id.tv_yes_button);
        textview_no = (TextView) v.findViewById(R.id.tv_no_button);
        lockName_EditText.setCursorVisible(true);
        lockName_EditText.setTypeface(UtilHelper.getTypface(mContext));
        textView_content1.setTypeface(UtilHelper.getTypface(mContext));
        textView_content2.setTypeface(UtilHelper.getTypface(mContext));
        textView_content3.setTypeface(UtilHelper.getTypface(mContext));
        textView_content4.setTypeface(UtilHelper.getTypface(mContext));
        textView_yes.setTypeface(UtilHelper.getTypface(mContext));
        textview_no.setTypeface(UtilHelper.getTypface(mContext));
        cardView_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNameToDB();

            }
        });
        cardView_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return v;
    }

    private void saveNameToDB() {
        lockName = lockName_EditText.getText().toString().trim();
        if (lockName != null && lockName.length() > 0) {
            lockName = lockName_EditText.getText().toString().trim();
            ((AddLockActivity) getActivity()).addNameToPreference(lockName);
        } else {
            Toast.makeText(getActivity(), "Please enter your Ellipse name", Toast.LENGTH_SHORT).show();
        }

    }
}
