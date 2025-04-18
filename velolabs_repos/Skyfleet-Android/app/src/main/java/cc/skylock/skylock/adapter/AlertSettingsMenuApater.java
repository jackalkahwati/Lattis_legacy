package cc.skylock.skylock.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import cc.skylock.skylock.R;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 01-08-2016.
 */
public class AlertSettingsMenuApater extends RecyclerView.Adapter<AlertSettingsMenuApater.AlertSettingsMenuViewHolder> implements View.OnClickListener {
    Context context;
    String[] mTitle;
    String[] mAlert_description;

    public AlertSettingsMenuApater(Context mContext, String[] title, String[] alert_description) {
        this.context = mContext;
        this.mTitle = title;
        this.mAlert_description = alert_description;
    }


    @Override
    public AlertSettingsMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlertSettingsMenuViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rows_alert_notifications, parent, false));
    }

    @Override
    public void onBindViewHolder(AlertSettingsMenuViewHolder holder, int position) {

        holder.textView_alertName.setTypeface(UtilHelper.getTypface(context));
        holder.textView_Alertdescription.setTypeface(UtilHelper.getTypface(context));
        holder.textView_alertName.setText(mTitle[position]);
        holder.textView_Alertdescription.setText(mAlert_description[position]);
        if (position == 0) {
            holder.toggleButton_Alert.setVisibility(View.GONE);
            holder.textView_Alertdescription.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mTitle.length;
    }

    @Override
    public void onClick(View v) {

    }

    public static class AlertSettingsMenuViewHolder extends RecyclerView.ViewHolder {

        TextView textView_alertName, textView_Alertdescription;
        ToggleButton toggleButton_Alert;

        public AlertSettingsMenuViewHolder(View itemView) {
            super(itemView);
            textView_alertName = (TextView) itemView.findViewById(R.id.tv_label_alert_name);
            toggleButton_Alert = (ToggleButton) itemView.findViewById(R.id.toggleButton_alert);
            textView_Alertdescription = (TextView) itemView.findViewById(R.id.tv_label_alert_description);
        }


    }
}
