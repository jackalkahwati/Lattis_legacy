package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import cc.skylock.skylock.R;

/**
 * Created by prabhu on 1/18/16.
 */
public class LockStepOneRela extends RelativeLayout {
    public LockStepOneRela(Context context){
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_lock_1, null);
    }
}
