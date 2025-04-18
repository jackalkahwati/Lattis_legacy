package com.lattis.ellipse.presentation.ui.history;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lattis.ellipse.Utils.CurrencyUtil;
import com.lattis.ellipse.Utils.UtilHelper;
import com.lattis.ellipse.data.network.model.response.history.RideHistoryDataResponse;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;

import static com.lattis.ellipse.Utils.UtilHelper.getTimeFromDuration;

public class TripsListAdapter extends RecyclerView.Adapter<TripsListAdapter.ViewHolder> {
    Activity mContext;
    List<RideHistoryDataResponse> rideHistoryDataResponses;


    public TripsListAdapter(RideHistoryListingActivity rideHistoryListingActivity, List<RideHistoryDataResponse> rideHistoryDataResponses) {
        this.rideHistoryDataResponses = rideHistoryDataResponses;
        this.mContext = rideHistoryListingActivity;
    }


    @Override
    public TripsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_trips_list, parent, false);
        return new TripsListAdapter.ViewHolder(view, rideHistoryDataResponses);
    }

    @Override
    public void onBindViewHolder(TripsListAdapter.ViewHolder holder, int position) {
        if (rideHistoryDataResponses.get(position).getDate_created() != null) {
            holder.tv_trip_time.setText(UtilHelper.getDateCurrentTimeZone(mContext,Long.parseLong
                    (rideHistoryDataResponses.get(position).getDate_created())));
        }
        if (rideHistoryDataResponses.get(position).getTotal() != null) {

            holder.tv_trip_cost_currency.setText(CurrencyUtil.getCurrencySymbolByCode(rideHistoryDataResponses.get(position).getCurrency()));
            holder.tv_trip_cost.setText(UtilHelper.getDotAfterNumber(rideHistoryDataResponses.get(position).getTotal()));

        }else
            holder.tv_trip_cost.setText(mContext.getString(R.string.label_cost_ride_free_default));


        holder.tv_trip_duration.setText(getTimeFromDuration(rideHistoryDataResponses.get(position).getDuration()));
        holder.tv_fleet_name.setText(rideHistoryDataResponses.get(position).getFleet_name());

    }

    @Override
    public int getItemCount() {
        return rideHistoryDataResponses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_trip_cost)
        TextView tv_trip_cost;
        @BindView(R.id.tv_trip_duration)
        TextView tv_trip_duration;
        @BindView(R.id.tv_fleet_name)
        TextView tv_fleet_name;
        @BindView(R.id.tv_trip_time)
        TextView tv_trip_time;
        @BindView(R.id.tv_trip_cost_currency)
        TextView tv_trip_cost_currency;

        List<RideHistoryDataResponse> rideHistoryDataResponses;


        public ViewHolder(View itemView, List<RideHistoryDataResponse> rideHistoryDataResponses) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.rideHistoryDataResponses = rideHistoryDataResponses;
            ButterKnife.bind(this, itemView);
        }


        @Override
        public void onClick(View v) {

            v.getContext().startActivity(new Intent(v.getContext()
                    , TripDetailsActivity.class)
                    .putExtra("TRIP_DETAILS",
                            new Gson().toJson(rideHistoryDataResponses.get(getAdapterPosition())))
            );
        }
    }


}
