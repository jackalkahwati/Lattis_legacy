package cc.skylock.skylock;

import android.app.Activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by AlexVijayRaj on 7/24/2015.
 */
public class CrashTheftAlert {

    Context context;
    BluetoothGattCharacteristic cChrashTheft;
    Dialog dialogCrash, dialogCrashAfter, dialogTheft;
    ImageButton ibIgnore, ibHelp;
    TextView tvTimer, tvTimeTheft, tvTimeCrash, tvTimeCrashAfter;
    Timer timer;
    int count = 30;
    private int flagCrashTheft = 0; //Crash theft flag; 0 = off; 1 = crash ON; 2 = theft ON;
    private int theftLevel = 2; // 1 = low; 2 = medium; 3 = high;
    private int thresholdCrashMAV = 150;
    private int thresholdCrashSD = 16900;
    private int thresholdTheftMAV = 70;
    private int thresholdTheftSD = 8100;

    public CrashTheftAlert(Context context1){
        context = context1;
        createCrashPopUp();
        createTheftPopUp();
        createCrashAfterPopUp();
    }

    public void flagCrashTheft(int flag, int theftLevel1){
        flagCrashTheft = flag;
        theftLevel = theftLevel1;
    }

    public void putCharacterstic(BluetoothGattCharacteristic characteristic){
        Integer mavX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
        Integer mavY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 2);
        Integer mavZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 4);
        Integer sdX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 6);
        Integer sdY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 8);
        Integer sdZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 10);

        if(flagCrashTheft == 1) {
            checkForCrash(mavX, sdX);
            checkForCrash(mavY, sdY);
            checkForCrash(mavZ, sdZ);
        }else if(flagCrashTheft == 2) {
                checkForTheft(mavX, sdX);
                checkForTheft(mavY, sdY);
                checkForTheft(mavZ, sdZ);
        }

    }

    private void checkForCrash(int mav, int sd) {
        if (mav >= thresholdTheftMAV) {
            if (sd <= thresholdTheftSD) {
                alertCrash();
            }
        }

    }

    private void checkForTheft(int mav, int sd) {
        if (mav >= thresholdTheftMAV) {
            if (sd <= thresholdTheftSD) {
                alertTheft();
            }
        }

    }

    private void alertCrash(){
        Intent intent = new Intent(context,MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification noti = new Notification.Builder(context)
                .setTicker("Skylock Crash Alert")
                .setContentTitle("Skylock Crash Alert")
                .setContentText("Ignore this or get some help")
                .setSmallIcon(R.drawable.crash_alert_inactive)
                .setContentIntent(pIntent) .getNotification();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        noti.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if(!dialogCrash.isShowing()) {
            dialogCrash.show();
            DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
            Date date = new Date();
            tvTimeCrash.setText("" + dateFormat.format(date));
            //Timer
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    timerCancel();

                }
            }, 0, 1000);
        }
        notificationManager.notify(0, noti);

    }

    private void alertTheft(){
        Intent intent = new Intent(context,MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification noti = new Notification.Builder(context)
                .setTicker("Skylock Theft Alert")
                .setContentTitle("Skylock Theft Alert")
                .setContentText("Medium Threat")
                .setSmallIcon(R.drawable.theft_alert_active)
                .setContentIntent(pIntent) .getNotification();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        noti.flags |= Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        dialogTheft.show();
        Calendar c = Calendar.getInstance();
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR);
        int am_pm = c.get(Calendar.AM_PM);
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        Date date = new Date();
        tvTimeTheft.setText("" + dateFormat.format(date));
        notificationManager.notify(0, noti);

    }

    private void createCrashPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.crash_alert_pop_up, null);
        builder.setView(view);
        dialogCrash = builder.create();
        dialogCrash.getWindow().setGravity(Gravity.TOP);
        dialogCrash.setCanceledOnTouchOutside(false);

        ibIgnore = (ImageButton) view.findViewById(R.id.bIgnore);
        ibHelp = (ImageButton) view.findViewById(R.id.bHelp);
        tvTimer = (TextView) view.findViewById(R.id.tvTimer);
        tvTimeCrash = (TextView) view.findViewById(R.id.tvTimeCrash);
        setOnClickListeners();

    }

    private void timerCancel() {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTimer.setText(""+count+"s");
                count--;
                if(count <= 9){
                    tvTimer.setTextColor(Color.RED);
                }
                if (count <= -1) {
                    timer.cancel();
                    dialogCrash.dismiss();
                    call_for_help();
                    count = 30;
                }
            }
        });
    }

    private void setOnClickListeners() {

        ibIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCrash.dismiss();
                timer.cancel();
                count = 30;
            }
        });

        ibHelp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogCrash.dismiss();
                timer.cancel();
                call_for_help();
                count = 30;
            }
        });
    }

    private void createTheftPopUp(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view  = inflater.inflate(R.layout.theft_alert_pop_up, null);
        builder.setView(view);
        dialogTheft = builder.create();
        dialogTheft.getWindow().setGravity(Gravity.TOP);
        dialogTheft.setCanceledOnTouchOutside(true);
        tvTimeTheft = (TextView) view.findViewById(R.id.tvTimeTheft);


    }

    private void createCrashAfterPopUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.crash_alert_after_pop_up, null);
        builder.setView(view);
        dialogCrashAfter = builder.create();
        dialogCrashAfter.getWindow().setGravity(Gravity.TOP);
        dialogCrashAfter.setCanceledOnTouchOutside(true);
        tvTimeCrashAfter = (TextView) view.findViewById(R.id.tvTimeCrashAfter);
    }

    private void call_for_help() {
        dialogCrashAfter.show();
        Calendar c = Calendar.getInstance();
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR);
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        Date date = new Date();
        tvTimeCrashAfter.setText("" + dateFormat.format(date));
    }
}
