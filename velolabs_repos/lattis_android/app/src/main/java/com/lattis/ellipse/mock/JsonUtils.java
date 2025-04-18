package com.lattis.ellipse.mock;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


public class JsonUtils {

    /**
     * Converts a JSON String to an Object of parameterized type.
     *
     * @param jsonString JSON String that will be deserialized
     * @param clazz      Class of T
     * @param <T>        Type of desired object
     * @return Object of type {@link T}
     */
    public static <T> T convertJsonString(String jsonString, Class<T> clazz) {
        T object = null;

        try {
            object = new GsonBuilder().create().fromJson(jsonString, clazz);
        } catch (JsonSyntaxException e) {
            Log.i("JsonUtils", jsonString);
        }

        return object;
    }

    public static String convertJsonObject(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
