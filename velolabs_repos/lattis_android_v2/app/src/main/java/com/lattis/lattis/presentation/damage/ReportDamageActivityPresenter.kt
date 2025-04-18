package com.lattis.lattis.presentation.damage

import android.text.TextUtils
import com.lattis.domain.models.UploadImage
import com.lattis.domain.usecase.maintenance.ReportDamageUseCase
import com.lattis.domain.usecase.uploadimage.UploadImageUseCase
import com.lattis.lattis.presentation.base.activity.ActivityPresenter
import com.lattis.lattis.presentation.base.activity.usercurrentstatus.BaseUserCurrentStatusPresenter
import com.lattis.lattis.presentation.ui.base.RxObserver
import javax.inject.Inject


class ReportDamageActivityPresenter @Inject constructor(
    val uploadImageUseCase: UploadImageUseCase,
    val reportDamageUseCase: ReportDamageUseCase
) : BaseUserCurrentStatusPresenter<ReportDamageActivityView>(){
    private var notes:String?=null
    private var imageLink:String?=null
    private var s3ImageLink:String?=null
    private var startReportDamageAfterStatus = false


    fun setNotes(notes:String?){
        this.notes = notes
        checkForCompleteness()
    }

    fun setImageLink(imageLink:String?){
        this.imageLink = imageLink
        checkForCompleteness()
    }

    fun checkForCompleteness(){
        view?.activateSubmitBtn(!TextUtils.isEmpty(notes) && !TextUtils.isEmpty(imageLink) )
    }


    fun uploadImage() {
        subscriptions.add(uploadImageUseCase
            .withFilePath(imageLink!!)
            .withUploadType("maintenance")
            .execute(object : RxObserver<UploadImage>() {
                override fun onNext(uploadImage: UploadImage) {
                    super.onNext(uploadImage)
                    view?.onUploadImageSuccess()
                    s3ImageLink = uploadImage.path
                    reportDamage()
                }

                override fun onError(e: Throwable) {
                    view?.onUploadImageFailure()
                }
            }))
    }

    fun checkOnStatus(){
        if(startReportDamageAfterStatus){
            reportDamage()
        }
    }

    fun reportDamage(

    ) {
        if(bike==null){
            startReportDamageAfterStatus=true
            userCurrentStatus()
            return
        }

        subscriptions.add(
            reportDamageUseCase
                .withBikeId(bike?.bike_id!!)
                .withTripId(if(ride==null)0 else ride?.rideId!!)
                .withRiderNotes(notes)
                .withMaintenanceImage(s3ImageLink)
                .execute(object : RxObserver<Boolean>(view) {
                    override fun onNext(t: Boolean) {
                        super.onNext(t)
                        view?.showViewWith(ride!=null && ride?.rideId!=0)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        view?.onReportDamageFailure()
                    }
                })
        )
    }

}