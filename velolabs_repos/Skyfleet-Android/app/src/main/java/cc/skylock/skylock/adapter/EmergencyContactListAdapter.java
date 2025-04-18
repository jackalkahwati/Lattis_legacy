package cc.skylock.skylock.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.fragment.EmergencyContacts;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by admin on 24/08/16.
 */
public class EmergencyContactListAdapter extends RecyclerView.Adapter<EmergencyContactListAdapter.EmergencyContactListViewHolder> {

    private ArrayList<HashMap<String, String>> mEmergencyContactList;
    private Context mContext;
    private PrefUtil mPrefUtil;

    public EmergencyContactListAdapter(ArrayList<HashMap<String, String>> emergencyContactList, Context context) {
        this.mEmergencyContactList = emergencyContactList;
        this.mContext = context;
        mPrefUtil = new PrefUtil(this.mContext);
    }


    @Override
    public EmergencyContactListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EmergencyContactListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emergency_contact_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final EmergencyContactListViewHolder holder, final int position) {
        holder.personNameTextView.setText(mEmergencyContactList.get(position).get("name"));
        holder.removeECNumber.setText(mEmergencyContactList.get(position).get("number"));
//
//        Uri uri = Uri.parse(mEmergencyContactList.get(position).get("photoUrl"));
//        Bitmap bitmap = Contacts.People.loadContactPhoto(mContext, uri, R.drawable.em_contacts, null);
        Bitmap photo = UtilHelper.openPhoto(mContext, Long.parseLong(mEmergencyContactList.get(position).get("id")));

        // SET IT HERE IN THE IMAGEVIEW
        holder.profilePic.setImageBitmap(photo);
        holder.iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int noOfSelectedContactNumber = 0; noOfSelectedContactNumber < mEmergencyContactList.size(); noOfSelectedContactNumber++) {
                    if (mEmergencyContactList.get(noOfSelectedContactNumber).get("number").equalsIgnoreCase(mEmergencyContactList.get(position).get("number"))) {
                        mEmergencyContactList.remove(noOfSelectedContactNumber);
                        JSONArray ecJson = new JSONArray(mEmergencyContactList);
                        mPrefUtil.setStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, ecJson.toString());
                        EmergencyContacts.newInstance().showContactBt();
                    }
                }
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mEmergencyContactList.size();
    }

    public static class EmergencyContactListViewHolder extends RecyclerView.ViewHolder {

        TextView personNameTextView, removeECNumber,iv_remove;
        ImageView profilePic;

        public EmergencyContactListViewHolder(View itemView) {
            super(itemView);
            personNameTextView = (TextView) itemView.findViewById(R.id.tv_selected_ec_lockName);
            profilePic = (ImageView) itemView.findViewById(R.id.iv_selected_ec_icon);
            removeECNumber = (TextView) itemView.findViewById(R.id.tv_selected_ec_number);
            iv_remove = (TextView) itemView.findViewById(R.id.iv_em_close);
        }
    }
}
