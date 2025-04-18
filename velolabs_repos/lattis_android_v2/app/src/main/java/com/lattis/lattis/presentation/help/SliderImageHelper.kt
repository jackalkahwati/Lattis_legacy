package com.lattis.lattis.presentation.help

import android.content.Context
import android.view.View
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import io.lattis.lattis.BuildConfig
import io.lattis.lattis.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.layout_image_slider_parent.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object SliderImageHelper {

    fun openSliderImage(context: Context,image_slider_parent: View){
        if(imageSliderApplies()) {
            Observable.timer(500,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    var images = ArrayList<Int>()
                    getImagesDependingUponFlavor(images)
                    image_slider_parent.imageSliderView.setSliderAdapter(SliderImageAdapter(context, images),false)
                    image_slider_parent.imageSliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
                    image_slider_parent.imageSliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
                    image_slider_parent.imageSliderView.stopAutoCycle()
                    image_slider_parent.visibility=View.VISIBLE
                    image_slider_parent.ct_skip.setOnClickListener {
                        image_slider_parent.visibility=View.GONE
                    }
                },{

                })
        }else{
            image_slider_parent.visibility=View.GONE
        }
    }

    fun getImagesDependingUponFlavor(images: ArrayList<Int>){
        if(BuildConfig.FLAVOR_product.equals("guestbike")){
            getGuestBikeTutorialImage(images)
        }else if (BuildConfig.FLAVOR_product.equals("bandwagon")){
            getBandWagonTutorialImages(images)
        }else if (BuildConfig.FLAVOR_product.equals("wave")){
            getWaveCoTutorialImages(images)
        }else if (BuildConfig.FLAVOR_product.equals("mount")){
            getMountTutorialImages(images)
        }else if (BuildConfig.FLAVOR_product.equals("fin")){
            getFinTutorialImages(images)
        }else if (BuildConfig.FLAVOR_product.equals("wawe")){
            getWaweTutorialImages(images)
        }else if (BuildConfig.FLAVOR_product.equals("monkeydonkey")){
            getMonkeydonkeyTutorialImage(images)
        }
    }


    fun getMonkeydonkeyTutorialImage(images: ArrayList<Int>){
        images.add(R.drawable.monkeydonkey_tutorial_1)
        images.add(R.drawable.monkeydonkey_tutorial_2)
        images.add(R.drawable.monkeydonkey_tutorial_3)
        images.add(R.drawable.monkeydonkey_tutorial_4)
        images.add(R.drawable.monkeydonkey_tutorial_5)
        images.add(R.drawable.monkeydonkey_tutorial_6)
    }

    fun getGuestBikeTutorialImage(images: ArrayList<Int>){
        images.add(R.drawable.gb_tutorial_1)
        images.add(R.drawable.gb_tutorial_2)
        images.add(R.drawable.gb_tutorial_3)
        images.add(R.drawable.gb_tutorial_4)
    }

    fun getBandWagonTutorialImages(images: ArrayList<Int>){
        images.add(R.drawable.bandwagon_tutorial_1)
        images.add(R.drawable.bandwagon_tutorial_2)
        images.add(R.drawable.bandwagon_tutorial_3)
        images.add(R.drawable.bandwagon_tutorial_4)
        images.add(R.drawable.bandwagon_tutorial_5)
    }

    fun getWaveCoTutorialImages(images: ArrayList<Int>){
        images.add(R.drawable.wave_tutorial_1)
        images.add(R.drawable.wave_tutorial_2)
    }

    fun getMountTutorialImages(images: ArrayList<Int>){
        images.add(R.drawable.mount_tutorial_1)
        images.add(R.drawable.mount_tutorial_2)
        images.add(R.drawable.mount_tutorial_3)
        images.add(R.drawable.mount_tutorial_4)
    }

    fun getFinTutorialImages(images: ArrayList<Int>){
        images.add(R.drawable.fin_tutorial_1)
        images.add(R.drawable.fin_tutorial_2)
        images.add(R.drawable.fin_tutorial_3)
        images.add(R.drawable.fin_tutorial_4)
        images.add(R.drawable.fin_tutorial_5)
    }

    fun getWaweTutorialImages(images: ArrayList<Int>){
        images.add(R.drawable.wawe_tutorial_1)
        images.add(R.drawable.wawe_tutorial_2)
        images.add(R.drawable.wawe_tutorial_3)
        images.add(R.drawable.wawe_tutorial_4)
        images.add(R.drawable.wawe_tutorial_5)
        images.add(R.drawable.wawe_tutorial_6)
        images.add(R.drawable.wawe_tutorial_7)
        images.add(R.drawable.wawe_tutorial_8)
        images.add(R.drawable.wawe_tutorial_9)
    }



    fun imageSliderApplies():Boolean{
        return BuildConfig.FLAVOR_product.equals("guestbike") ||
                BuildConfig.FLAVOR_product.equals("bandwagon") ||
                BuildConfig.FLAVOR_product.equals("wave") ||
                BuildConfig.FLAVOR_product.equals("mount") ||
                BuildConfig.FLAVOR_product.equals("fin") ||
                BuildConfig.FLAVOR_product.equals("wawe") ||
                BuildConfig.FLAVOR_product.equals("monkeydonkey")
    }
}