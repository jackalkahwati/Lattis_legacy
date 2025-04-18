package com.lattis.lattis.presentation.base.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.lattis.domain.models.Reservation
import com.lattis.lattis.presentation.base.BaseView
import com.lattis.lattis.presentation.base.activity.DrawerAdapter.Companion.ITEM
import io.lattis.lattis.R
import kotlinx.android.synthetic.main.view_drawer_content.*
import kotlinx.android.synthetic.main.view_drawer_layout.*
import kotlinx.android.synthetic.main.view_toolbar.*

abstract class BaseDrawerActivity<Presenter : ActivityPresenter<V>,V:BaseView> : BaseAuthenticatedActivity<Presenter,V>() {

    lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerAdapter: DrawerAdapter
    private lateinit var drawerMenus: List<DrawerMenu>
    private var isMenuShownForRide = false
    override val viewStubIdFullScreen
        protected get() = R.id.view_stub_full_screen

    protected abstract val activityContentLayoutId: Int
    override val viewStubLayoutId: Int = activityContentLayoutId

    override fun configureViews() {
        if (drawer_layout != null && toolbar != null) {
            drawerToggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.menu_open,
                R.string.menu_close
            )
            drawer_layout?.addDrawerListener(drawerToggle)
            setupAppbar(toolbar, false)
            drawerToggle?.syncState()
        }
        drawerMenus =
            if (isMenuShownForRide) DrawerMenu.Companion.menusWithRide() else DrawerMenu.Companion.menusWithoutRide()
        drawerAdapter = DrawerAdapter(drawerMenus)
        navigation_recycler_view?.layoutManager = LinearLayoutManager(this)
        navigation_recycler_view?.adapter = drawerAdapter
    }

    override fun setToolbarBackArrowAction() {}


    protected fun setCheckedItem(menuItem: DrawerMenu) {
        var newPosition = -1
        for (position in drawerMenus?.indices) {
            if (drawerMenus[position].itemId == menuItem.itemId) {
                newPosition = position
                break
            }
        }
        if (newPosition == -1) return
        //int newPosition = DrawerMenu.valueOf(menuItem.toString()).ordinal();
        drawerAdapter?.notifyItemChanged(drawerAdapter.selectedPosition)
        drawerAdapter?.selectedPosition = newPosition
        drawerAdapter?.notifyItemChanged(newPosition)
    }

    fun setHomeForBackPressed() {
        setCheckedItem(drawerMenus[0])
    }

    protected fun navigateTo(menuItem: DrawerMenu) {
        setCheckedItem(menuItem)
        onDrawerItemClicked(menuItem)
    }

    override fun configureSubscriptions() {
        super.configureSubscriptions()
        if (navigation_view != null) {
            subscriptions.add(drawerAdapter
                .viewClickSubject
                .subscribe { view: View ->
                    val newPosition = navigation_recycler_view.getChildAdapterPosition(view)
                    drawerAdapter?.notifyItemChanged(drawerAdapter.selectedPosition)
                    drawerAdapter?.selectedPosition=newPosition
                    drawerAdapter?.notifyItemChanged(newPosition)
                    val itemMenu = drawerMenus[newPosition]
                    onDrawerItemClicked(itemMenu)
                }
            )

            val headerView = navigation_view.getHeaderView(0)
            headerView?.findViewById<ConstraintLayout>(R.id.cl_nav_header_home)?.setOnClickListener {
                onDrawerItemClicked(DrawerMenu(R.id.menu_profile_settings,R.string.profile_settings_label,null,ITEM))
            }

        }
    }

    open fun onDrawerItemClicked(menuItem: DrawerMenu) {
        onDrawerItemSelected(menuItem)
        closeDrawer()
    }

    open fun onDrawerItemSelected(menuItem: DrawerMenu) {}
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (drawerToggle != null) {
            drawerToggle?.syncState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (drawerToggle != null) {
            drawerToggle?.onConfigurationChanged(newConfig)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle?.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private val isDrawerOpen: Boolean
        private get() = drawer_layout != null && navigation_view != null &&
                drawer_layout.isDrawerOpen(navigation_view)

    fun closeDrawer() {
        if (drawer_layout != null && navigation_view != null) {
            drawer_layout.closeDrawer(navigation_view)
        }
    }

    fun openDrawer() {
        if (drawer_layout != null && navigation_view != null) {
            drawer_layout.openDrawer(navigation_view)
        }
    }

    override val activityLayoutId
        protected get() = R.layout.activity_base_drawer

//    protected abstract val defaultSelectedItemId: Int
    protected fun setBaseDrawerMenuWithRide() {
        isMenuShownForRide = true
        drawerMenus = DrawerMenu.Companion.menusWithRide()
        if (navigation_recycler_view != null && drawerAdapter != null) {
            drawerAdapter?.setDrawerMenuList(drawerMenus)
            drawerAdapter?.notifyDataSetChanged()
        }
    }

    protected fun setBaseDrawerMenuWithoutRide() {
        isMenuShownForRide = false
        drawerMenus = DrawerMenu.Companion.menusWithoutRide()
        if (navigation_recycler_view != null && drawerAdapter != null) {
            drawerAdapter?.setDrawerMenuList(drawerMenus)
            drawerAdapter?.notifyDataSetChanged()
        }
    }

    protected fun setReservationsNumber(reservationCount:Int){
        drawerAdapter?.setReservationsNumber(reservationCount)
        drawerAdapter?.notifyDataSetChanged()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            drawerToggle?.isDrawerIndicatorEnabled = true
        } else {
            drawerToggle?.isDrawerIndicatorEnabled = false
        }
        drawerToggle?.syncState()
    }
}