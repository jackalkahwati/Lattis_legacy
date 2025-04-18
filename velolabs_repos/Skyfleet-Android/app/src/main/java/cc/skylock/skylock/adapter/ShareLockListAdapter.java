package cc.skylock.skylock.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cc.skylock.skylock.Bean.ShareUnshareList;
import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.alert.CentralizedAlertDialog;
import cc.skylock.skylock.ui.fragment.SharingChildFragment_LockList;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by admin on 12/06/16.
 */
public class ShareLockListAdapter extends RecyclerView.Adapter<ShareLockListAdapter.LockListViewHolder> implements View.OnClickListener {

    private ArrayList<HashMap<String, String>> mTotalLockListData;
    public static Context mContext;
    SharingChildFragment_LockList mSharingChildFragment_lockList;
    PrefUtil mPrefUtil;
    private String sharedto = null;


    public ShareLockListAdapter(ArrayList<HashMap<String, String>> mTotalLockListData, SharingChildFragment_LockList mFragment) {
        this.mTotalLockListData = mTotalLockListData;
        this.mContext = mFragment.getActivity();
        this.mSharingChildFragment_lockList = mFragment;
        mPrefUtil = new PrefUtil(mContext);
    }


    @Override
    public LockListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LockListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_share_lock, parent, false));
    }

    @Override
    public void onBindViewHolder(final LockListViewHolder holder, final int position) {
        holder.lockName.setText(mTotalLockListData.get(position).get("LOCK_NAME"));
        if (mPrefUtil != null && mPrefUtil.getBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + mTotalLockListData.get(position).get("LOCK_MACID"), false)) {
            holder.unshare_RelativeLayout.setVisibility(View.VISIBLE);
            holder.textView_description.setVisibility(View.GONE);
            holder.textView_label.setText("SHARE WITH ANOTHER FRIEND");
            sharedto = mPrefUtil.getStringPref(SkylockConstant.PREF_KEY_LOCK_SHARED_TO + mTotalLockListData.get(position).get("LOCK_ID"), "");
            if (sharedto != "") {
                holder.textView_sharedTo.setText("Shared with " + sharedto);
            } else {
                sharedto = "Your friend ";
                holder.textView_sharedTo.setText("Shared with  your friend");
            }


        } else {
            holder.unshare_RelativeLayout.setVisibility(View.GONE);
            holder.textView_label.setText("SHARE NOW");
            holder.textView_description.setVisibility(View.VISIBLE);
        }

        holder.cardView_shareNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrefUtil != null && !mPrefUtil.getBooleanPref(SkylockConstant.PREF_KEY_SHARED_LOCK + mTotalLockListData.get(position).get("LOCK_MACID"), false)) {
                    mSharingChildFragment_lockList.handleContactsPermission(position);
                    SharingChildFragment_LockList.currentSharingLockID = mTotalLockListData.get(position).get("LOCK_ID");
                } else {
                    final String header = mContext.getResources().getString(R.string.info);
                    final String message = mContext.getResources().getString(R.string.reshare_warning);
                    CentralizedAlertDialog.showDialog(mContext, header, sharedto + " " + message, 0);
                }

            }
        });
        holder.cardView_shareStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sharedtoID = mTotalLockListData.get(position).get("SHARED_TO_USER_ID");
                final String shareID = mTotalLockListData.get(position).get("SHARE_ID");
                final String lockMacid = mTotalLockListData.get(position).get("LOCK_MACID");
                final String lockId = mTotalLockListData.get(position).get("LOCK_ID");
                mSharingChildFragment_lockList.stopSharing(shareID, sharedtoID, lockMacid, lockId);
            }
        });

    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public int getItemCount() {
        return mTotalLockListData.size();
    }


    public static class LockListViewHolder extends RecyclerView.ViewHolder {

        public TextView lockName, textView_description, textView_label, textView_label_unshare, textView_sharedTo;
        public CardView cardView_shareNow, cardView_shareStop;
        public RelativeLayout unshare_RelativeLayout;

        public LockListViewHolder(View itemView) {
            super(itemView);
            lockName = (TextView) itemView.findViewById(R.id.tv_share_lockName);
            textView_description = (TextView) itemView.findViewById(R.id.tv_share_content);
            cardView_shareNow = (CardView) itemView.findViewById(R.id.cv_share_now);
            cardView_shareStop = (CardView) itemView.findViewById(R.id.cv_stop_share);
            textView_label = (TextView) itemView.findViewById(R.id.tv_label);
            unshare_RelativeLayout = (RelativeLayout) itemView.findViewById(R.id.rl_share_layout);
            textView_label_unshare = (TextView) itemView.findViewById(R.id.tv_label_stop);
            textView_sharedTo = (TextView) itemView.findViewById(R.id.tv_shared_to);
            textView_sharedTo.setTypeface(UtilHelper.getTypface(mContext));
            textView_description.setTypeface(UtilHelper.getTypface(mContext));
            textView_label_unshare.setTypeface(UtilHelper.getTypface(mContext));
            lockName.setTypeface(UtilHelper.getTypface(mContext));
            textView_label.setTypeface(UtilHelper.getTypface(mContext));

        }
    }

}
