package cc.skylock.skylock.cardswipe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import cc.skylock.skylock.ObjectRepo;
import cc.skylock.skylock.R;
import cc.skylock.skylock.util.SkylockConstand;

/**
 * Created by prabhu on 1/18/16.
 */

public class FeedItemView extends RelativeLayout implements CardStackView.CardStackListener {

    int mAddLockStepType=1;
    RelativeLayout mAddLockStepOne,mAddLockStepTwo,mAddLockStepThree,mAddLockStepFour,mAddLockStepFive;
    CardStackView cardStackView;
    Timer bluetoothTimer;
    int bluetoothProgressCount = 1;
    Context context;
    ImageView ivPaginationAddLock, ivBluetoothProgress;

    public FeedItemView(final Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.add_lock_bg, this, true);
        mAddLockStepOne = (RelativeLayout) findViewById(R.id.addlock_step_one);
        mAddLockStepTwo = (RelativeLayout) findViewById(R.id.addlock_step_two);
        mAddLockStepThree = (RelativeLayout) findViewById(R.id.addlock_step_three);
        mAddLockStepFour = (RelativeLayout) findViewById(R.id.addlock_step_four);
        mAddLockStepFive = (RelativeLayout) findViewById(R.id.addlock_step_five);
        ImageButton next = (ImageButton) findViewById(R.id.ibNextStep1);
        ivBluetoothProgress = (ImageView) findViewById(R.id.ivBluetoothProgress);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackView.leftSide();

            }
        });
        ImageButton pervStepTwo = (ImageButton) findViewById(R.id.ibPreviousStep2);
        pervStepTwo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLockBackOperationIntent = new Intent( SkylockConstand.addLockBackOption);
                addLockBackOperationIntent.putExtra("step",1);
                context.sendBroadcast(addLockBackOperationIntent);
            }
        });
        ImageButton pervStepThree = (ImageButton) findViewById(R.id.ibPreviousStep3);
        pervStepThree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addLockBackOperationIntent = new Intent( SkylockConstand.addLockBackOption);
                addLockBackOperationIntent.putExtra("step",2);
                context.sendBroadcast(addLockBackOperationIntent);
            }
        });

        ImageButton nextStepTwo = (ImageButton) findViewById(R.id.ibNextStep2);
        nextStepTwo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                cardStackView.leftSide();
            }
        });
        ImageButton noStepFour = (ImageButton) findViewById(R.id.ibNo);
        noStepFour.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent addLockBackOperationIntent = new Intent( SkylockConstand.addLockBackOption);
                addLockBackOperationIntent.putExtra("step", 3);
                context.sendBroadcast(addLockBackOperationIntent);

            }
        });

        ImageButton ibFinished = (ImageButton) findViewById(R.id.ibFinished);
        ibFinished.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) context).finish();
            }
        });

        ImageButton ibNextStep2 = (ImageButton) findViewById(R.id.ibNextStep2);
        ibNextStep2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackView.leftSide();

            }
        });

        ImageButton ibNextStep3 = (ImageButton) findViewById(R.id.ibNextStep3);
        ibNextStep3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackView.leftSide();

            }
        });
        ImageButton ibYes = (ImageButton) findViewById(R.id.ibYes);
        ibYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStackView.leftSide();

            }
        });
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    public void bind(Integer item,CardStackView cardStackView) {
        mAddLockStepType = item;
        this.cardStackView = cardStackView;
        if(item ==1){
            mAddLockStepOne.setVisibility(View.VISIBLE);
            mAddLockStepTwo.setVisibility(View.GONE);
            mAddLockStepThree.setVisibility(View.GONE);
            mAddLockStepFour.setVisibility(View.GONE);
            mAddLockStepFive.setVisibility(View.GONE);

            mAddLockStepFive.setTag("");
        }else  if(item ==2) {

            mAddLockStepOne.setVisibility(View.GONE);
            mAddLockStepTwo.setVisibility(View.VISIBLE);
            mAddLockStepThree.setVisibility(View.GONE);
            mAddLockStepFour.setVisibility(View.GONE);
            mAddLockStepFive.setVisibility(View.GONE);
            mAddLockStepFive.setTag("");
        }else  if(item ==3) {

            mAddLockStepOne.setVisibility(View.GONE);
            mAddLockStepTwo.setVisibility(View.GONE);
            mAddLockStepThree.setVisibility(View.VISIBLE);
            mAddLockStepFour.setVisibility(View.GONE);
            mAddLockStepFive.setVisibility(View.GONE);
            bluetoothTimer = new Timer();
            bluetoothTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startBluetoothImageSwap();
                }
            }, 0, 300);

            mAddLockStepFive.setTag("");
        }else  if(item ==4) {

            mAddLockStepOne.setVisibility(View.GONE);
            mAddLockStepTwo.setVisibility(View.GONE);
            mAddLockStepThree.setVisibility(View.GONE);
            mAddLockStepFour.setVisibility(View.VISIBLE);
            mAddLockStepFive.setVisibility(View.GONE);

            mAddLockStepFive.setTag("");
        }else  if(item ==5) {

            mAddLockStepOne.setVisibility(View.GONE);
            mAddLockStepTwo.setVisibility(View.GONE);
            mAddLockStepThree.setVisibility(View.GONE);
            mAddLockStepFour.setVisibility(View.GONE);
            mAddLockStepFive.setVisibility(View.VISIBLE);
            mAddLockStepFive.setTag("last");
        }
        return;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

//        if (mFeedItem != null) {
//            int resource = getResources().getIdentifier(
//                    "content_card_x_0" + mFeedItem.getId(),
//                    "drawable", getContext().getPackageName());
//
//            loadPicture(resource);
//
//            id.setText(mFeedItem.toString());
//        }
    }

//    public FeedItem getFeedItem() {
//        return mFeedItem;
//    }

    void loadPicture(int id) {
        Drawable drawable = getResources().getDrawable(id);

        setPicture(drawable);
    }

    void setPicture(Drawable drawable) {
//        picture.setImageDrawable(drawable);
    }

    @Override
    public void onUpdateProgress(boolean positif, float percent, View view) {
//        if (positif) {
//            no.setAlpha(percent);
//        } else {
//            ok.setAlpha(percent);
//        }
    }

    @Override
    public void onCancelled(View beingDragged) {
//        ok.setAlpha(0);
//        no.setAlpha(0);
    }

    @Override
    public void onChoiceMade(boolean choice, View beingDragged) {
//        ok.setAlpha(0);
//        no.setAlpha(0);
    }


    private void startBluetoothImageSwap() {
        if(bluetoothProgressCount >= 5){
            bluetoothProgressCount = 1;
        }else{
            bluetoothProgressCount++;
        }

        ((Activity)context).runOnUiThread(new Runnable() {
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