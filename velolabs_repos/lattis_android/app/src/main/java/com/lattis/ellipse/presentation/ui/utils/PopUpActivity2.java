package com.lattis.ellipse.presentation.ui.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;

import com.lattis.ellipse.presentation.ui.base.activity.BaseCloseActivity;
import com.lattis.ellipse.presentation.view.CustomButton;
import com.lattis.ellipse.presentation.view.CustomTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;

public class PopUpActivity2 extends BaseCloseActivity<PopUpActivity2Presenter> implements PopUpActivity2View {

    public final static String TITLE_POP_UP = "TITLE_POP_UP";
    public final static String SUBTITLE1_POP_UP = "SUBTITLE1_POP_UP";
    public final static String SUBTITLE2_POP_UP = "SUBTITLE2_POP_UP";
    public final static String ACTIONBTN_POP_UP = "ACTIONBTN_POP_UP";


    public static void launchForResult(Activity activity, int requestCode, String title, String subTitle1, String subTitle2, String actionBtn) {
        Intent intent = new Intent(activity, PopUpActivity2.class);
        intent.putExtra(TITLE_POP_UP,title);
        intent.putExtra(SUBTITLE1_POP_UP,subTitle1);
        intent.putExtra(SUBTITLE2_POP_UP,subTitle2);
        intent.putExtra(ACTIONBTN_POP_UP,actionBtn);
        activity.startActivityForResult(intent,requestCode);
    }


    @BindView(R.id.cv_title_pop_up)
    CustomTextView cv_title_pop_up;

    @BindView(R.id.cv_subtitle1_pop_up)
    CustomTextView cv_subtitle1_pop_up;

    @BindView(R.id.cv_subtitle2_pop_up)
    CustomTextView cv_subtitle2_pop_up;

    @BindView(R.id.cv_action_btn_pop_up)
    CustomButton cv_action_btn_pop_up;


    @OnClick(R.id.iv_close_pop_up)
    public void actionCancel(){
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.cv_action_btn_pop_up)
    public void actionOK(){
        setResult(RESULT_OK);
        finish();
    }


    @Inject
    PopUpActivity2Presenter presenter;

    @NonNull
    @Override
    protected PopUpActivity2Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_pop_up2;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @Override
    public void setView(String title, String subTitle1, String subTitle2, String actionBtn) {
        if(title!=null){
            cv_title_pop_up.setText(title);
        }else{
            cv_title_pop_up.setVisibility(View.INVISIBLE);
        }

        if(subTitle1!=null){
            cv_subtitle1_pop_up.setText(subTitle1);
        }else{
            cv_subtitle1_pop_up.setVisibility(View.INVISIBLE);
        }

        if(subTitle2!=null && !subTitle2.equals("")){
            cv_subtitle2_pop_up.setText(subTitle2);
        }else{
            cv_subtitle2_pop_up.setVisibility(View.GONE);
        }

        if(actionBtn!=null){
            cv_action_btn_pop_up.setText(actionBtn);
        }else{
            cv_action_btn_pop_up.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }
}
