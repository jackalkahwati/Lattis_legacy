package com.lattis.lattis.presentation.help

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter
import io.lattis.lattis.R

class SliderImageAdapter (
    var context:Context,
    var images:List<Int>
    ):SliderViewAdapter<SliderImageAdapter.SliderImageAdapterVH>()
{

    override fun onCreateViewHolder(parent: ViewGroup): SliderImageAdapterVH {
        var inflate:View = LayoutInflater.from(parent.context).inflate(R.layout.layout_image_slider_item,null)
        return SliderImageAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderImageAdapterVH, position: Int) {
        val image = images[position]
        Glide.with(viewHolder.itemView)
            .load(image)
            .fitCenter()
            .into(viewHolder.image)
    }

    override fun getCount(): Int {
        return images.size
    }

    class SliderImageAdapterVH(itemView:View): SliderViewAdapter.ViewHolder(itemView){
        var image:ImageView
        init {
            image = itemView.findViewById(R.id.iv_image_slider)
        }
    }
}