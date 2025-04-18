package cc.skylock.skylock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cc.skylock.skylock.R;
import cc.skylock.skylock.pojo.PojoforContacts;

/**
 * Created by Velo Labs Android on 12-01-2016.
 */
public class ListviewCustomAdapter extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<PojoforContacts> list = null;
    LayoutInflater inflater = null;
    private ArrayFilter mFilter;
    private ArrayList<PojoforContacts> _Contacts;
    private ArrayList<PojoforContacts> mStringFilterList;
    public ListviewCustomAdapter(Context context,
                                 ArrayList<PojoforContacts> list) {
        this.context = context;
        this._Contacts = list;
        this.mStringFilterList = list;
        getFilter();
    }

    @Override
    public int getCount() {
        return mStringFilterList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
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
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
         holder.tv_Name.setText(mStringFilterList.get(position).getName());
        holder.tv_Number.setText(mStringFilterList.get(position).getNumber());

        return rowView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public class ViewHolder{
        ImageView iv_Contacts;
        TextView tv_Name;
        TextView tv_Number;
    }
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
    private class ArrayFilter extends Filter {

        //Invoked in a worker thread to filter the data according to the constraint.
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results=new FilterResults();
            if(constraint!=null && constraint.length()>0){
                ArrayList<PojoforContacts> filterList=new ArrayList<PojoforContacts>();
                for(int i=0;i<mStringFilterList.size();i++){
                    if((mStringFilterList.get(i).getName().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {
                        PojoforContacts contacts = new PojoforContacts();
                        contacts.setName(mStringFilterList.get(i).getName());
                        contacts.setId(mStringFilterList.get(i).getId());
                        filterList.add(contacts);
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
            }else{
                results.count=mStringFilterList.size();
                results.values=mStringFilterList;
            }
            return results;
        }


        //Invoked in the UI thread to publish the filtering results in the user interface.
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            _Contacts=(ArrayList<PojoforContacts>) results.values;
            notifyDataSetChanged();
        }
    }


}
