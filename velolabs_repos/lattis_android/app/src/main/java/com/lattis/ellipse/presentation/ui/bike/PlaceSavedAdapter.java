package com.lattis.ellipse.presentation.ui.bike;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.lattis.ellipse.domain.model.SavedAddress;

import java.util.List;

import io.lattis.ellipse.R;

public class PlaceSavedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface SavedPlaceListener {
        public void onSavedPlaceClick(SavedAddress savedAddress);
    }

    Context mContext;
    List<SavedAddress> mResponse;
    SavedPlaceListener mListener;

    public PlaceSavedAdapter(Context ctx, List<SavedAddress> response) {
        this.mContext = ctx;
        this.mResponse = response;
        this.mListener = (SavedPlaceListener) mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = null;
        RecyclerView.ViewHolder vh = null;
        rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_savedaddress, parent, false);
        vh = new HistoryViewHolder(rowView);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        HistoryViewHolder vh = (HistoryViewHolder) holder;
        vh.mAddress.setText(mResponse.get(position).getName());
        vh.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSavedPlaceClick(mResponse.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResponse.size();
    }

    /*
    View Holder For Trip History
     */
    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mParentLayout;
        public TextView mAddress;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            mParentLayout = (LinearLayout) itemView.findViewById(R.id.parent);
            mAddress = (TextView) itemView.findViewById(R.id.address);
        }
    }
}
