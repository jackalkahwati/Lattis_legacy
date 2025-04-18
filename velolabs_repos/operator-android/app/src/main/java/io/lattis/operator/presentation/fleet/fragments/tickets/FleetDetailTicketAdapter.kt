package io.lattis.operator.presentation.fleet.fragments.tickets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.lattis.domain.models.Ticket
import io.lattis.operator.R
import io.lattis.operator.presentation.customview.CustomTextView
import io.lattis.operator.presentation.utils.DateTimeUtil
import io.lattis.operator.utils.ResourceUtils.convertCategory

class FleetDetailTicketAdapter(
    var mContext: Context,
    val fleetDetailTicketListener: FleetDetailTicketListener,
    var tickets: List<Ticket>
) : RecyclerView.Adapter<FleetDetailTicketAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fleet_details_ticket, parent, false)
        return ViewHolder(
            view

        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.ct_ticket_name_in_fleet_details_ticket.text = tickets.get(position).vehicle?.name
        holder.ct_ticket_category_in_fleet_details_ticket.text = convertCategory(mContext,tickets.get(position).category)
        holder.ct_ticket_date_in_fleet_details_ticket.text = if(tickets.get(position).createdAt!=null) DateTimeUtil.getDateFromUnixTimeStamp(
            tickets.get(position).createdAt,
            DateTimeUtil.ticketCreatedDateFormat
        ) else ""

        downloadImage(
            holder,
            tickets.get(position).vehicle?.group?.image
        )

        holder.itemView.setOnClickListener {
            fleetDetailTicketListener.onTicketClicked(position)
        }
    }

    fun downloadImage(
        holder: ViewHolder,
        url: String?
    ) {

        val requestOptions = RequestOptions()
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.dontAnimate()

        Glide.with(mContext)
            .load(url)
            .apply(requestOptions)
            .into(holder.iv_ticket_image_in_fleet_details_ticket)
    }

    override fun getItemCount(): Int {
        return tickets!!.size
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        var iv_ticket_image_in_fleet_details_ticket: ImageView =
            itemView.findViewById(R.id.iv_ticket_image_in_fleet_details_ticket)
        var ct_ticket_name_in_fleet_details_ticket: CustomTextView =
            itemView.findViewById(R.id.ct_ticket_name_in_fleet_details_ticket)
        var ct_ticket_category_in_fleet_details_ticket: CustomTextView =
            itemView.findViewById(R.id.ct_ticket_category_in_fleet_details_ticket)
        var ct_ticket_date_in_fleet_details_ticket: CustomTextView =
            itemView.findViewById(R.id.ct_ticket_date_in_fleet_details_ticket)

    }




}