package cc.skylock.skylock.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cc.skylock.skylock.Bean.Person;
import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.SharingActivity;
import cc.skylock.skylock.ui.fragment.Contacts;

/**
 * Adapter for Person items. Sorts them by last name into sections starting with the
 * first letter of the last name.
 */
public class AddressBookDemoAdapter extends SectioningAdapter {

    Locale locale = Locale.getDefault();
    ItemViewHolder ivh;
    Context mContext;
    static final boolean USE_DEBUG_APPEARANCE = false;
    HeaderViewHolder hvh;
    int lastSelectItemPosition = 0, lastSelectSectionIndex = 0;
    Contacts contactsData;
    private class Section {
        String alpha;
        ArrayList<Person> people = new ArrayList<>();

    }

    public class ItemViewHolder extends SectioningAdapter.ItemViewHolder {
        TextView personNameTextView, personNumberTextView;
        RelativeLayout contactItemOutterLayout;

        ImageView selectedIndicater;

        public ItemViewHolder(View itemView) {
            super(itemView);
            contactItemOutterLayout = (RelativeLayout) itemView.findViewById(R.id.rl_share_connection_list);
            personNameTextView = (TextView) itemView.findViewById(R.id.tv_share_lockName);
            personNumberTextView = (TextView) itemView.findViewById(R.id.tv_share_lockNumber);
            selectedIndicater = (ImageView) itemView.findViewById(R.id.iv_share_lock_icon);
        }
    }

