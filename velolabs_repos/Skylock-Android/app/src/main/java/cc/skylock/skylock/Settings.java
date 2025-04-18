package cc.skylock.skylock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.Arrays;

/**
 * Created by AlexVijayRaj on 8/5/2015.
 */
public class Settings {

    Context context;
    Dialog dialog;
    ImageButton ibBack, ibResetPin, ibSavePin, ibCap, ibAutoLock, ibLowBandwidth, ibCapPin1, ibCapPin2, ibCapPin3, ibCapPin4, ibLow, ibMed, ibHigh;
    TextView tvCapDisplay;
    ObjectRepo objRepo;
    ViewSwitcher vsEmergency;
    View view;
    int count = 0;
    int[] capSequence1, capSequence2 ;

    public Settings(Context context,ObjectRepo objRepo1){
        this.context = context;
        objRepo = objRepo1;


        init();
        setOnClickListeners();
    }

    public void showSettings(){
        dialog.show();
    }

    private void init() {
        dialog=new Dialog(context,android.R.style.Theme_Holo_Light_NoActionBar);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogZoom;
        dialog.setContentView(R.layout.settings_dialog);
        ibBack = (ImageButton) dialog.findViewById(R.id.ibBack);
        ibResetPin = (ImageButton) dialog.findViewById(R.id.ibResetPin);
        ibSavePin = (ImageButton) dialog.findViewById(R.id.ibSavePin);
        ibCap = (ImageButton) dialog.findViewById(R.id.ibCap);
        ibCapPin1 = (ImageButton) dialog.findViewById(R.id.ibCapPin1);
        ibCapPin2 = (ImageButton) dialog.findViewById(R.id.ibCapPin2);
        ibCapPin3 = (ImageButton) dialog.findViewById(R.id.ibCapPin3);
        ibCapPin4 = (ImageButton) dialog.findViewById(R.id.ibCapPin4);
        ibAutoLock = (ImageButton) dialog.findViewById(R.id.ibAutoLock);
        ibLowBandwidth = (ImageButton) dialog.findViewById(R.id.ibLowBandwidth);
        ibLow = (ImageButton) dialog.findViewById(R.id.ibTheftLow);
        ibMed = (ImageButton) dialog.findViewById(R.id.ibTheftMed);
        ibHigh = (ImageButton) dialog.findViewById(R.id.ibTheftHigh);
        vsEmergency = (ViewSwitcher) dialog.findViewById(R.id.vsEmergency);
        tvCapDisplay = (TextView) dialog.findViewById(R.id.tvCapDisplay);

        //initialize cap sequence to all 0's
        capSequence1 = new int[16];
        capSequence2 = new int[16];
        resetCapSequence();


    }

    //resets both the sequences(the first one and the confirmation one)
    private void resetCapSequence() {
        for(int i =0; i<capSequence1.length; i++){
            capSequence1[i] = 0;
            capSequence2[i] = 0;
        }
        count = 0;
    }

    //resets the first one
    private void resetCapSequence1() {
        for(int i =0; i<capSequence1.length; i++){
            capSequence1[i] = 0;

        }
        count = 0;
    }

