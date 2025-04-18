package cc.skylock.skylock.ui.fragment;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.EmergencyContactListAdapter;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by admin on 19/08/16.
 */
public class EmergencyContacts extends Fragment {

    private static EmergencyContacts emergencyContacts = null;
    View view;
    RelativeLayout mEmergencyContactBtLayout;
    RecyclerView mEmergencyContactRecyclerView;
    PrefUtil mPrefUtil;
    ArrayList<HashMap<String, String>> mEmergencyContactList;
    TextView  textView_label_content, textView_label_choosecontacts;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.emergency_contact, null);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        mEmergencyContactBtLayout = (RelativeLayout) view.findViewById(R.id.cv_contacts);
        mEmergencyContactRecyclerView = (RecyclerView) view.findViewById(R.id.rv_ec_contacts_list);
        textView_label_content = (TextView) view.findViewById(R.id.tv_label);
        textView_label_choosecontacts = (TextView) view.findViewById(R.id.textView);
        textView_label_content.setTypeface(UtilHelper.getTypface(mContext));
        textView_label_choosecontacts.setTypeface(UtilHelper.getTypface(mContext));
        JSONArray ecListJson;
        mEmergencyContactList = new ArrayList<>();

        mEmergencyContactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            final String ecList = mPrefUtil.getStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, "");
            if (!Objects.equals(ecList, "") && ecList != null) {
                ecListJson = new JSONArray(ecList);
                for (int noOfEmergencyList = 0; noOfEmergencyList < ecListJson.length(); noOfEmergencyList++) {
                    HashMap<String, String> emergencyContactItem = new HashMap<>();
                    emergencyContactItem.put("number", ecListJson.getJSONObject(noOfEmergencyList).getString("number"));
                    emergencyContactItem.put("id", ecListJson.getJSONObject(noOfEmergencyList).getString("id"));
                    emergencyContactItem.put("name", ecListJson.getJSONObject(noOfEmergencyList).getString("name"));
                    emergencyContactItem.put("photoUrl", ecListJson.getJSONObject(noOfEmergencyList).getString("photoUrl"));
                    mEmergencyContactList.add(emergencyContactItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EmergencyContactListAdapter emergencyContactListAdapter = new EmergencyContactListAdapter(mEmergencyContactList, getContext());
        mEmergencyContactRecyclerView.setAdapter(emergencyContactListAdapter);
        if (mEmergencyContactList.size() >= 1)
            textView_label_content.setVisibility(View.GONE);
        if (mEmergencyContactList.size() < 3) {
            mEmergencyContactBtLayout.setVisibility(View.VISIBLE);
        } else {
            mEmergencyContactBtLayout.setVisibility(View.VISIBLE);
        }
        mEmergencyContactBtLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!weHavePermissionToReadContacts()) {
                    requestReadContactsPermissionFirst();
                } else
                    ShowContactsList();

            }
        });


        return view;

    }

    private boolean weHavePermissionToReadContacts() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadContactsPermissionFirst() {
        if (this.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    public void ShowContactsList() {

     //   Contacts.newInstance().setTypeContact("EmergencyContacts");
        Fragment contactList = Contacts.newInstance();
        Contacts.newInstance().setTypeContact("EmergencyContacts");
        Contacts.selectedContactNumber = mEmergencyContactList;
        ((HomePageActivity) getActivity()).setFragment(contactList, true, "EmergencyContacts");
        ((HomePageActivity) getActivity()).changeHeaderUI("CHOOSE CONTACTS",
                ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimarylightdark, null), Color.WHITE);
    }

    private void requestForResultContactsPermission() {
        this.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 123);
    }

    @Override
    public void onResume() {
        ((HomePageActivity) getActivity()).changeHeaderUI("EMERGENCY CONTACTS",
                ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimarylightdark, null),Color.WHITE);
        super.onResume();
    }

    public static EmergencyContacts newInstance() {
        if (emergencyContacts == null) {
            emergencyContacts = new EmergencyContacts();
        }
        return emergencyContacts;
    }

    private Bitmap retrieveContactPhoto(String contactID) {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContext().getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);

            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return photo;
    }

    public void showContactBt() {
        if (mEmergencyContactList.size() == 0)
            textView_label_content.setVisibility(View.VISIBLE);
        if (mEmergencyContactList.size() < 3) {
            mEmergencyContactBtLayout.setVisibility(View.VISIBLE);
        } else {
            mEmergencyContactBtLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ShowContactsList();
        } else {
            return;
        }
    }
}
