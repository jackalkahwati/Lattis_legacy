package com.lattis.ellipse.presentation.ui.base.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import butterknife.BindView;
import io.lattis.ellipse.R;

public abstract class BaseDrawerActivity<Presenter extends ActivityPresenter>
        extends BaseAuthenticatedActivity<Presenter> {

    @Nullable
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @Nullable
    @BindView(R.id.navigation_recycler_view)
    RecyclerView recyclerView;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    private DrawerAdapter drawerAdapter;

    private List<DrawerMenu> drawerMenus;
    private Boolean isMenuShownForRide=false;


    @Override
    protected int getViewStubId() {
        return R.id.view_stub;
    }

    @Override
    protected int getViewStubLayoutId() {
        return getActivityContentLayoutId();
    }

    @Override
    protected void configureViews() {
        if(drawerLayout!=null && toolbar != null){
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_open, R.string.menu_close);
            drawerLayout.addDrawerListener(drawerToggle);
            setupAppbar(toolbar,false);
            drawerToggle.syncState();
        }

        if(isMenuShownForRide)
            drawerMenus = DrawerMenu.getMenusWithRide();
        else
            drawerMenus = DrawerMenu.getMenusWithoutRide();


        drawerAdapter = new DrawerAdapter(drawerMenus);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(drawerAdapter);
    }





    @Override
    protected void setToolbarBackArrowAction() { }

    protected void setCheckedItem(DrawerMenu menuItem) {
        int newPosition=-1;
        for(int position=0;position<drawerMenus.size();position++){
            if(drawerMenus.get(position).getItemId() == menuItem.getItemId()){
                newPosition=position;
                break;
            }
        }
        if(newPosition==-1)
            return;

        //int newPosition = DrawerMenu.valueOf(menuItem.toString()).ordinal();

        drawerAdapter.notifyItemChanged(drawerAdapter.getSelectedPosition());
        drawerAdapter.setSelectedPosition(newPosition);
        drawerAdapter.notifyItemChanged(newPosition);
    }

    public void setHomeForBackPressed(){
        setCheckedItem(drawerMenus.get(0));
    }

    protected void navigateTo(DrawerMenu menuItem) {
        setCheckedItem(menuItem);
        onDrawerItemClicked(menuItem);
    }

    @Override
    protected void configureSubscriptions() {
        super.configureSubscriptions();
        if (navigationView != null) {
            subscriptions.add(drawerAdapter
                    .getViewClickSubject()
                    .subscribe(view -> {
                        int newPosition = recyclerView.getChildAdapterPosition(view);

                        drawerAdapter.notifyItemChanged(drawerAdapter.getSelectedPosition());
                        drawerAdapter.setSelectedPosition(newPosition);
                        drawerAdapter.notifyItemChanged(newPosition);

                        DrawerMenu itemMenu = drawerMenus.get(newPosition);
                        onDrawerItemClicked(itemMenu);
                    }));
        }
    }

    protected void onDrawerItemClicked(DrawerMenu menuItem){
        onDrawerItemSelected(menuItem);
        closeDrawer();
    }

    protected void onDrawerItemSelected(DrawerMenu menuItem){}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(drawerToggle!=null){
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(drawerToggle!=null){
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isDrawerOpen()){
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isDrawerOpen(){
        return drawerLayout != null && navigationView!=null &&
                drawerLayout.isDrawerOpen(navigationView);
    }

    public void closeDrawer(){
        if(drawerLayout != null && navigationView!=null){
            drawerLayout.closeDrawer(navigationView);
        }
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_base_drawer;
    }

    protected abstract int getDefaultSelectedItemId();


    protected void setBaseDrawerMenuWithRide(){
        isMenuShownForRide=true;
        drawerMenus = DrawerMenu.getMenusWithRide();
        if(recyclerView!=null && drawerAdapter!=null){
            drawerAdapter.setDrawerMenuList(drawerMenus);
            drawerAdapter.notifyDataSetChanged();
        }

    }

    protected void setBaseDrawerMenuWithoutRide(){
        isMenuShownForRide=false;
        drawerMenus = DrawerMenu.getMenusWithoutRide();
        if(recyclerView!=null && drawerAdapter!=null){
            drawerAdapter.setDrawerMenuList(drawerMenus);
            drawerAdapter.notifyDataSetChanged();
        }
    }

    protected abstract int getActivityContentLayoutId();



    protected void onInternetConnectionChanged(boolean isConnected) {
        if(isConnected){
            drawerToggle.setDrawerIndicatorEnabled(true);
        }else{
            drawerToggle.setDrawerIndicatorEnabled(false);
        }
        drawerToggle.syncState();

    }
}
