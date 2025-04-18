package com.lattis.ellipse.presentation.ui.damagebike;

import android.content.Intent;
import androidx.annotation.NonNull;
import android.widget.EditText;

import com.lattis.ellipse.presentation.ui.base.activity.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.lattis.ellipse.R;


public class DescriptionActivity extends BaseActivity<DescriptionPresenter> implements DescriptionView {
    @BindView(R.id.et_damage_description)
    EditText editText_description;
    public final String DESCRIPTION_TAG = "DESCRIPTION";

    @Inject
    DescriptionPresenter descriptionPresenter;

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected DescriptionPresenter getPresenter() {
        return descriptionPresenter;
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.activity_description;
    }


    @OnClick(R.id.save_btn)
    public void saveButtonClicked() {
        final String description = editText_description.getText().toString();
        if (description != null && !description.isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra(DESCRIPTION_TAG, description);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void configureViews() {
        super.configureViews();
        setToolbarHeader(getString(R.string.title_activity_description));
    }

    @Override
    public void setDescriptionInfo(String info) {
        editText_description.setText(info);
    }

    @Override
    protected void onInternetConnectionChanged(boolean isConnected) {

    }

}
