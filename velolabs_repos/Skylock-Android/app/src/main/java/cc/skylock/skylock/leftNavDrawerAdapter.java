package cc.skylock.skylock;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;


/**
 * Created by AlexVijayRaj on 7/11/2015.
 */
public class leftNavDrawerAdapter extends BaseAdapter {

    String[] locks = new String[5];
    private final Context context;

    View row = null;

    Profile profile;
    ProfilePictureView ivUserPic;
    TextView tvUserName;


    public leftNavDrawerAdapter(Context context1){

        this.context = context1;

    }


    @Override
    public int getCount() {
        return 5;
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
                    row = inflater.inflate(R.layout.left_nav_drawer_0, parent, false);
                    row.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            row.setBackgroundColor(Color.parseColor("#ffffff"));
                            return false;
                        }
                    });
                    ivUserPic = (ProfilePictureView) row.findViewById(R.id.ivUserPic);
                    ivUserPic.setPresetSize(-2);
                    tvUserName = (TextView) row.findViewById(R.id.tvUserName);
                    if(profile != null){
                        ivUserPic.setProfileId(profile.getId());
                        tvUserName.setText(""+profile.getName());
                    }

//                } else if (position == 1) {
//
//                    row = inflater.inflate(R.layout.left_nav_drawer_lock, parent, false);
//                    //row = inflater.inflate(R.layout.left_nav_drawer_null, parent, false);

                } else if (position == 1) {
                    row = inflater.inflate(R.layout.left_nav_drawer_sharing, parent, false);
                } else if (position == 2) {
                    row = inflater.inflate(R.layout.left_nav_drawer_add_lock, parent, false);
                } else if (position == 3) {
                    row = inflater.inflate(R.layout.left_nav_drawer_store, parent, false);
                } else if (position == 4) {
                    row = inflater.inflate(R.layout.left_nav_drawer_help, parent, false);


                }

        } else {
            row = convertView;
        }

        return row;
    }

    public void putProfile(Profile profile1){
        profile = profile1;

    }
}
