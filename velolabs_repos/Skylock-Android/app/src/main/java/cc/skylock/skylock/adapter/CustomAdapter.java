package cc.skylock.skylock.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.pojo.PojoforContacts;

/**
 * Created by Velo Labs Android on 13-01-2016.
 */
public class CustomAdapter extends BaseAdapter implements Filterable {

    Context context;
   public static ArrayList<PojoforContacts> contactlist;
    ArrayList<PojoforContacts> mStringFilterList;
    ValueFilter valueFilter;
    LayoutInflater inflater;

    public CustomAdapter(Context context, ArrayList<PojoforContacts> pojoforContacts) {
        this.context = context;
        this.contactlist = pojoforContacts;
        this.mStringFilterList = pojoforContacts;
    }

    @Override
    public int getCount() {
        return contactlist.size();
    }

    @Override
    public Object getItem(int position) {
        return contactlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return contactlist.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        if (rowView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.emergencycontacts_row, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tv_Name = (TextView) rowView.findViewById(R.id.textview_name);
            viewHolder.tv_Number = (TextView) rowView.findViewById(R.id.texview_number);
            viewHolder.iv_Contacts = (ImageView) rowView
                    .findViewById(R.id.imageview_contacts);
            viewHolder.view_line = (View)rowView.findViewById(R.id.line_view);
            rowView.setTag(viewHolder);
        }
        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.tv_Name.setText(contactlist.get(position).getName());
        holder.tv_Number.setText(contactlist.get(position).getNumber());

        return rowView;
    }

    public class ViewHolder {
        ImageView iv_Contacts;
        TextView tv_Name;
        TextView tv_Number;
        View view_line;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<PojoforContacts> filterList = new ArrayList<PojoforContacts>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getName().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        PojoforContacts country = new PojoforContacts(mStringFilterList.get(i)
                                .getName(), mStringFilterList.get(i)
                                .getNumber(), mStringFilterList.get(i)
                                .getId());

                        filterList.add(country);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            contactlist = (ArrayList<PojoforContacts>) results.values;
            notifyDataSetChanged();
        }

    }

}
