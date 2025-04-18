package cc.skylock.skylock.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 30-07-2016.
 */
public class SharingChildFragment_Home extends Fragment {
    View view;
    CardView cardView_startbutton;
    TextView textView_label_content_one, textView_cv_label;
    Context mContext;
    ImageView imageView_close;
    public static SharingChildFragment_Home sharingChildFragment_home = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.childfragment_sharing_home, null);
        mContext = getActivity();
        showAlerDialog(mContext);
        return view;

    }

    private void showAlerDialog(Context mContext) {
        final Dialog dialog = new Dialog(mContext, android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.alert_sharing_home);
        dialog.setCancelable(false);
        imageView_close = (ImageView) dialog.findViewById(R.id.iv_close);
        textView_label_content_one = (TextView) dialog.findViewById(R.id.tv_content1);
        textView_cv_label = (TextView) dialog.findViewById(R.id.tv_label_startbutton);
        cardView_startbutton = (CardView) dialog.findViewById(R.id.cv_start_button);
        textView_label_content_one.setTypeface(UtilHelper.getTypface(mContext));
        textView_cv_label.setTypeface(UtilHelper.getTypface(mContext));
        final String one = "<font color='#57D8FF'>" + "HOW DO I SHARE MY ELLIPSE? " + "</font>";
        final String two = "<font color='#9B9B9B'>" + "<br/><br/> You can share your Ellipse with any of your phone contacts.  Just choose a contact and weʼll SMS them an invitation. " + "</font>";
        final String three = "<font color='#57D8FF'>" + "<br/><br/><br/> WHAT DO MY FRIENDS NEED TO DO?" + "</font>";
        final String four = "<font color='#9B9B9B'>" + "<br/><br/> Theyʼll need to install the Ellipse app and once theyʼve accepted your invitation, they can start using your Ellipse.  Theyʼll be able to lock and unlock it just like you can but they wonʼt be able to change any of your settings.";
        textView_label_content_one.setText(Html.fromHtml((one + two + three + four)));
        cardView_startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment sharingChildFragment_LockList = SharingChildFragment_LockList.newInstance();
                ((HomePageActivity) getActivity()).setFragment(sharingChildFragment_LockList, true, "SharingChildFragment_LockList");
                dialog.cancel();
            }
        });
        imageView_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomePageActivity) getActivity()).hideHeaderLayout();
                removeFragment();
                dialog.cancel();
            }

        });
        dialog.show();

    }

    private void removeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getFragments() != null) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        }

    }

    public static SharingChildFragment_Home newInstance() {
        if (sharingChildFragment_home == null) {
            sharingChildFragment_home = new SharingChildFragment_Home();
        }
        return sharingChildFragment_home;
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}


