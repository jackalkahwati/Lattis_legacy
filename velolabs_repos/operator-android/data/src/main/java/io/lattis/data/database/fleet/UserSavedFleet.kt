package io.lattis.data.database.fleet

import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import io.lattis.domain.models.Fleet
import javax.inject.Inject


class UserSavedFleet @Inject constructor(
        private val sharedPreferences: SharedPreferences
) {
    private val USER_FLEET = "USER_FLEET"

    fun saveFleet(fleet: Fleet){
        val prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
        val userFleet: String = Gson().toJson(fleet)
        prefsEditor.putString(USER_FLEET, userFleet)
        prefsEditor.commit()
    }

    fun getFleet():Fleet?{
        if(sharedPreferences.contains(USER_FLEET) && sharedPreferences.getString(USER_FLEET, null)!=null){
            val userFleet: String? = sharedPreferences.getString(USER_FLEET, null)
            return Gson().fromJson(userFleet, Fleet::class.java)
        }
       return null
    }

    fun deleteFleet(){
        if(sharedPreferences.contains(USER_FLEET)){
            val prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
            prefsEditor.remove(USER_FLEET)
            prefsEditor.commit()
        }
    }

}