    public class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        }
    }


    List<Person> peopleList;
    ArrayList<Section> sections = new ArrayList<>();

    public AddressBookDemoAdapter(Context context, String searchName, String searchNumber,Contacts contactsData) {
        mContext = context;
        this.contactsData = contactsData;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + searchName + "%' or " + ContactsContract.CommonDataKinds.Phone.NUMBER + " like'%" + searchNumber + "%'";
//        selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + searchName +"%'";
        ArrayList<String> contactListNameDuplicateCheck = new ArrayList<>();
        peopleList = new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (!contactListNameDuplicateCheck.contains(name)) {
                Person person = new Person();
                person.contactId = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                person.picture.thumbnail = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                person.name = name;
                person.phone = phoneNumber;
                contactListNameDuplicateCheck.add(name);
                peopleList.add(person);
            }
        }
        if (peopleList != null) {
            setPeople(peopleList);
        }
        phones.close();
    }


    public void searchContact(String searchName, String searchNumber) {
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + searchName + "%' or " + ContactsContract.CommonDataKinds.Phone.NUMBER + " like'%" + searchNumber + "%'";


        ArrayList<String> contactListNameDuplicateCheck = new ArrayList<>();
        peopleList = new ArrayList<>();
        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER                        ));
            if (!contactListNameDuplicateCheck.contains(name)) {
                Person person = new Person();
                person.contactId = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                person.picture.thumbnail = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                person.name = name;
                person.phone = phoneNumber;
                contactListNameDuplicateCheck.add(name);
                peopleList.add(person);
            }
        }
        if (peopleList != null && peopleList.size() > 0) {
            setPeople(peopleList);
        } else {
            notifyAllSectionsDataSetChanged();
        }
        phones.close();
    }

    public List<Person> getPeople() {
        return peopleList;
    }

    public void setPeople(List<Person> people) {
//		this.people = people;
        sections.clear();
        // sort people into buckets by the first letter of last name
        char alpha = 0;
        Section currentSection = null;
        for (Person person : people) {
            if (person.name.charAt(0) != alpha) {
                if (currentSection != null) {
                    sections.add(currentSection);
                }
                currentSection = new Section();
                alpha = person.name.charAt(0);
                currentSection.alpha = String.valueOf(alpha);
            }
            if (currentSection != null) {
                currentSection.people.add(person);
            }
        }

        if (currentSection != null) {
            sections.add(currentSection);
        }

        notifyAllSectionsDataSetChanged();
    }

    @Override
    public int getNumberOfSections() {
        return sections.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return sections.get(sectionIndex).people.size();
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return true;
    }

    @Override
    public boolean doesSectionHaveFooter(int sectionIndex) {
        return false;
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.share_lock_list_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_item_addressbook_header, parent, false);

        return new HeaderViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder viewHolder, final int sectionIndex, final int itemIndex, int itemType) {


        Section s = sections.get(sectionIndex);
        ivh = (ItemViewHolder) viewHolder;
        final Person person = s.people.get(itemIndex);
        ivh.personNameTextView.setText(capitalize(person.name));
        ivh.personNumberTextView.setText(person.phone);
        if (!person.isSelected) {
            ivh.selectedIndicater.setImageResource(R.drawable.not_selected_oval);
        } else if (person.isSelected) {
            ivh.selectedIndicater.setImageResource(R.drawable.selected_oval);
        }

        final String typeOfContact = Contacts.newInstance().getTypeContact();
        if (typeOfContact.equalsIgnoreCase("SharingLock")) {
            ivh.selectedIndicater.setVisibility(View.GONE);
            if (!person.isSelected) {
                ivh.contactItemOutterLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            } else if (person.isSelected) {
                ivh.contactItemOutterLayout.setBackgroundColor(mContext.getResources().getColor(R.color.text_color_accent));
            }
        }


        ivh.contactItemOutterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Contacts.newInstance().getTypeContact().equalsIgnoreCase("SharingLock")) {
                        sections.get(lastSelectSectionIndex).people.get(lastSelectItemPosition).isSelected = false;
                    }
                    lastSelectItemPosition = itemIndex;
                    lastSelectSectionIndex = sectionIndex;
                    if (!person.isSelected) {
                        if (typeOfContact.equalsIgnoreCase("EmergencyContacts")) {

                            ivh.contactItemOutterLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
                            ivh.selectedIndicater.setVisibility(View.VISIBLE);
                            if (Contacts.selectedContactNumber.size() < 3) {
                                boolean isNumberadded = false;
                                HashMap<String, String> ecContact = new HashMap<String, String>();
                                ecContact.put("number", person.phone);
                                ecContact.put("name", person.name);
                                ecContact.put("id", person.contactId);
                                ecContact.put("photoUrl", person.picture.thumbnail);
                                for (HashMap<String, String> contactNumber : Contacts.selectedContactNumber) {
                                    if (contactNumber.get("number").equalsIgnoreCase(person.phone)) {
                                        isNumberadded = true;
                                    }
                                }
                                if (!isNumberadded) {
                                    Contacts.selectedContactNumber.add(ecContact);
                                }
                                person.isSelected = true;
                            }
                        } else if (typeOfContact.equalsIgnoreCase("SharingLock")) {
                            mContext.startActivity(new Intent(mContext, SharingActivity.class).putExtra("SHARE_USER_NAME", person.name).putExtra("SHARE_USER_PHONE", person.phone));
                            v.setBackgroundColor(mContext.getResources().getColor(R.color.text_color_accent));
                            Contacts.shareLockNumber = person.phone;
                            Contacts.sharePersonName = person.name;
                            person.isSelected = true;
                        }
                    } else if (person.isSelected) {
                        if (typeOfContact.equalsIgnoreCase("SharingLock")) {
                            Contacts.shareLockNumber = "";
                        } else if (typeOfContact.equalsIgnoreCase("EmergencyContacts")) {
                            for (int noOfSelectedContactNumber = 0; noOfSelectedContactNumber < Contacts.selectedContactNumber.size(); noOfSelectedContactNumber++) {
                                if (Contacts.selectedContactNumber.get(noOfSelectedContactNumber).get("number").equalsIgnoreCase(person.phone)) {
                                    Contacts.selectedContactNumber.remove(noOfSelectedContactNumber);
                                }
                            }
                        }
                        person.isSelected = false;
                    }
                    notifyAllSectionsDataSetChanged();


                    if (Contacts.selectedContactNumber.size() ==0) {
                        contactsData.emergencyContactCount.setVisibility(View.INVISIBLE);
                    } else {
                        contactsData.emergencyContactCount.setVisibility(View.VISIBLE);
                    }
                    if (Contacts.selectedContactNumber.size()  == 2) {
                        contactsData.emergencyContactCount.setText(R.string.add_emergency_with_two_contacts);
                    } else if (Contacts.selectedContactNumber.size()  == 1) {
                        contactsData.emergencyContactCount.setText(R.string.add_emergency_with_one_contacts);
                    } else if (Contacts.selectedContactNumber.size()  == 3) {
                        contactsData.emergencyContactCount.setText(R.string.add_emergency_with_zero_contacts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerType) {
        Section s = sections.get(sectionIndex);
        hvh = (HeaderViewHolder) viewHolder;

        if (USE_DEBUG_APPEARANCE) {
            hvh.itemView.setBackgroundColor(0x55ffffff);
            hvh.titleTextView.setText(pad(sectionIndex * 2) + s.alpha);
        } else {
            hvh.titleTextView.setText(s.alpha);
        }

    }

    private String capitalize(String s) {
        if (s != null && s.length() > 0) {
            return s.substring(0, 1).toUpperCase(locale) + s.substring(1);
        }

        return "";
    }

    private String pad(int spaces) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            b.append(' ');
        }
        return b.toString();
    }

    public class CustomComparator {
        CustomComparator() {
            super();
        }

        public int compare(Person object1, Person object2) {
            return object1.name.compareTo(object2.name);
        }
    }

}
