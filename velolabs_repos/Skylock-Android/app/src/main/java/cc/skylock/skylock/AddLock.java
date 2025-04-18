package cc.skylock.skylock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by AlexVijayRaj on 8/12/2015.
 */
public class AddLock extends Activity {

    Context context;
    Dialog dAddLockBg, dAddLock1, dAddLock2, dAddLock3, dAddLock4, dAddLock5;
    ImageButton ibSearchDone, ibNextStep1, ibPreviousStep2, ibNextStep2, ibPreviousStep3, ibNextStep3, ibYes, ibNo, ibFinished;
    ImageView ivPaginationAddLock, ivBluetoothProgress;
    RelativeLayout rlPicture;
    TextView tvTextView1, tvTextView2, tvOrder;
    Timer bluetoothTimer;
    ObjectRepo objRepo;
    Dialog dialog;

    int bluetoothProgressCount = 1;

    public AddLock(Context context1, ObjectRepo objRepo1){
        context = context1;
        objRepo = objRepo1;

//        dAddLockBg=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
//        dAddLockBg.setContentView(R.layout.add_lock_bg);

        dAddLock1=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dAddLock1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock1.setContentView(R.layout.add_lock_1);

        dAddLock2=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock2.setContentView(R.layout.add_lock_2);
        dAddLock2.setCancelable(false);

        dAddLock3=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock3.setContentView(R.layout.add_lock_3);
        dAddLock3.setCancelable(false);

        dAddLock4=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dAddLock4.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock4.setContentView(R.layout.add_lock_4);
        dAddLock4.setCancelable(false);

        dAddLock5=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dAddLock5.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock5.setContentView(R.layout.add_lock_5);
        dAddLock5.setCancelable(false);


        ibNextStep1 = (ImageButton) dAddLock1.findViewById(R.id.ibNextStep1);
        tvOrder = (TextView) dAddLock1.findViewById(R.id.tvOrder);
        ibPreviousStep2 = (ImageButton) dAddLock2.findViewById(R.id.ibPreviousStep2);
        ibNextStep2 = (ImageButton) dAddLock2.findViewById(R.id.ibNextStep2);
        ibPreviousStep3 = (ImageButton) dAddLock3.findViewById(R.id.ibPreviousStep3);
        ibNextStep3 = (ImageButton) dAddLock3.findViewById(R.id.ibNextStep3);
        ivBluetoothProgress = (ImageView) dAddLock3.findViewById(R.id.ivBluetoothProgress);
        ibYes = (ImageButton) dAddLock4.findViewById(R.id.ibYes);
        ibNo = (ImageButton) dAddLock4.findViewById(R.id.ibNo);
        ibFinished = (ImageButton) dAddLock5.findViewById(R.id.ibFinished);

        setOnClickListeners();

    }

    public void showAddLock(){

        dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock4.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
        dAddLock5.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;

        dAddLockBg.show();
        dAddLock1.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.bluetooth_list, null);
        objRepo.objBluetoothClass.scanBluetooth = (Button) view.findViewById(R.id.scanBluetooth);
        objRepo.objBluetoothClass.listView = (ListView) view.findViewById(R.id.bluetoothList);
        builder.setView(view);
        objRepo.objBluetoothClass.temp_add_lock();
        dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCanceledOnTouchOutside(true);
        //dialog.show();


    }

    private void setOnClickListeners() {
        ibNextStep1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
                dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;

                dAddLock1.dismiss();
                dAddLock2.show();

                objRepo.objBluetoothClass.checkBluetoothState();

            }
        });

        ibPreviousStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;
                dAddLock1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;

                dAddLock1.show();
                dAddLock2.dismiss();
            }
        });

        ibNextStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
                dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;

                dAddLock3.show();
                dAddLock2.dismiss();

                //objRepo.objBluetoothClass.addLockStartSearch();

                bluetoothTimer = new Timer();
                bluetoothTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startBluetoothImageSwap();
                    }
                }, 0, 300);
            }
        });

        ibPreviousStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;
                dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;

                dAddLock2.show();
                dAddLock3.dismiss();

                bluetoothTimer.cancel();
                bluetoothProgressCount = 1;
            }
        });

        ibNextStep3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
                dAddLock4.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;

                dAddLock4.show();
                dAddLock3.dismiss();

                //bluetoothTimer.cancel();
                //bluetoothProgressCount = 1;

                //objRepo.objBluetoothClass.startLEDBlink();
                //objRepo.objBluetoothClass.enableVerify();
                //objRepo.objBluetoothClass.getKeys();


            }
        });

        ibNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock4.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;
                dAddLock3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationRight;

                dAddLock3.show();
                dAddLock4.dismiss();

                bluetoothTimer = new Timer();
                bluetoothTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startBluetoothImageSwap();
                    }
                }, 0, 300);
            }
        });

        ibYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock5.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;
                dAddLock4.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeft;

                dAddLock5.show();
                dAddLock4.dismiss();

                try{
                    //objRepo.objBluetoothClass.ledBlinkTimer.cancel();
                    //objRepo.objBluetoothClass.LED_OFF();
                }catch(Exception ignore){}

                /*objRepo.objBluetoothClass.LED_OFF();

                final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                objRepo.objBluetoothClass.writePUBKey();
            }
        }, 500);*/
            }
        });

        ibFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAddLock5.dismiss();
                dAddLockBg.dismiss();
            }
        });





        tvOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.skylock.cc/"));
                context.startActivity(browserIntent);
            }
        });



    }

    private void startBluetoothImageSwap() {
        if(bluetoothProgressCount >= 5){
            bluetoothProgressCount = 1;
        }else{
            bluetoothProgressCount++;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(bluetoothProgressCount){
                    case 1:
                        ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_1));
                        break;
                    case 2:
                        ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_2));
                        break;
                    case 3:
                        ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_3));
                        break;
                    case 4:
                        ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_4));
                        break;
                    default:
                        bluetoothProgressCount = 1;
                        ivBluetoothProgress.setImageDrawable(context.getResources().getDrawable(R.drawable.bluetooth_progress_1));
                        break;
                }

            }
        });
    }

}
