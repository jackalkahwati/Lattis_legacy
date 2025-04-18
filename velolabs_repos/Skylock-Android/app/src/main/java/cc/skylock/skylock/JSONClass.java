package cc.skylock.skylock;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexVijayRaj on 9/14/2015.
 */
public class JSONClass {

    //public static String url = "http://api.tiles.mapbox.com/v4/directions/mapbox.driving/-122.421215,37.761734;-122.421409,37.759919.json?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6IlhHVkZmaW8ifQ.hAMX5hSW-QnTeRCMAy9A8Q";

    int GET = 1;
    int POST = 2;
    int PUT = 3;
    int requestType = 1;
    List<NameValuePair> nameValuePair;
    StringEntity se = null;
    String url;
    //ProgressDialog pDialog;

    StringBuilder sb = new StringBuilder();

    TextView output;

    JSONArray contacts = null;
    JSONArray coordinatesArray;

    Context context;

    public JSONClass(Context contextTemp){
        context = contextTemp;
    }

    public JSONObject executeJSON(){

        JSONObject temp1 = null;
        //pDialog = new ProgressDialog(context);
        try {
            temp1 = new GetJSON().execute().get();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp1;

    }

    public void putURL(String urlTemp, int requestTypeTemp, List<NameValuePair> nameValuePairTemp, StringEntity seTemp){
        url = urlTemp;
        requestType = requestTypeTemp;
        nameValuePair = nameValuePairTemp;
        se = seTemp;
    }

    private class GetJSON extends AsyncTask<JSONObject, Void, JSONObject> {


        String temp;
        JSONArray coordinatesArray1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            //pDialog.setMessage("Please wait...");
            //pDialog.setCancelable(false);
            //pDialog.show();

        }

        @Override
        protected JSONObject doInBackground(JSONObject... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            JSONObject jsonObj = null;


            String jsonStr = sh.makeServiceCall(url, requestType,nameValuePair,se);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    jsonObj = new JSONObject(jsonStr);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return jsonObj;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
           // if (pDialog.isShowing())
            //    pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            //temp = sb.toString();

            //output.setText("Success " + "\n" + sb.toString());


        }

    }


    private class ServiceHandler {

        String response = null;
        public final static int GET = 1;
        public final static int POST = 2;
        public final static int PUT = 3;

        public ServiceHandler() {

        }

        /**
         * Making service call
         *
         * @url - url to make request
         * @method - http request method
         */
        public String makeServiceCall(String url, int method) {
            return this.makeServiceCall(url, method, null);
        }

        /**
         * Making service call
         *
         * @url - url to make request
         * @method - http request method
         * @params - http request params
         */
        public String makeServiceCall(String url, int method,
                                      List<NameValuePair> params){
            UrlEncodedFormEntity tempUrl = null;
            try {
                tempUrl = new UrlEncodedFormEntity(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return this.makeServiceCall(url, method, params,tempUrl);
        }
        public String makeServiceCall(String url, int method,
                                      List<NameValuePair> params, StringEntity se) {
            try {
                // http client
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;

                // Checking http request method type
                if (method == POST) {
                    HttpPost httpPost = new HttpPost(url);
                    // adding post params
                    if (params != null) {
                        httpPost.setEntity(se);
                    }

                    httpResponse = httpClient.execute(httpPost);

                } else if (method == GET) {
                    // appending params to url
                    if (params != null) {
                        String paramString = URLEncodedUtils
                                .format(params, "utf-8");
                        url += "?" + paramString;
                    }
                    HttpGet httpGet = new HttpGet(url);

                    httpResponse = httpClient.execute(httpGet);

                }else if (method == PUT){
                    HttpPut httpPut = new HttpPut(url);
                    if (params != null) {
                        httpPut.setEntity(se);
                    }

                    httpResponse = httpClient.execute(httpPut);

                }
                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;

        }
    }
}
