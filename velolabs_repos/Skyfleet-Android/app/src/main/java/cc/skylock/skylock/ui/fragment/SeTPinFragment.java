package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.ui.AddLockActivity;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SharedPreference.Myconstants;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 18-08-2016.
 */
public class SeTPinFragment extends Fragment implements View.OnClickListener {
    private TextView textView_header;
    private Context mContext;
    private LinearLayout linearLayout_imageList;
    private ImageView imageView_Top, imageView_Bottom, imageView_Left, imageView_Right, imageView_clearpin, imageView_pin;
    private int count = 0;
    private LinearLayout.LayoutParams layoutParams;
    private CardView cardView_savePin;
    private View v;
    private TextView textView_label_savepin;
    private String pinCode = "";
    private PrefUtil mPrefUtil;
    String[] touchPadSequence = new String[8];

    public static SeTPinFragment newInstance() {
        SeTPinFragment f = new SeTPinFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_setpin, container, false);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        cardView_savePin = (CardView) v.findViewById(R.id.cv_save_button);
        linearLayout_imageList = (LinearLayout) v.findViewById(R.id.ll_imagelist);
        imageView_Top = (ImageView) v.findViewById(R.id.iv_top_pin);
        imageView_Bottom = (ImageView) v.findViewById(R.id.iv_bottom_pin);
        imageView_Left = (ImageView) v.findViewById(R.id.iv_left_pin);
        imageView_Right = (ImageView) v.findViewById(R.id.iv_right_pin);
        textView_header = (TextView) v.findViewById(R.id.tv_title);
        imageView_clearpin = (ImageView) v.findViewById(R.id.iv_clearpin);
        textView_label_savepin = (TextView) v.findViewById(R.id.tv_touch_button);
        imageView_Top.setOnClickListener(this);
        imageView_Bottom.setOnClickListener(this);
        imageView_Left.setOnClickListener(this);
        imageView_Right.setOnClickListener(this);
        imageView_clearpin.setOnClickListener(this);
        cardView_savePin.setOnClickListener(this);
        textView_header.setTypeface(UtilHelper.getTypface(mContext));

        UtilHelper.analyticTrackUserAction("Pincode changed","Custom","Lock settings",null, "ANDROID");
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_top_pin: {
                addPinImage(R.drawable.icon_input_top, "01", "up");
            }
            break;
            case R.id.iv_bottom_pin: {
                addPinImage(R.drawable.icon_input_down, "04", "down");
            }
            break;
            case R.id.iv_left_pin: {
                addPinImage(R.drawable.icon_input_left, "08", "left");
            }
            break;
            case R.id.iv_right_pin: {
                addPinImage(R.drawable.icon_input_right, "02", "right");
            }
            break;
            case R.id.iv_clearpin: {
                clearPinImage();
            }
            break;
            case R.id.cv_save_button: {
                for (int i = pinCode.length(); i < 16; i++) {
                    pinCode = pinCode + "0";
                }
                mPrefUtil.setBooleanPref(Myconstants.KEY_USER_LOCK_SET_PIN, true);
                ((AddLockActivity) getActivity()).setCapPin(pinCode,touchPadSequence);
            }
            break;
        }

    }

    private void addPinImage(int drawable, String pin, String requestPinCode) {
        if (count < 8) {
            pinCode = pinCode + pin;
            touchPadSequence[count] = requestPinCode;
            layoutParams = new LinearLayout.LayoutParams(65, 65);
            imageView_pin = new ImageView(getActivity());
            imageView_pin.setId(count);
            imageView_pin.setTag(count);
            imageView_pin.setLayoutParams(layoutParams);
            imageView_pin.setBackgroundResource(drawable);
            linearLayout_imageList.addView(imageView_pin);
            count++;
            if (count >= 4) {
                textView_label_savepin.setTextColor(Color.WHITE);
                cardView_savePin.setBackgroundColor(ResourcesCompat.getColor(getActivity().getResources(), R.color.colorAccent, null));

            }
        }
    }

    private void clearPinImage() {
        if (count >= 0) {
            touchPadSequence = new String[8];
            pinCode = "";
            cardView_savePin.setBackgroundColor(ResourcesCompat.getColor(getActivity().getResources(), R.color.cardview_savepin_color, null));
            count = 0;
            textView_label_savepin.setTextColor(Color.WHITE);
            linearLayout_imageList.removeAllViews();
        }


    }
}
