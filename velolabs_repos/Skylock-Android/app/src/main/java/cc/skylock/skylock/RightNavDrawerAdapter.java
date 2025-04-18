package cc.skylock.skylock;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by AlexVijayRaj on 7/11/2015.
 */
public class RightNavDrawerAdapter extends BaseAdapter {

    String[] locks = new String[5];
    private final Context context;

    View row = null;
    ObjectRepo objRepo;
    ImageButton ibToggleCycling, ibClose, ibSearchClose;
    TextView tvDistance;
    Button bGetCyclingDirections;
    Dialog dialog;
    AutoCompleteTextView mAutocompleteView;

    int navDrawerSize = 1;
    String[] strHTMLArray, strDistanceArray = null;

    public RightNavDrawerAdapter(Context context1, ObjectRepo objRepo1){

        context = context1;
        objRepo = objRepo1;
        init();

    }

    private void init() {
        dialog=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dialog.setContentView(R.layout.cycling_directions);

        ibSearchClose = (ImageButton) dialog.findViewById(R.id.ibBack);
        bGetCyclingDirections = (Button) dialog.findViewById(R.id.bGetCyclingDirections);
        ibSearchClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resetRightNavDrawer();
            }
        });
        mAutocompleteView = (AutoCompleteTextView) dialog.findViewById(R.id.tvAutoComplete);

        bGetCyclingDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mAutocompleteView.getText().toString().replace(" ", ",");
                objRepo.objMapActivity.getCyclingDirections(address);
                dialog.dismiss();

            }
        });



        mAutocompleteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    //String[] address = getString("" + s);
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, address);
                    //mAutocompleteTextView.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }

    public void setDirections(int size, String totalDistance, String[] strDistanceArray1, String[] strHTMLArray1){
        navDrawerSize = size+1;
        tvDistance.setText("" + totalDistance);
        strDistanceArray = strDistanceArray1;
        strHTMLArray = strHTMLArray1;
        ibToggleCycling.setImageResource(R.drawable.cycling_enable);
        ibToggleCycling.setTag("active");

    }

    public void resetRightNavDrawer(){
        ibToggleCycling.setImageResource(R.drawable.cycling_disable);
        ibToggleCycling.setTag("inactive");
        tvDistance.setText("---");
        mAutocompleteView.setText("");
    }



    @Override
    public int getCount() {
        return navDrawerSize;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView==null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (position == 0) {
                row = inflater.inflate(R.layout.right_nav_drawer_0, parent, false);
                ibClose = (ImageButton) row.findViewById(R.id.ibClose);
                ibClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        objRepo.mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    }
                });
                ibToggleCycling = (ImageButton) row.findViewById(R.id.ibToggleCycling);
                ibToggleCycling.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ibToggleCycling.getTag().toString().equals("inactive")){
                            dialog.show();
                            ibToggleCycling.setImageResource(R.drawable.cycling_enable);
                            ibToggleCycling.setTag("active");
                        }else{
                            ibToggleCycling.setImageResource(R.drawable.cycling_disable);
                            ibToggleCycling.setTag("inactive");
                        }
                    }
                });
                tvDistance = (TextView) row.findViewById(R.id.tvDistance);


            } else if (position == 1) {
                row = inflater.inflate(R.layout.right_nav_drawer_1, parent, false);
                ImageView ivIcon = (ImageView) row.findViewById(R.id.ivIcon);
                ivIcon.setImageResource(R.drawable.icon_start);
                TextView tvDirection = (TextView) row.findViewById(R.id.tvDirection);
                tvDirection.setText(strHTMLArray[position - 1]);
                TextView tvShortDistance = (TextView) row.findViewById(R.id.tvShortDistance);
                tvShortDistance.setText(strDistanceArray[position-1]);

            }else if (position == (navDrawerSize-1)) {
                row = inflater.inflate(R.layout.right_nav_drawer_1, parent, false);
                ImageView ivIcon = (ImageView) row.findViewById(R.id.ivIcon);
                ivIcon.setImageResource(R.drawable.icon_finish);
                TextView tvDirection = (TextView) row.findViewById(R.id.tvDirection);
                tvDirection.setText(strHTMLArray[position - 1]);
                TextView tvShortDistance = (TextView) row.findViewById(R.id.tvShortDistance);
                tvShortDistance.setText(strDistanceArray[position-1]);

            }else{
                row = inflater.inflate(R.layout.right_nav_drawer_1, parent, false);
                ImageView ivIcon = (ImageView) row.findViewById(R.id.ivIcon);
                ivIcon.setImageResource(R.drawable.icon_inbetween);
                TextView tvDirection = (TextView) row.findViewById(R.id.tvDirection);
                tvDirection.setText(strHTMLArray[position - 1]);
                TextView tvShortDistance = (TextView) row.findViewById(R.id.tvShortDistance);
                tvShortDistance.setText(strDistanceArray[position-1]);
            }


        } else {
            row = convertView;
        }

        return row;
    }


}


