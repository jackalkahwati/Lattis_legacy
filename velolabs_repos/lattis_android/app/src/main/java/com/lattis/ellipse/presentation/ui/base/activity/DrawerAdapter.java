package com.lattis.ellipse.presentation.ui.base.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lattis.ellipse.R;
import io.reactivex.subjects.PublishSubject;


public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private List<DrawerMenu> drawerMenus;

    DrawerAdapter(List<DrawerMenu> drawerMenus){
        this.drawerMenus=drawerMenus;
    }

    private PublishSubject<View> viewClickSubject = PublishSubject.create();

    private int selectedPosition = 0;


    public void setDrawerMenuList(List<DrawerMenu> drawerMenus){
        this.drawerMenus=drawerMenus;
    }

    public PublishSubject<View> getViewClickSubject() {
        return viewClickSubject;
    }

    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewLayoutId = R.layout.layout_drawer_menu_item_1;

        View view = LayoutInflater.from(parent.getContext()).inflate(viewLayoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        if (viewType != 2) {
            RxView.clicks(view)
                    .takeUntil(RxView.detaches(parent))
                    .map(aVoid -> view)
                    .subscribe(viewClickSubject);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        DrawerMenu entry = drawerMenus.get(position);

        int titleId = entry.getTitleId();

        if (titleId > 0) {
            holder.titleView.setText(titleId);
        }

        int imageId = entry.getImageId();

        if(imageId>0){
            holder.imageView.setImageDrawable(context.getResources().getDrawable(imageId));
        }

    }

    @Override
    public int getItemCount() {
        return drawerMenus.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.title)
        TextView titleView;

        @Nullable
        @BindView(R.id.image)
        ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
