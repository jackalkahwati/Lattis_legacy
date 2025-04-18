package cc.skylock.skylock.ui.alert;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import cc.skylock.skylock.R;
import cc.skylock.skylock.utils.UtilHelper;

/**
 * Created by Velo Labs Android on 27-12-2016.
 */

public class CentralizedAlertDialog {

    public static void showDialog(final Context context, String title, String message, int type) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_centralized_layout);
        dialog.setCancelable(false);
        final TextView textView_label_title = (TextView) dialog.findViewById(R.id.tv_title);
        final TextView textView_label_description = (TextView) dialog.findViewById(R.id.tv_description);
        final TextView textView_label_ok = (TextView) dialog.findViewById(R.id.tv_no_button);
        final CardView cv_cancel = (CardView) dialog.findViewById(R.id.cv_cancel_button);
        textView_label_ok.setTextColor(Color.WHITE);
        cv_cancel.setBackgroundColor(Color.parseColor("#57D8FF"));
        if (type == 0) {
            textView_label_ok.setText("OK");

        } else {
            textView_label_ok.setText("Try again");
        }
        textView_label_title.setTypeface(UtilHelper.getTypface(context));
        textView_label_ok.setTypeface(UtilHelper.getTypface(context));
        textView_label_description.setTypeface(UtilHelper.getTypface(context));
        textView_label_title.setText(title);
        textView_label_description.setText(message);
        cv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        cv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }




}
