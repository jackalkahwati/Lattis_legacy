package cc.skylock.skylocktestapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by AlexVijayRaj on 10/13/2015.
 */
public class FirmwareUpdate {

    //JSON size(0 to 292 = 293)
    int fileSize = 293;
    int GET = 1;
    int POST = 2;
    Context context;
    private String[] fwUpdateArray = new String[fileSize];
    private static final String TAG_NAME = "boot_loader";
    private static final String TAG_PAYLOAD = "payload";

    JSONObject tempJSONObject;

    public FirmwareUpdate(Context context1){
        context = context1;
        tempJSONObject = getJSONObject();
    }

    public String[] getStringArray(){
        //JSON size(0 to 257 = 258)
        for(int i=0; i<fileSize; i++){
            fwUpdateArray[i] = getStringFromJSON(i);
        }
        return fwUpdateArray;
    }

    private String getStringFromJSON(int tempInt){

        String tempString = null;
        try {
            JSONArray tempArray= tempJSONObject.getJSONArray(TAG_PAYLOAD);
            String temp = String.valueOf(tempArray.length());
            tempString = tempArray.getJSONObject(tempInt).getString(TAG_NAME);

        } catch (JSONException e) {}

        return tempString;
    }

    private JSONObject getJSONObject(){
        JSONClass objJSONClass = new JSONClass(context);
        //String url = "https://skylock-beta.herokuapp.com/updates/";
        String url = "https://skylock-beta.herokuapp.com/api/v1/updates/";
        objJSONClass.putURL(url, GET, null, null);
        JSONObject temp = objJSONClass.executeJSON();
        //Log.i("Fw Update", "" +temp.toString());
        return temp;
    }

}