    private void setOnClickListeners() {
        //back button - closes the dialog
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ibLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibLow.getTag().toString().equals("inactive")){
                    if(ibMed.getTag().toString().equals("active")){
                        ibMed.setImageResource(R.drawable.med_light);
                        ibMed.setTag("inactive");
                    }else if(ibHigh.getTag().toString().equals("active")){
                        ibHigh.setImageResource(R.drawable.high_light);
                        ibHigh.setTag("inactive");
                    }
                    ibLow.setImageResource(R.drawable.low_dark);
                    ibLow.setTag("active");
                }
            }
        });

        ibMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibMed.getTag().toString().equals("inactive")){
                    if(ibLow.getTag().toString().equals("active")){
                        ibLow.setImageResource(R.drawable.low_light);
                        ibLow.setTag("inactive");
                    }else if(ibHigh.getTag().toString().equals("active")){
                        ibHigh.setImageResource(R.drawable.high_light);
                        ibHigh.setTag("inactive");
                    }
                    ibMed.setImageResource(R.drawable.med_dark);
                    ibMed.setTag("active");
                }
            }
        });

        ibHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibHigh.getTag().toString().equals("inactive")){
                    if(ibLow.getTag().toString().equals("active")){
                        ibLow.setImageResource(R.drawable.low_light);
                        ibLow.setTag("inactive");
                    }else if(ibMed.getTag().toString().equals("active")){
                        ibMed.setImageResource(R.drawable.med_light);
                        ibMed.setTag("inactive");
                    }
                    ibHigh.setImageResource(R.drawable.high_dark);
                    ibHigh.setTag("active");
                }
            }
        });

        //Cap buttons - registers Cap pin clicks - checks if it is 4 to 16 and throws an error
        ibCapPin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != 16){
                    capSequence1[count] = 1;
                    count++;
                }else{
                    Toast.makeText(context, "Max number of presses reached",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        ibCapPin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != 16){
                    capSequence1[count] = 8;
                    count++;
                }else{
                    Toast.makeText(context, "Max number of presses reached",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        ibCapPin3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != 16){
                    capSequence1[count] = 2;
                    count++;
                }else{
                    Toast.makeText(context, "Max number of presses reached",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        ibCapPin4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != 16){
                    capSequence1[count] = 4;
                    count++;
                }else{
                    Toast.makeText(context, "Max number of presses reached",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //reset Pin - starts the cap pin registration sequence - checks if a bluetooth device is connected and throws an error
        ibResetPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(objRepo.objBluetoothClass.mBluetoothGatt != null) {
                    vsEmergency.showNext();
                    resetCapSequence();
                }else{
                    Toast.makeText(context, "No device Connected",
                            Toast.LENGTH_LONG).show();
                }

            }
        });


        ibSavePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibSavePin.getTag().toString().equals("mode_1")){
                    if(count>3){                                                                    //checks if the count is atleast 4
                        capSequence2 = capSequence1.clone();
                        ibSavePin.setTag("mode_2");
                        tvCapDisplay.setText("Please Re - enter the Sequence to confirm");          //registers the first sequence and moves on to the next confirmation sequence
                        resetCapSequence1();
                    }else{
                        vsEmergency.showPrevious();
                        tvCapDisplay.setText("Enter a new sequence with 4-16* touches.");
                        resetCapSequence();
                        Toast.makeText(context, "Enter atleast 4 touch Sequence \n Capacitive Pin not reset",
                                Toast.LENGTH_LONG).show();
                    }


                }else if(ibSavePin.getTag().toString().equals("mode_2")){
                    ibSavePin.setTag("mode_1");

                    if(Arrays.equals(capSequence1,capSequence2) ) {                                 //compares if the first sequence matches with the second one and throws an error
                        new AlertDialog.Builder(context)                                            //if sequences matches a yes or no alert dialog box is created for final confirmation
                                .setTitle("Change Capactive Pin")
                                .setMessage("Are you sure you want to Change the Capacitive Pin? ")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (objRepo.objBluetoothClass.mBluetoothGatt != null) {     //checks for valid bluetooth connection
                                            objRepo.objBluetoothClass.resetCapPin(capSequence1);
                                            Toast.makeText(context, "Capacitive Pin Successfully changed",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(context, "No device connected",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        vsEmergency.showPrevious();                                 //reset back to previous mode
                                        tvCapDisplay.setText("Enter a new sequence with 4-16* touches.");
                                        resetCapSequence();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(context, "Capacitive Pin not reset",
                                                Toast.LENGTH_LONG).show();
                                        vsEmergency.showPrevious();                                 //reset back to previous mode
                                        tvCapDisplay.setText("Enter a new sequence with 4-16* touches.");
                                        resetCapSequence();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setCancelable(false)
                                .show();
                    }else{
                        Toast.makeText(context, "Sequence do not match",
                                Toast.LENGTH_LONG).show();
                        vsEmergency.showPrevious();
                        tvCapDisplay.setText("Enter a new sequence with 4-16* touches.");
                        resetCapSequence();
                    }


                }
            }
        });

        ibCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibCap.getTag().toString().equals("inactive")){
                    ibCap.setTag("active");
                    ibCap.setImageResource(R.drawable.toggle_on);
                }else if(ibCap.getTag().toString().equals("active")){
                    ibCap.setTag("inactive");
                    ibCap.setImageResource(R.drawable.toggle_off);
                }

            }
        });

        ibAutoLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ibAutoLock.getTag().toString().equals("inactive")){
                    ibAutoLock.setTag("active");
                    objRepo.objBluetoothClass.AUTO_LOCK = 1;
                    ibAutoLock.setImageResource(R.drawable.toggle_on);
                }else if(ibAutoLock.getTag().toString().equals("active")){
                    ibAutoLock.setTag("inactive");
                    objRepo.objBluetoothClass.AUTO_LOCK = 0;
                    ibAutoLock.setImageResource(R.drawable.toggle_off);
                }

            }
        });

        ibLowBandwidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ibLowBandwidth.getTag().toString().equals("inactive")) {
                    ibLowBandwidth.setTag("active");
                    ibLowBandwidth.setImageResource(R.drawable.toggle_on);
                } else if (ibLowBandwidth.getTag().toString().equals("active")) {
                    ibLowBandwidth.setTag("inactive");
                    ibLowBandwidth.setImageResource(R.drawable.toggle_off);
                }
            }
        });
    }
}
