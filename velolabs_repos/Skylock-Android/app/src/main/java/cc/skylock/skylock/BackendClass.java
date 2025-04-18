package cc.skylock.skylock;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.style.SuperscriptSpan;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexVijayRaj on 10/7/2015.
 */
public class BackendClass {

    Context context;
    ObjectRepo objRepo;
    JSONClass objJSON;
    List<NameValuePair> nameValuePair;
    StringEntity se = null;
    String fb_id;
    int GET = 1;
    int POST = 2;
    int PUT = 3;

    private static final String BASE_URL = "https://skylock-beta.herokuapp.com";
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";

    public BackendClass(Context context1, ObjectRepo objRepo1){
        context = context1;
        objRepo = objRepo1;
        objJSON = objRepo.objJSON;
        fb_id = objRepo.fb_id;
    }

    public boolean createUser(String firstName, String lastName, String fb_id, String username){
        boolean returnValue = false;
        try {

            JSONObject json = new JSONObject();
            String url = BASE_URL + "/user/";

            try {

                json.put("first_name", ""+firstName);
                json.put("fb_id", ""+fb_id);
                json.put("last_name", ""+lastName);
                json.put("user_name", ""+username);

                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, POST, nameValuePair, se);
            json = objJSON.executeJSON();
            if(json.getString(STATUS).equals(SUCCESS)){
                returnValue = true;
            }
        }catch (Exception e){}
        return returnValue;
    }

    public String addLock(String MacAddress){
            String returnValue = "success";
        try {
            JSONObject json = new JSONObject();
            String url = BASE_URL + "/users/" + fb_id + "/keys/";
            try {
                json.put("mac_id", ""+MacAddress);
                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, POST, nameValuePair, se);
            json = objJSON.executeJSON();
            if(!json.getString(STATUS).equals(SUCCESS)){
                returnValue = json.getString(STATUS);
                Toast.makeText(context, ""+ returnValue,
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){}
        return returnValue;
    }

    public String grantSharing(String fb_id_guest, String MacAddress){
        try {
            JSONObject json = new JSONObject();
            String url = BASE_URL + "/users/" + fb_id + "/share/";
            try {
                json.put("mac_id", ""+MacAddress);
                json.put("shared_to", ""+fb_id_guest);
                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, POST, nameValuePair, se);
            json = objJSON.executeJSON();
            if(!json.getString(STATUS).equals(SUCCESS)){
                json.getString(STATUS);
                Toast.makeText(context, ""+ json.getString(STATUS),
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){}
        return null;
    }

    public boolean revokeSharing(String fb_id_guest, String MacAddress){
        try {
            JSONObject json = new JSONObject();
            String url = BASE_URL + "/users/" + fb_id + "/unshare/";
            try {
                json.put("mac_id", ""+MacAddress);
                json.put("fb_id", ""+fb_id_guest);
                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, POST, nameValuePair, se);
            json = objJSON.executeJSON();
            if(!json.getString(STATUS).equals(SUCCESS)){
                json.getString(STATUS);
                Toast.makeText(context, ""+ json.getString(STATUS),
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){}
        return false;
    }

    public void addPhoneNumber(String Number){
        try {
            JSONObject json = new JSONObject();
            String url = BASE_URL + "/users/" + fb_id + "/mobiles/";
            try {
                json.put("mobile", ""+Number);

                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, PUT, nameValuePair, se);
            json = objJSON.executeJSON();
            if(!json.getString(STATUS).equals(SUCCESS)){
                json.getString(STATUS);
                Toast.makeText(context, ""+ json.getString(STATUS),
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){}
    }


    public void putEmergencyContacts1(String Number, String Name){
        try {
            JSONObject json = new JSONObject();
            String url = BASE_URL + "/mobiles/" + fb_id + "/usermob_e1/";
            try {
                json.put("emergency_contact1", ""+Number);
                json.put("emergency_contact1_name", ""+Name);
                se = new StringEntity("" + json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("requestcode", "10"));
            nameValuePair.add(new BasicNameValuePair("devicetype", "phone"));
            objJSON.putURL(url, PUT, nameValuePair, se);
            json = objJSON.executeJSON();
            if(!json.getString(STATUS).equals(SUCCESS)){
                json.getString(STATUS);
                Toast.makeText(context, ""+ json.getString(STATUS),
                        Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){}
    }



}
