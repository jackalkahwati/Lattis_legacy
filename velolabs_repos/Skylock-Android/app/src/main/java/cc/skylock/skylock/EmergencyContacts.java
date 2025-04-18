package cc.skylock.skylock;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cc.skylock.skylock.adapter.CustomAdapter;
import cc.skylock.skylock.pojo.PojoforContacts;

/**
 * Created by AlexVijayRaj on 9/3/2015.
 */
public class EmergencyContacts {

    Context context;
    Dialog dialog;
    ImageButton ibBack, ibDismiss, ibDone, ibEC1, ibEC2, ibEC3;
    TextView tvContact1, tvContact2, tvContact3, tvEC;
    AlertDialog dialogEC;
    ListView lvAddContacts;
    SearchView searchView;
   // ArrayAdapter adapter;
    //ListviewCustomAdapter adapter;
    CustomAdapter adapter;
    Filter filter;
    ObjectRepo objRepo;
    int EC = 1;
    String temp1 = null, temp2 = null, temp3 = null;
    ArrayList<PojoforContacts> contacts = new ArrayList<>();

    public EmergencyContacts(Context context1, ObjectRepo objRepo1, TextView tvEC1){
        context = context1;
        objRepo = objRepo1;
        tvEC = tvEC1;
        init();
        setOnClickListeners();
        fetchContacts();
    }

    public void showAlertDialogEC(){

        dialogEC.show();
    }

