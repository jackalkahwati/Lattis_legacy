package cc.skylock.skylock.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.fragment.MyEllipsesFragment;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by admin on 12/06/16.
 */
public class LockListAdapter extends RecyclerSwipeAdapter<LockListAdapter.LockListViewHolder> implements View.OnClickListener {

    private ArrayList<HashMap<String, String>> mSkylockList;
    boolean settingClick = false;
    private MyEllipsesFragment myEllipsesFragment;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private String shareID, shareToUserId, lockMacId, lockId;

    public LockListAdapter(Context mContext, ArrayList<HashMap<String, String>> skylockList, MyEllipsesFragment myEllipsesFragment) {
        this.mSkylockList = skylockList;
        this.myEllipsesFragment = myEllipsesFragment;
        this.mContext = mContext;
        mPrefUtil = new PrefUtil(mContext);
    }

    public void setSkylockList(ArrayList<HashMap<String, String>> skylockList) {
        this.mSkylockList = skylockList;
    }

    @Override
    public LockListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LockListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final LockListViewHolder holder, int position) {

        final int itemPosition = holder.getAdapterPosition();
        holder.textView_connect.setTypeface(UtilHelper.getTypface(mContext));
        holder.lockName.setTypeface(UtilHelper.getTypface(mContext));
        holder.textView_delete.setTypeface(UtilHelper.getTypface(mContext));
        holder.textView_lastconnect.setTypeface(UtilHelper.getTypface(mContext));
        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.settingLockLayout);
        holder.swipeLayout.setLeftSwipeEnabled(false);
        if (mSkylockList != null && mSkylockList.size() > 0) {

            holder.lockName.setText(mSkylockList.get(itemPosition).get("LOCK_NAME"));
            try {
                lockMacId = mSkylockList.get(itemPosition).get("LOCK_MACID");
                shareID = mSkylockList.get(itemPosition).get("SHARE_ID");
                lockId = mSkylockList.get(itemPosition).get("LOCK_ID");
                final String timestamp = mPrefUtil.getStringPref(lockMacId + SkylockConstant.LAST_CONNECTED_TIMESTAMP, "");
                if (!timestamp.equals("")) {
                    String time = UtilHelper.getDateCurrentTimeZone(Long.parseLong(timestamp));
                    holder.textView_lastconnect.setText("Last connected on" + " " + time);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mSkylockList.get(itemPosition).get("USER_TYPE").equals("BORROWER")) {
                final String unshare = mContext.getResources().getString(R.string.unshare);
                holder.textView_delete.setText(unshare);
            }

        }


        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
        });
        holder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                //            Toast.makeText(mContext, "DoubleClick", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteLock_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.isNetworkAvailable(mContext)) {
                    if (mSkylockList.get(itemPosition).get("USER_TYPE").equals("BORROWER")) {
                        shareToUserId = mSkylockList.get(itemPosition).get("SHARE_TO_USER_ID");
                        myEllipsesFragment.stopSharing(shareID, shareToUserId, lockMacId, lockId);
                    } else {
                        myEllipsesFragment.showDeleteScreen(mSkylockList.get(itemPosition).get("LOCK_MACID"), false);
                    }

                } else
                    Toast.makeText(mContext, "No network connection", Toast.LENGTH_SHORT).show();
            }
        });
        holder.connectLock_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myEllipsesFragment.bleConnection(mSkylockList.get(itemPosition).get("LOCK_MACID"), UtilHelper.getMD5Hash(mSkylockList.get(itemPosition).get("USER_ID")));
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public int getItemCount() {
        return mSkylockList.size();
    }


    public static class LockListViewHolder extends RecyclerView.ViewHolder {

        public TextView lockName, textView_lastconnect, textView_delete, textView_connect;
        public ImageView iv_deleteLock, iv_connect;
        public RelativeLayout lockLayout, settingLockLayout, deleteLock_layout, connectLock_layout;
        public ImageView settingLock;
        SwipeLayout swipeLayout;

        public LockListViewHolder(View itemView) {
            super(itemView);
            textView_connect = (TextView) itemView.findViewById(R.id.tv_connect);
            textView_delete = (TextView) itemView.findViewById(R.id.tv_delete);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeLockList);
            lockName = (TextView) itemView.findViewById(R.id.tv_lockName);
            textView_lastconnect = (TextView) itemView.findViewById(R.id.tv_lastconnect);
            settingLock = (ImageView) itemView.findViewById(R.id.iv_lockSetting);
            lockLayout = (RelativeLayout) itemView.findViewById(R.id.rl_lockLayout);
            iv_deleteLock = (ImageView) itemView.findViewById(R.id.iv_deletelock);
            iv_connect = (ImageView) itemView.findViewById(R.id.iv_connect);
            settingLockLayout = (RelativeLayout) itemView.findViewById(R.id.lock_setting_layout);
            deleteLock_layout = (RelativeLayout) itemView.findViewById(R.id.rl_deletelock);
            connectLock_layout = (RelativeLayout) itemView.findViewById(R.id.rl_connnectlock);

        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipeSelectedLock;
    }
}
