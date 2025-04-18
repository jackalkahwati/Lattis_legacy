package com.lattis.ellipse.presentation.ui.profile;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.lattis.ellipse.domain.model.TermsAndConditions;
import com.lattis.ellipse.presentation.ui.base.fragment.BaseFragment;

import javax.inject.Inject;

import butterknife.BindView;
import io.lattis.ellipse.R;

public class TermsAndConditionsFragment extends BaseFragment<TermsAndConditionsFragmentPresenter>
        implements TermsAndConditionsFragmentView {

    public final static String TAG = TermsAndConditionsFragment.class.getSimpleName();

    public interface Listener {
        void onTermsAndConditionsLoaded();
        void onTermsAndConditionsFailedLoading();
    }

    @Inject TermsAndConditionsFragmentPresenter presenter;

    @BindView(R.id.tv_description)
    TextView descriptionView;

    private Listener listener;

    @NonNull
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected void inject() {
        getComponent().inject(this);
    }

    @NonNull
    @Override
    protected TermsAndConditionsFragmentPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.fragment_termscondition;
    }

    @Override
    protected void configureViews() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onTermsAndConditionsLoaded(TermsAndConditions termsAndConditions) {
        descriptionView.setText(termsAndConditions.getContent());
        if (listener != null) {
            listener.onTermsAndConditionsLoaded();
        }
    }

    @Override
    public void onTermsAndConditionsFailedLoading() {
        if (listener != null) {
            listener.onTermsAndConditionsFailedLoading();
        }
    }

    public static TermsAndConditionsFragment newInstance() {
        return new TermsAndConditionsFragment();
    }

}
