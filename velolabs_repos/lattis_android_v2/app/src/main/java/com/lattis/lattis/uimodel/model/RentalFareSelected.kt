package com.lattis.lattis.uimodel.model

import com.lattis.domain.models.Bike

class RentalFareSelected {
    var newPayPerUseSelected:Boolean=false
    var finalPayPerUseSelected:Boolean=false
    var rentalFareSelected:Boolean=false
    var newRentalFareSelectedIndex:Int=-1
    var finalRentalFareSelectedIndex:Int=-1
    var pricingOptionSelected:Bike.Pricing_options?=null
}