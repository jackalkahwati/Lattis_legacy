package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cc.skylock.skylock.R;
import cc.skylock.skylock.adapter.AddressBookDemoAdapter;
import cc.skylock.skylock.ui.HomePageActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;

/**
 * Created by admin on 09/08/16.
 */
public class Contacts extends Fragment {

    View view;
    PrefUtil mPrefUtil;
    Context mContext;
    public static ArrayList<HashMap<String, String>> selectedContactNumber = new ArrayList<>();
    public static String shareLockNumber, sharePersonName;
    public static Contacts contactList = null;
    private String mTypeOfContact;
    AddressBookDemoAdapter addressBookDemoAdapter;
    String header = null;
    EditText searchText;
    String ecList;
    public TextView emergencyContactCount;
    JSONArray ecListJson;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_contact_list, null);
        mPrefUtil = new PrefUtil(getContext());
        searchText = (EditText) view.findViewById(R.id.et_mobilenumbersearch);
        searchText.addTextChangedListener(mSearchTextEditorWatcher);
        RecyclerView contactList = (RecyclerView) view.findViewById(R.id.rv_contacts);
        addressBookDemoAdapter = new AddressBookDemoAdapter(getContext(), "", "", this);
        CardView shareLockBt = (CardView) view.findViewById(R.id.cv_share_button);
        shareLockBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharingChildFragment_LockList.newInstance().shareLockCall(Contacts.shareLockNumber, Contacts.sharePersonName);
            }
        });
        contactList.setLayoutManager(new StickyHeaderLayoutManager());
        contactList.setAdapter(addressBookDemoAdapter);
        RelativeLayout addContactLayout = (RelativeLayout) view.findViewById(R.id.rl_addcontacts);
        TextView addEmergencyContact = (TextView) view.findViewById(R.id.tv_label_emergency_contact_button);
        emergencyContactCount = (TextView) view.findViewById(R.id.tv_label_emergency_contact_count_text);
        TextView shareLockContact = (TextView) view.findViewById(R.id.tv_label_Share_button);
        if (getTypeContact().equalsIgnoreCase("SharingLock")) {
            header = "SHARING";
            shareLockContact.setVisibility(View.GONE);
            addEmergencyContact.setVisibility(View.GONE);
            emergencyContactCount.setVisibility(View.GONE);
            addContactLayout.setVisibility(View.GONE);
        } else {
            header = "EMERGENCY CONTACTS";
            shareLockContact.setVisibility(View.GONE);
            addEmergencyContact.setVisibility(View.VISIBLE);
            addContactLayout.setVisibility(View.VISIBLE);
        }
        updateContactCount();

        addContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray ecJson = new JSONArray(selectedContactNumber);
                Log.i("size", "" + ecJson.length());
                mPrefUtil.setStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, ecJson.toString());

                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager.getFragments() != null) {
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                    }
                }
            }
        });
        shareLockContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharingChildFragment_LockList.newInstance().shareLockCall(shareLockNumber, sharePersonName);
            }
        });
        mContext = getActivity();

        return view;

    }

    @Override
    public void onResume() {
        ((HomePageActivity) getActivity()).changeHeaderUI(header,
                ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimarylightdark, null), Color.WHITE);
        super.onResume();
    }

    private final TextWatcher mSearchTextEditorWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            addressBookDemoAdapter.searchContact(searchText.getText().toString(), searchText.getText().toString());
        }

        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onStop() {
        ((HomePageActivity) getActivity()).changeHeaderUI(header,
                ResourcesCompat.getColor(getResources(),
                        R.color.colorPrimary, null),Color.WHITE);
        super.onStop();

    }

    public void setTypeContact(String typeOfContact) {
        mTypeOfContact = typeOfContact;
    }

    public String getTypeContact() {
        return mTypeOfContact;
    }

    public static Contacts newInstance() {
        if (contactList == null) {
            contactList = new Contacts();
        }
        return contactList;
    }

    public void updateContactCount() {
        try {
            ecList = mPrefUtil.getStringPref(SkylockConstant.PREF_EMERGENCY_CONTACT_LIST, "");
            if (!Objects.equals(ecList, "") && ecList != null) {
                ecListJson = new JSONArray(ecList);
                if (ecListJson.length() == 0) {
                    emergencyContactCount.setVisibility(View.INVISIBLE);
                } else {
                    emergencyContactCount.setVisibility(View.VISIBLE);
                }
                if (ecListJson.length() == 2) {
                    emergencyContactCount.setText(R.string.add_emergency_with_two_contacts);
                } else if (ecListJson.length() == 1) {
                    emergencyContactCount.setText(R.string.add_emergency_with_one_contacts);
                } else if (ecListJson.length() == 3) {
                    emergencyContactCount.setText(R.string.add_emergency_with_zero_contacts);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
