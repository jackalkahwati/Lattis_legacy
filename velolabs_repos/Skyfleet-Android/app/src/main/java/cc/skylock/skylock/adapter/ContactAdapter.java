package cc.skylock.skylock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cc.skylock.skylock.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by admin on 19/08/16.
 */
public class ContactAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private String[] countries;
    private LayoutInflater inflater;

    public ContactAdapter(Context context) {
        inflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return countries.length;
    }

    @Override
    public Object getItem(int position) {
        return countries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.share_lock_list_item, parent, false);
            holder.personNameTextView = (TextView) convertView.findViewById(R.id.tv_share_lockName);
            holder.personNumberTextView = (TextView) convertView.findViewById(R.id.tv_share_lockNumber);
            holder.selectedIndicater = (ImageView) convertView.findViewById(R.id.iv_share_lock_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.personNameTextView.setText(countries[position]);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.list_item_addressbook_header, parent, false);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
        holder.titleTextView.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return countries[position].subSequence(0, 1).charAt(0);
    }

    class HeaderViewHolder {
        TextView titleTextView;
    }

    class ViewHolder {

        TextView personNameTextView;
        TextView personNumberTextView;
        ImageView selectedIndicater;
    }

}
