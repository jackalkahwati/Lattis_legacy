package cc.skylock.skylock;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
 * Created by AlexVijayRaj on 9/14/2015.
 */
public class BackendTest {

    Context context;
    Dialog dialog;
    Button bUserA, bUserB, bShare, bPullA, bPullB, bRevoke, bgetFBPic;
    TextView tvTest;
    ImageView fbPic;
    JSONClass objJSONClass;
    ObjectRepo objRepo;
    List<NameValuePair> nameValuePair;
    StringEntity se = null;
    int GET = 1;
    int POST = 2;

    public BackendTest(Context contextTemp, ObjectRepo objRepo1){
        context = contextTemp;
        objRepo = objRepo1;
        
        init();
        setOnClickListeners();
    }

    public void showBackendTest(){
        dialog.show();
    }

    private void init() {
        dialog=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogZoom;
        dialog.setContentView(R.layout.backend_test);
        objJSONClass = new JSONClass(context);

        bUserA = (Button) dialog.findViewById(R.id.bTest);
        bUserB = (Button) dialog.findViewById(R.id.bTest1);
        bShare = (Button) dialog.findViewById(R.id.bTest2);
        bPullA = (Button) dialog.findViewById(R.id.bTest3);
        bPullB = (Button) dialog.findViewById(R.id.bTest4);
        bRevoke = (Button) dialog.findViewById(R.id.bTest5);
        bgetFBPic = (Button) dialog.findViewById(R.id.bTest6);
        fbPic = (ImageView) dialog.findViewById(R.id.ivFBPic);
        tvTest = (TextView) dialog.findViewById(R.id.tvTest);
    }

    private void setOnClickListeners() {
        bUserA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/user/";

                    try {

                        json.put("first_name", "Alex");
                        json.put("fb_id", "12345");
                        json.put("last_name", "Vijay Raj");
                        json.put("user_name", "alexvijayraj");

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
                    objJSONClass.putURL(url, POST, nameValuePair, se);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }*/

                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/users/12345/keys/";

                    try {

                        json.put("mac_id", "FD:0E:6E:12:2F:BD");

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
                    objJSONClass.putURL(url, POST, nameValuePair, se);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bUserB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/user/";

                    try {

                        json.put("first_name", "Natalie");
                        json.put("fb_id", "54321");
                        json.put("last_name", "Goldstein");
                        json.put("user_name", "natalie");

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
                    objJSONClass.putURL(url, POST, nameValuePair, se);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                    }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/users/12345/share/";

                    try {

                        json.put("shared_to", "54321");
                        json.put("mac_id", "FD:0E:6E:12:2F:BD");

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
                    objJSONClass.putURL(url, POST, nameValuePair, se);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bRevoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/users/12345/unshare_sharer/";

                    try {

                        json.put("shared_to", "54321");
                        json.put("mac_id", "FD:0E:6E:12:2F:BD");

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
                    objJSONClass.putURL(url, POST, nameValuePair, se);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bPullA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/users/12345/share_details/";
                    objJSONClass.putURL(url, GET, null, null);
                    json = objJSONClass.executeJSON();
                    tvTest.setText(""+json.toString());
                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bPullB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject json = new JSONObject();
                    String url = "https://skylock-beta.herokuapp.com/users/54321/share_details/";
                    objJSONClass.putURL(url, GET, null, null);
                    json = objJSONClass.executeJSON();
                    String temp = json.getJSONArray("Locks_Shred_to_ME").getJSONObject(0).getString("mac_id");

                    tvTest.setText(""+temp);

                }catch (Exception e){
                    tvTest.setText(""+e.toString());
                }
            }
        });

        bgetFBPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }



}
