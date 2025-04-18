package cc.skylock.skylock.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import java.sql.SQLException;

import cc.skylock.skylock.Database.Dbfunction;
import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.DeleteAccountActivity;
import cc.skylock.skylock.ui.PofilePasswordActivity;
import cc.skylock.skylock.ui.SplashActivity;
import cc.skylock.skylock.ui.UpdatePhoneNumberActivity;
import cc.skylock.skylock.ui.fragment.UserProfileFragment;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 01-08-2016.
 */
public class AccountSettingsMenuApater extends RecyclerView.Adapter<AccountSettingsMenuApater.AccountSettingsMenuViewHolder> implements View.OnClickListener {
    Context context;
    String[] mTitle;
    LayoutInflater inflater;
    PrefUtil mPrefUtil;
    Profile profile;
    UserProfileFragment mUserProfileFragment;
    boolean mFacebooklogin;
    Dbfunction  dbfunction;


    public AccountSettingsMenuApater(Context mContext, String[] title, UserProfileFragment userProfileFragment, boolean isFacebooklogin) {
        this.context = mContext;
        this.mTitle = title;
        inflater = LayoutInflater.from(context);
        mPrefUtil = new PrefUtil(context);
        FacebookSdk.sdkInitialize(mContext.getApplicationContext());
        profile = Profile.getCurrentProfile();
        this.mUserProfileFragment = userProfileFragment;
        this.mFacebooklogin = isFacebooklogin;
        dbfunction = new Dbfunction(context);


    }


    @Override
    public AccountSettingsMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AccountSettingsMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_account_settings, parent, false));
    }

    @Override
    public void onBindViewHolder(AccountSettingsMenuViewHolder holder, final int position) {

        holder.tvTitle.setTypeface(UtilHelper.getTypface(context));
        holder.tvTitle.setText(mTitle[position]);
        if (position == 1 && mFacebooklogin) {
            holder.rl_row_content.setVisibility(View.GONE);
            holder.lineView.setVisibility(View.GONE);
        }
        holder.imageView_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    context.startActivity(new Intent(context, UpdatePhoneNumberActivity.class));
                } else if (position == 1) {
                    context.startActivity(new Intent(context, PofilePasswordActivity.class));
                } else if (position == 2) {
                    context.startActivity(new Intent(context, DeleteAccountActivity.class).putExtra("DELETION_TYPE",0));
                } else if (position == 3) {
                    showLogoutDialog();
                }


            }
        });

    }

    private void showLogoutDialog() {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.setContentView(R.layout.alert_logout);
        TextView textView_label_cancel = (TextView) dialog.findViewById(R.id.tv_label_cancel);
        TextView textView_label_Logout = (TextView) dialog.findViewById(R.id.tv_label_logout);
        TextView textview_content = (TextView) dialog.findViewById(R.id.tv_content);
        textView_label_cancel.setTypeface(UtilHelper.getTypface(context));
        textView_label_Logout.setTypeface(UtilHelper.getTypface(context));
        textview_content.setTypeface(UtilHelper.getTypface(context));
        textView_label_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        textView_label_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrefUtil.getBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false)) {
                    if (profile != null) {
                        LoginManager.getInstance().logOut();
                        mPrefUtil.setBooleanPref(Myconstants.KEY_FACBOOK_LOGIN, false);
                    }
                }
                try {
                    dbfunction.open();
                    dbfunction.deleteAccount();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (dbfunction.isOpen())
                        dbfunction.close();
                }
                mUserProfileFragment.deleteProfilePic();
                mUserProfileFragment.closeLockConnection();
                mPrefUtil.clearAllPref();
                mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_LOGIN_STRING, false);
                ((Activity) context).finish();
                dialog.cancel();
                final Intent intent = new Intent(context, SplashActivity.class);
                context.startActivity(intent);
                mPrefUtil.setBooleanPref(Myconstants.KEY_FIRST_TIME_WALK_THROUGH_STRING, true);
                UtilHelper.analyticTrackUserAction("LogOut","Custom"," ",null, "ANDROID");
            }
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return mTitle.length;
    }

    @Override
    public void onClick(View v) {

    }

    public static class AccountSettingsMenuViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ImageView imageView_arrow;
        RelativeLayout rl_row_content;
        View lineView;

        public AccountSettingsMenuViewHolder(View itemView) {
            super(itemView);
            rl_row_content = (RelativeLayout) itemView.findViewById(R.id.rl_row_content);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_label_name);
            imageView_arrow = (ImageView) itemView.findViewById(R.id.tv_image_arrow);
            lineView = (View) itemView.findViewById(R.id.lineview);
        }


    }
}
