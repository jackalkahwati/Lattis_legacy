package com.lattis.ellipse.presentation.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lattis.ellipse.domain.model.PrivateNetwork;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;


/**
 * Created by ssd3 on 5/9/17.
 */

public class PrivateNetworkAdapter extends RecyclerView.Adapter<PrivateNetworkAdapter.ViewHolder> {


    private Context context;
    private List<PrivateNetwork> privateNetworks;


    public PrivateNetworkAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (privateNetworks == null) {
            return 0;
        }
        return privateNetworks.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_network_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PrivateNetwork privateNetwork = privateNetworks.get(position);

        if (privateNetwork.getFleet_name() != null)
            holder.tv_private_network_name.setText(privateNetwork.getFleet_name());

        if (privateNetwork.getEmail() != null)
            holder.tv_private_network_email.setText(privateNetwork.getEmail());

        if (privateNetwork.getLogo() != null)
            downloadImage(holder, privateNetwork.getLogo());

    }

    void downloadImage(final ViewHolder holder, String url) {

        Glide
                .with(context)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>(100,100) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        holder.iv_private_network_icon.setImageBitmap(resource);
                    }
                });
    }


    public void setPrivateNetworks(List<PrivateNetwork> privateNetworks) {
        this.privateNetworks = privateNetworks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_private_network_name)
        TextView tv_private_network_name;
        @BindView(R.id.tv_private_network_email)
        TextView tv_private_network_email;
        @BindView(R.id.iv_private_network_icon)
        ImageView iv_private_network_icon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
