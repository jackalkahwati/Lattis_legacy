package cc.skylock.skylock;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by AlexVijayRaj on 8/11/2015.
 */
public class Sharing {

    Context context;
    Dialog dialog;
    ImageButton ibBack;
    Button bAddContacts;
    AddContacts objAddContacts;
    ListView lvFBFriends;
    ObjectRepo objRepo;
    int facebookFriendSize = 0;
    ArrayList<String> facebookFriendNameList = null;
    ArrayList<String> facebookFriendIdList = null;
    public static ArrayList<String> list = new ArrayList<>();
    public static ArrayAdapter adapter;

    public Sharing(Context context1, ObjectRepo objRepo1) {
        context = context1;
        objRepo = objRepo1;


        init();
        setOnClickListeners();
    }

    private void init() {
        objAddContacts = new AddContacts(context);

        Log.i("test", "intialization");
        dialog = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogZoom;
        dialog.setContentView(R.layout.sharing_dialog);

        ibBack = (ImageButton) dialog.findViewById(R.id.ibBack);
        lvFBFriends = (ListView) dialog.findViewById(R.id.lvFBFriends);
        bAddContacts = (Button) dialog.findViewById(R.id.bAddContacts);


        facebookFriendNameList = new ArrayList<String>();
        facebookFriendIdList = new ArrayList<String>();

        populateFacebookFriendList();

    }

    private void setOnClickListeners() {

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
// share new contacts flow-----
        bAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                objAddContacts.showAddContacts();
            }
        });

    }

    public void showSharing() {
        dialog.show();
    }

    public void populateFacebookFriendList() {

        FBFriendsArrayAdapter adapter = new FBFriendsArrayAdapter(context, objRepo, facebookFriendSize, facebookFriendIdList, facebookFriendNameList, objAddContacts);

        lvFBFriends.setAdapter(adapter);

    }

    public void putFacebookFriendSize(int size) {
        facebookFriendSize = size;
        //populateFacebookFriendList();
    }

    public void putFacebookFriendNameList(ArrayList<String> List) {
        facebookFriendNameList = List;
        //populateFacebookFriendList();
    }

    public void putFacebookFriendIDList(ArrayList<String> List) {
        facebookFriendIdList = List;
        //populateFacebookFriendList();
    }
}


class AddContacts {
    Context context;
    Dialog dialog;
    ImageButton ibBack;
    ListView lvAddContacts;
    SearchView searchView;
    ArrayAdapter adapter;
    Filter filter;
    InviteContactsAdapter inviteContactsAdapter;

    public AddContacts(Context context1) {
        context = context1;

        init();
        setOnClickListeners();
        fetchContacts();
    }

    private void init() {

        dialog = new Dialog(context, android.R.style.Theme_Holo_Light_NoActionBar);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dialog.setContentView(R.layout.sharing_dialog_add_contacts);

        ibBack = (ImageButton) dialog.findViewById(R.id.ibBack);
        lvAddContacts = (ListView) dialog.findViewById(R.id.lvAddContacts);
        searchView = (SearchView) dialog.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint(" + Add Contacts");



    }


    private void setOnClickListeners() {

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    lvAddContacts.clearTextFilter();

                } else {
                    //lvAddContacts.setFilterText(query.toString());
                    filter.filter(query);
                    //
                    // lvAddContacts.setAdapter(inviteContactsAdapter);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    lvAddContacts.clearTextFilter();

                } else {
                    //lvAddContacts.setFilterText(newText.toString());
                    filter.filter(newText);
                    inviteContactsAdapter.notifyDataSetChanged();
                    //  lvAddContacts.setAdapter(inviteContactsAdapter);

                }
                return true;
            }
        });


    }

    public void showAddContacts() {
        dialog.show();
    }

    public void fetchContacts() {

        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        final ArrayList<String> list = new ArrayList<String>();
        final ArrayList<String> listPhone = new ArrayList<String>();


        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));


                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        list.add(name + "\n" + phoneNumber);


                    }

                    phoneCursor.close();
                }

            }


            int count = list.size();
            inviteContactsAdapter = new InviteContactsAdapter(context, count, list);
            lvAddContacts.setAdapter(inviteContactsAdapter);
            filter = inviteContactsAdapter.getFilter();
            filter.filter("   ");
            lvAddContacts.setTextFilterEnabled(false);


        }
    }

}