    private void init() {
        dialog=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dialog.setContentView(R.layout.dialog_add_ec);

        ibBack = (ImageButton) dialog.findViewById(R.id.ibBack);
        lvAddContacts = (ListView) dialog.findViewById(R.id.lvAddContacts);
        searchView = (SearchView) dialog.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint(" + Add Contacts");

        //Emergency contacts alert dialog
        LayoutInflater li = LayoutInflater.from(context);
        View dialog_ec = li.inflate(R.layout.dialog_ec, null);
        ibDismiss = (ImageButton) dialog_ec.findViewById(R.id.ibDismiss);
        ibDone = (ImageButton) dialog_ec.findViewById(R.id.ibDone);
        ibEC1 = (ImageButton) dialog_ec.findViewById(R.id.ibEC1);
        ibEC2 = (ImageButton) dialog_ec.findViewById(R.id.ibEC2);
        ibEC3 = (ImageButton) dialog_ec.findViewById(R.id.ibEC3);
        tvContact1 = (TextView) dialog_ec.findViewById(R.id.tvContact1);
        tvContact2 = (TextView) dialog_ec.findViewById(R.id.tvContact2);
        tvContact3 = (TextView) dialog_ec.findViewById(R.id.tvContact3);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialog_ec);
        alertDialogBuilder.setCancelable(false);
        dialogEC = alertDialogBuilder.create();

    }

    private void setOnClickListeners() {

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //close keyboard
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        ibDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEC.dismiss();
            }
        });

        ibDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEC.dismiss();
            }
        });

        ibEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibEC1.getTag().toString().equals("inactive")) {
                    EC = 1;
                    dialog.show();
                    ibEC1.setTag("active");
                }else if(ibEC1.getTag().toString().equals("active")){
                    ibEC1.setTag("inactive");
                    ibEC1.setImageResource(R.drawable.btn_add_ec);
                    tvContact1.setText("Add new");
                    setTextEC(1, null);
                }
            }
        });
        ibEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibEC2.getTag().toString().equals("inactive")) {
                    EC = 2;
                    dialog.show();
                    ibEC2.setTag("active");
                }else if(ibEC2.getTag().toString().equals("active")){
                    ibEC2.setTag("inactive");
                    ibEC2.setImageResource(R.drawable.btn_add_ec);
                    tvContact2.setText("Add new");
                    setTextEC(2, null);
                }
            }
        });
        ibEC3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibEC3.getTag().toString().equals("inactive")) {
                    EC = 3;
                    dialog.show();
                    ibEC3.setTag("active");
                }else if(ibEC3.getTag().toString().equals("active")){
                    ibEC3.setTag("inactive");
                    ibEC3.setImageResource(R.drawable.btn_add_ec);
                    tvContact3.setText("Add new");
                    setTextEC(3, null);
                }
            }
        });

        lvAddContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(EC == 1 ) {
                    String item = adapter.contactlist.get(position).getName()+"\n"+adapter.contactlist.get(position).getNumber();
                    Log.i("item name",item);
                    ibEC1.setImageResource(R.drawable.btn_remove_ec);
                    tvContact1.setText(item);
                    dialog.dismiss();
                    String lines[] = item.split("\\r?\\n");
                    //objRepo.objBackendClass.putEmergencyContacts1(lines[1], lines[0]);
                    String arr[] = lines[0].split(" ", 2);
                    setTextEC(1, arr[0]);

                }else if (EC == 2 ){
                    String item = adapter.contactlist.get(position).getName()+"\n"+adapter.contactlist.get(position).getNumber();
                    ibEC2.setImageResource(R.drawable.btn_remove_ec);
                    tvContact2.setText(item);
                    dialog.dismiss();
                    String lines[] = item.split("\\r?\\n");
                    String arr[] = lines[0].split(" ", 2);
                    setTextEC(2, arr[0]);
                }else if (EC == 3){
                    String item = adapter.contactlist.get(position).getName()+"\n"+adapter.contactlist.get(position).getNumber();
                    ibEC3.setImageResource(R.drawable.btn_remove_ec);
                    tvContact3.setText(item);
                    dialog.dismiss();
                    String lines[] = item.split("\\r?\\n");
                    String arr[] = lines[0].split(" ", 2);
                    setTextEC(3, arr[0]);
                }

                //close keyboard
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               /* if (TextUtils.isEmpty(query)) {
                    lvAddContacts.clearTextFilter();
                } else {
                  //  lvAddContacts.setFilterText(query.toString());
                    filter.filter(query);
                }*/
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    lvAddContacts.clearTextFilter();
                } else {
                //  lvAddContacts.setFilterText(newText.toString());
                  adapter.getFilter().filter(newText);
                }

                return false;
            }
        });

    }

    private void fetchContacts() {
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

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

        final ArrayList<String> list = new ArrayList<String>();
        final ArrayList<String> listPhone = new ArrayList<String>();


        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));



                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex( _ID ));
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        contacts.add(new PojoforContacts(name,phoneNumber,id));
                     //   list.add(name+"\n"+phoneNumber);

                    }

                    phoneCursor.close();
                }

            }

       //   adapter = new ArrayAdapter(context,android.R.layout.simple_list_item_1, list);
            adapter = new CustomAdapter(context,contacts);
            lvAddContacts.setAdapter(adapter);
            filter = adapter.getFilter();
            filter.filter("   ");
            lvAddContacts.setAdapter(adapter);
            lvAddContacts.setTextFilterEnabled(false);


        }
    }

    private void setTextEC(int position, String temp){

        switch (position){
            case 1: temp1 = temp;
                break;
            case 2: temp2 = temp;
                break;
            case 3: temp3 = temp;
                break;
        }
        if(temp1 != null && temp2 != null && temp3 != null){
            tvEC.setText(temp1+", "+temp2+", "+temp3);
        }else if(temp1 != null && temp2 != null ){
            tvEC.setText(temp1+", "+temp2);
        }else if(temp2 != null && temp3 != null ){
            tvEC.setText(temp2+", "+temp3);
        }else if(temp1 != null && temp3 != null ){
            tvEC.setText(temp1+", "+temp3);
        }else if(temp1 != null ){
            tvEC.setText(temp1);
        }else if(temp2 != null ){
            tvEC.setText(temp2);
        }else if(temp3 != null ){
            tvEC.setText(temp3);
        }else{
            tvEC.setText("+ Add Contacts");
        }


    }
}
