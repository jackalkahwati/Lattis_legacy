package com.lattis.ellipse.mock;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;

import io.reactivex.Observable;

public class MockUtils {

    /**
     * Return a JSON String from a JSON file in "assets/json" directory.
     *
     * @param context  Application context
     * @param filename Json filename in "assets/json" directory
     * @return Json string of file.
     */
    public static String getJsonStringAsset(Context context, String filename) {
        String json = "";

        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open("json/" + filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception e) {
            Log.i("MockUtils", filename);
        }

        return json;
    }


    public static <T> T jsonAssetToObject(Context context, String filename, Class<T> clazz) {
        String jsonString = MockUtils.getJsonStringAsset(context, filename);
        return JsonUtils.convertJsonString(jsonString, clazz);
    }

     /*(public static <T> Observable<T> generateHttpException(RequestError errorResponse) {
        //Response response = Response.error(errorResponse.getErrorCode(), ResponseBody.create(MediaType.parse("application/json"), errorResponse.getErrorMessage()));
        //return Observable.error(new HttpException(response));
        return null;
    }*/

     public static <T> Observable<T> generateHttpExceptionFromAsset(Context context, String filename) {
        //RequestError errorResponse = jsonAssetToObject(context, filename, RequestError.class);
        //return generateHttpException(errorResponse);
        return null;
    }
}