class FBFriendsArrayAdapter extends BaseAdapter {

    Context context;
    int count;
    View row = null;
    ArrayList<String> FBFriendIDList;
    ArrayList<String> FBFriendNameList;
    AddContacts objAddContacts;
    ObjectRepo objRepo;

    public FBFriendsArrayAdapter(Context context1, ObjectRepo objRepo1, int countTemp, ArrayList<String> List_id, ArrayList<String> List_name, AddContacts objAddContactsTemp) {
        context = context1;
        objRepo = objRepo1;
        count = countTemp;
        FBFriendNameList = List_name;
        FBFriendIDList = List_id;
        objAddContacts = objAddContactsTemp;
    }

    @Override
    public int getCount() {

        return count;
    }

    @Override
    public Object getItem(int position) {
        return FBFriendNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = null;
        final int type = position;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.fb_friend_row, parent, false);
            TextView tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            final ImageButton ibSharingToggle = (ImageButton) row.findViewById(R.id.ibSharingToggle);
            ibSharingToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ibSharingToggle.getTag().toString().equals("inactive")) {
                        //objRepo.objBackendClass.grantSharing(FBFriendIDList.get(type),"FD0E6E122FBD");
                        ibSharingToggle.setImageResource(R.drawable.sharing_on);
                        ibSharingToggle.setTag("active");

                    } else if (ibSharingToggle.getTag().toString().equals("active")) {
                        //objRepo.objBackendClass.revokeSharing(FBFriendIDList.get(type),"FD0E6E122FBD");
                        ibSharingToggle.setImageResource(R.drawable.sharing_off);
                        ibSharingToggle.setTag("inactive");
                    }
                }
            });
            ProfilePictureView ivUserPic = (ProfilePictureView) row.findViewById(R.id.ivFBFriendPic);
            ivUserPic.setPresetSize(-2);
            if (FBFriendIDList.get(position) != null) {
                ivUserPic.setProfileId(FBFriendIDList.get(position));
                tvUserName.setText("" + FBFriendNameList.get(position));

            }

        } else {
            row = convertView;
        }


        return row;
    }
}

class InviteContactsAdapter extends BaseAdapter {

    Context context;
    int count;

    public static ArrayList<String> originalData;
    public static ArrayList<String> filteredData = null;
    Object mLock = new Object();
    List<String> mObjects;
    ItemFilter mFilter = new ItemFilter();


    public InviteContactsAdapter(Context contextTemp, int countTemp, ArrayList<String> FBFriendNameListTemp) {
        context = contextTemp;
        count = countTemp;
        originalData = FBFriendNameListTemp;
        filteredData = FBFriendNameListTemp;

    }

    @Override
    public int getCount() {

        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        int type = position;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.invite_friends, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder. tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            viewHolder. ivfriendImage = (ImageView)row.findViewById(R.id.imageview_invite_friends);
            viewHolder. ibTextingToggle = (ImageButton) row.findViewById(R.id.ibTextingToggle);
            row.setTag(viewHolder);
        }
      final  ViewHolder viewHolder = (ViewHolder) row.getTag();

        viewHolder. tvUserName.setText("" + filteredData.get(position));

        viewHolder. ibTextingToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.ibTextingToggle.getTag().toString().equals("inactive")) {
                    viewHolder.ibTextingToggle.setImageResource(R.drawable.sent_icon);
                    viewHolder.ibTextingToggle.setTag("active");

                } else if (viewHolder.ibTextingToggle.getTag().toString().equals("active")) {
                    viewHolder.ibTextingToggle.setImageResource(R.drawable.text_icon);
                    viewHolder. ibTextingToggle.setTag("inactive");
                }
            }
        });

        return row;
    }
    public class ViewHolder {
        ImageView ivfriendImage;
        TextView tvUserName;
        ImageButton ibTextingToggle;
    }
    public Filter getFilter() {
        return mFilter;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }
}
