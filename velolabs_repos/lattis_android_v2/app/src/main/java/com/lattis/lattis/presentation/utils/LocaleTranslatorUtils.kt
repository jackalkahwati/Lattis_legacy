package com.lattis.lattis.presentation.utils

import android.content.Context
import com.lattis.lattis.utils.UtilsHelper.isNumber

object LocaleTranslatorUtils {
    fun getLocaleString(context: Context, word: String?,count:String?=null): String? {
        return if(count!=null && isNumber(count) && count.toIntOrNull()!=null){
            getQuantityResourceByName(context,word,count.toInt())
        }else {
            getStringResourceByName(context,word)
        }
    }

    private fun getQuantityResourceByName(
                                          context: Context,
                                          word: String?,
                                          count:Int
    ): String? {
        try {
            if (word != null) {
                val packageName = context.packageName
                val resId =
                    context.resources.getIdentifier(word.lowercase(), "plurals", packageName)
                return context.resources.getQuantityString(resId,count,count)
            }
        } catch (r: Exception) {

        }
        return getStringResourceByName(context,word)
    }

    private fun getStringResourceByName(
        context: Context,
        word: String?
    ): String? {
        try {
            if (word != null) {
                val packageName = context.packageName
                val resId =
                    context.resources.getIdentifier(word.lowercase(), "string", packageName)
                return context.getString(resId)
            }
        } catch (r: Exception) {
        }
        return word
    }
}