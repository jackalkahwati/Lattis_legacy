package com.lattis.ellipse.presentation.ui.bike;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.lattis.ellipse.domain.model.map.PlaceAutocomplete;
import com.lattis.ellipse.presentation.view.CustomTextView;

import java.util.ArrayList;

import io.lattis.ellipse.R;


public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder> {

    public interface PlaceAutoCompleteInterface {
        void onPlaceClick(ArrayList<PlaceAutocomplete> mResultList, int position);
    }

    Context mContext;
    PlaceAutoCompleteInterface mListener;
    private static final String TAG = "PlaceAutocompleteAdapter";
    ArrayList<PlaceAutocomplete> mResultList;
    private int layout;


    public PlaceAutocompleteAdapter(Context context, int resource) {
        this.mContext = context;
        layout = resource;
        this.mListener = (PlaceAutoCompleteInterface) mContext;
    }

    /*
    Clear List items
     */
    public void clearList() {
        if (mResultList != null && mResultList.size() > 0) {
            mResultList.clear();
        }
    }



    public void setSearchResult(ArrayList<PlaceAutocomplete> results){
        this.mResultList = results;
        notifyDataSetChanged();
    }


    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(layout, viewGroup, false);
        PlaceViewHolder mPredictionHolder = new PlaceViewHolder(convertView);
        return mPredictionHolder;
    }


    @Override
    public void onBindViewHolder(PlaceViewHolder mPredictionHolder, final int i) {
        mPredictionHolder.mAddress1.setText(mResultList.get(i).getAddress1());
        mPredictionHolder.mAddress2.setText(mResultList.get(i).getAddress2());


        mPredictionHolder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaceClick(mResultList, i);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mResultList != null)
            return mResultList.size();
        else
            return 0;
    }

    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        public CustomTextView mAddress1, mAddress2;
        public LinearLayout mParentLayout;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            mAddress1 = (CustomTextView) itemView.findViewById(R.id.address1);
            mAddress2 = (CustomTextView) itemView.findViewById(R.id.address2);
            mParentLayout = (LinearLayout) itemView.findViewById(R.id.ll_search_row);
        }

    }
}
