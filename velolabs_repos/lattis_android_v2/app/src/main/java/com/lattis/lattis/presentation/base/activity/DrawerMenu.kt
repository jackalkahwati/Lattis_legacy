package com.lattis.lattis.presentation.base.activity

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.lattis.domain.models.Reservation
import com.lattis.lattis.presentation.base.activity.DrawerAdapter.Companion.DIVIDER
import com.lattis.lattis.presentation.base.activity.DrawerAdapter.Companion.HEADER
import com.lattis.lattis.presentation.base.activity.DrawerAdapter.Companion.ITEM
import com.lattis.lattis.presentation.base.activity.DrawerAdapter.Companion.RESERVATION
import io.lattis.lattis.R
import java.util.*

class DrawerMenu(@param:IdRes val itemId: Int?, @param:StringRes val titleId: Int, @param:DrawableRes val imageId: Int?, val type:Int) {
    var isChecked = false

    companion object {
        fun menusWithRide(): List<DrawerMenu>{
            val drawerMenus: MutableList<DrawerMenu> =
                ArrayList()

            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.menu_account,
                    null,
                    HEADER
                )
            )

//                drawerMenus.add(
//                    DrawerMenu(
//                        R.id.menu_profile_settings,
//                        R.string.profile_settings_label,
//                        R.drawable.menu_profile_settings,
//                        ITEM
//                    )
//                )


            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_payment,
                    R.string.payment,
                    R.drawable.menu_payment,
                    ITEM
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_ride_history,
                    R.string.ride_history,
                    R.drawable.menu_ride_history,
                    ITEM
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_private_fleets,
                    R.string.private_fleet,
                    R.drawable.menu_private_fleet,
                    ITEM
                )
            )


            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_reservation,
                    R.string.reservation,
                    R.drawable.reservation,
                    ITEM
                )
            )

            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_membership,
                    R.string.membership,
                    R.drawable.menu_membership,
                    ITEM
                )
            )


            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.drawer_divider,
                    null,
                    DIVIDER
                )
            )


            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.menu_support,
                    null,
                    HEADER
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_report_issue,
                    R.string.report_damage,
                    R.drawable.menu_report_issue,
                    ITEM
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_help,
                    R.string.help,
                    R.drawable.menu_help,
                    ITEM
                )
            )

            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.drawer_divider,
                    null,
                    DIVIDER
                )
            )

            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_logout,
                    R.string.logout,
                    R.drawable.menu_logout,
                    ITEM
                )
            )
            return drawerMenus
        }

        fun menusWithoutRide(): List<DrawerMenu> {
            val drawerMenus: MutableList<DrawerMenu> =
                ArrayList()

            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.menu_account,
                    null,
                    HEADER
                )
            )

//                drawerMenus.add(
//                    DrawerMenu(
//                        R.id.menu_profile_settings,
//                        R.string.profile_settings_label,
//                        R.drawable.menu_profile_settings,
//                        ITEM
//                    )
//                )

            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_payment,
                    R.string.payment,
                    R.drawable.menu_payment,
                    ITEM
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_ride_history,
                    R.string.ride_history,
                    R.drawable.menu_ride_history,
                    ITEM
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_private_fleets,
                    R.string.private_fleet,
                    R.drawable.menu_private_fleet,
                    ITEM
                )
            )
                drawerMenus.add(
                    DrawerMenu(
                        R.id.menu_membership,
                        R.string.memberships,
                        R.drawable.menu_membership,
                        ITEM
                    )
                )


            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_reservation,
                    R.string.reservation,
                    R.drawable.reservation,
                    ITEM
                )
            )


            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.drawer_divider,
                    null,
                    DIVIDER
                )
            )

            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.menu_support,
                    null,
                    HEADER
                )
            )
            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_help,
                    R.string.help,
                    R.drawable.menu_help,
                    ITEM
                )
            )


            drawerMenus.add(
                DrawerMenu(
                    null,
                    R.string.drawer_divider,
                    null,
                    DIVIDER
                )
            )

            drawerMenus.add(
                DrawerMenu(
                    R.id.menu_logout,
                    R.string.logout,
                    R.drawable.menu_logout,
                    ITEM
                )
            )
            return drawerMenus
        }
    }

}