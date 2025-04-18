package com.lattis.ellipse.Utils;

import android.content.Context;

public class LocaleTranslatorUtils {

    public static String getLocaleString(Context context, String word){
        return  getStringResourceByName(context,word);
    }

    private static String getStringResourceByName(Context context,String word) {
        try {
            if (word != null) {
                String packageName = context.getPackageName();
                int resId = context.getResources().getIdentifier(word.toLowerCase(), "string", packageName);
                return context.getString(resId);
            }
        }catch(Exception r){

        }
        return word;
    }

}
