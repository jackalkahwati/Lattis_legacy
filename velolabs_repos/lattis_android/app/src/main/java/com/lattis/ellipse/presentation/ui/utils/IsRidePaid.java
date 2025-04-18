package com.lattis.ellipse.presentation.ui.utils;

/**
 * Created by ssd3 on 8/3/17.
 */

public class IsRidePaid {


    public final static String FLEET_TYPE_PUBLIC_PAYMENT = "public";
    public final static String FLEET_TYPE_PUBLIC_NO_PAYMENT = "public_no_payment";
    public final static String FLEET_TYPE_PRIVATE_PAYMENT = "private";
    public final static String FLEET_TYPE_PRIVATE_NO_PAYMENT = "private_no_payment";

    public static boolean isRidePaidForFleet(String fleet_type){
        if(fleet_type==null){
            return false;
        }else if(fleet_type.equals("")){
            return false;
        }else if(fleet_type.equalsIgnoreCase(FLEET_TYPE_PUBLIC_PAYMENT) || fleet_type.equalsIgnoreCase(FLEET_TYPE_PRIVATE_PAYMENT)){
            return true;
        }else{
            return false;
        }
    }

}
