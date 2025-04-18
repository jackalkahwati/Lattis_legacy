package com.lattis.lattis.presentation.base.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.view.detaches
import com.lattis.domain.models.Reservation
import io.lattis.lattis.R
import io.reactivex.rxjava3.subjects.PublishSubject

class DrawerAdapter internal constructor(
    private var drawerMenus: List<DrawerMenu>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    val viewClickSubject = PublishSubject.create<View>()
    var selectedPosition = 0
    var reservationCount = 0

    companion object{
        val HEADER =0
        val ITEM =1
        val DIVIDER =2
        val RESERVATION =3
    }


    fun setReservationsNumber(reservationCount:Int){
        this.reservationCount = reservationCount
    }

    fun setDrawerMenuList(drawerMenus: List<DrawerMenu>) {
        this.drawerMenus = drawerMenus
    }

    override fun getItemViewType(position: Int): Int {
        val entry = drawerMenus[position]
        return entry.type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        if(viewType == ITEM) {
            val viewLayoutId: Int = R.layout.layout_drawer_menu_item_1
            val view =
                LayoutInflater.from(parent.context).inflate(viewLayoutId, parent, false)
            val viewHolder =
                ViewHolderForItem(view)
            if (viewType != 2) {
                view.clicks()
                    .takeUntil(parent.detaches())
                    .map { aVoid: Any? -> view }
                    .subscribe(viewClickSubject)
            }
            return viewHolder
        }else if(viewType == HEADER){
            val viewLayoutId: Int = R.layout.layout_drawer_menu_section_1
            val view =
                LayoutInflater.from(parent.context).inflate(viewLayoutId, parent, false)
            val viewHolder =
                ViewHolderForSection(view)

            return viewHolder
        }else if(viewType == DIVIDER){
            val viewLayoutId: Int = R.layout.layout_drawer_menu_divider_1
            val view =
                LayoutInflater.from(parent.context).inflate(viewLayoutId, parent, false)
            val viewHolder =
                ViewHolderForDivider(view)

            return viewHolder
        }else if (viewType == RESERVATION){
            val viewLayoutId: Int = R.layout.layout_drawer_menu_reservation
            val view =
                LayoutInflater.from(parent.context).inflate(viewLayoutId, parent, false)
            val viewHolder =
                ViewHolderForReservation(view)
            if (viewType != 2) {
                view.clicks()
                    .takeUntil(parent.detaches())
                    .map { aVoid: Any? -> view }
                    .subscribe(viewClickSubject)
            }
            return viewHolder
        }else { // its required to put in else so writing other wise not required
            val viewLayoutId: Int = R.layout.layout_drawer_menu_item_1
            val view =
                LayoutInflater.from(parent.context).inflate(viewLayoutId, parent, false)
            val viewHolder =
                ViewHolderForItem(view)
            if (viewType != 2) {
                view.clicks()
                    .takeUntil(parent.detaches())
                    .map { aVoid: Any? -> view }
                    .subscribe(viewClickSubject)
            }
            return viewHolder
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {


        val context = holder.itemView.context
        val entry = drawerMenus[position]

        if(entry.type == ITEM) {

            val titleId = entry.titleId
            if (titleId > 0) {
                (holder as ViewHolderForItem).titleView.setText(titleId)
            }

            var itemId = entry.itemId
            if(itemId!=null && itemId == R.id.menu_reservation && reservationCount>0){
                (holder as ViewHolderForItem).reservationCountView.visibility = View.VISIBLE
                (holder as ViewHolderForItem).reservationCountView.setText(""+reservationCount)
            }else{
                (holder as ViewHolderForItem).reservationCountView.visibility = View.GONE
            }
            val imageId = entry.imageId!!
            if (imageId > 0) {
                (holder as ViewHolderForItem).imageView.setImageDrawable(context.resources.getDrawable(imageId))
            }
        }else if(entry.type == HEADER){
            val titleId = entry.titleId
            if (titleId > 0) {
                (holder as ViewHolderForSection).titleView.setText(titleId)
            }
        }else if(entry.type == DIVIDER){

        }else if(entry.type == RESERVATION
//            && reservationInCurrentStatus!=null &&
//            reservationInCurrentStatus?.reservation_start!=null){
//            val reservationHeading = context.getString(R.string.reservation)+" - "+getCurrentDateFromString(reservationInCurrentStatus?.reservation_start!!)
//            (holder as ViewHolderForReservation).reservationText.text = reservationHeading

            ){
                (holder as ViewHolderForReservation).reservationText.text = context.getString(R.string.reservations)
        }

    }


    override fun getItemCount(): Int {
        return drawerMenus.size
    }

    inner class ViewHolderForItem(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var titleView: TextView = itemView.findViewById(R.id.title)
        var imageView: ImageView = itemView.findViewById(R.id.image)
        var reservationCountView: TextView = itemView.findViewById(R.id.cv_reservation_count)
    }


    inner class ViewHolderForSection(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var titleView: TextView = itemView.findViewById(R.id.title)
    }

    inner class ViewHolderForDivider(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var divider: View = itemView.findViewById(R.id.drawer_divider)
    }

    inner class ViewHolderForReservation(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var reservationText: TextView = itemView.findViewById(R.id.ct_drawer_reservation)
    }



}