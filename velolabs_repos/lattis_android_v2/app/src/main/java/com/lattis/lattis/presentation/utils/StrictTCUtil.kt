package com.lattis.lattis.presentation.utils

import io.lattis.lattis.BuildConfig

object StrictTCUtil {

    val grinTC1 = "He leido y estoy de acuerdo con los terminos y condiciones de la web."
    val grinTC2 = "Tras leer la politica de privacidad, autorizo el tratamiento de mis datos personales para los propositos especificados en ella."
    val grinTCLink = "Puedes consultar informacion adicional y detallada sobre Proteccion de Datos en la <a href=\"%s\">Politicia de Privacidad</a>, y en los <a href=\"%s\">Terminos y Condiciones</a>"

    fun getStrictTCString():List<String>?{
        return when(BuildConfig.FLAVOR_product){
            "grin" ->  {
                listOf(grinTC1, grinTC2)
            }
            else -> return null
        }
    }

    fun getStrictTCLink():String?{
        return when(BuildConfig.FLAVOR_product){
            "grin" -> grinTCLink
            else -> null
        }
    }

    fun hasStrictTC():Boolean{
        return when(BuildConfig.FLAVOR_product){
            "grin" -> true
            else -> false
        }
    }
}