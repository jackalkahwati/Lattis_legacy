package cc.skylock.skylock.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cc.skylock.skylock.Bean.SuccessResponse;
import cc.skylock.skylock.Bean.TermsAndConditionResponse;
import cc.skylock.skylock.Bean.UserRegistrationResponse;
import cc.skylock.skylock.R;
import cc.skylock.skylock.operation.UserApiService;
import cc.skylock.skylock.retofit.RetofitRestAdapter;
import cc.skylock.skylock.utils.Network.NetworkUtil;
import cc.skylock.skylock.utils.PrefUtil;
import cc.skylock.skylock.utils.SkylockConstant;
import cc.skylock.skylock.utils.UtilHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Velo Labs Android on 03-02-2017.
 */

public class TermsAndConditionFragment extends Fragment {
    public static TermsAndConditionFragment termsAndConditionFragment;
    private TextView mTextView_description;
    private RelativeLayout mRelativeLayout_progress;
    private Context mContext;
    private PrefUtil mPrefUtil;
    private String description = null;


    public static TermsAndConditionFragment newInstance() {
        if (termsAndConditionFragment == null) {
            termsAndConditionFragment = new TermsAndConditionFragment();
        }
        return termsAndConditionFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_termscondition, null);
        mContext = getActivity();
        mPrefUtil = new PrefUtil(mContext);
        mTextView_description = (TextView) view.findViewById(R.id.tv_description);
        mRelativeLayout_progress = (RelativeLayout) view.findViewById(R.id.progressBar_relativeLayout);
        mRelativeLayout_progress.setVisibility(View.VISIBLE);
        mTextView_description.setTypeface(UtilHelper.getTypface(mContext));
        mTextView_description.setMovementMethod(new ScrollingMovementMethod());
        if (NetworkUtil.isNetworkAvailable(mContext)) {
            getTermAndConditionsFromServer();
        } else {
            mRelativeLayout_progress.setVisibility(View.GONE);
            description = mPrefUtil.getStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
            mTextView_description.setText(description);

        }
        return view;
    }

    private void getTermAndConditionsFromServer() {
        UserApiService UserApiService = RetofitRestAdapter.getClient(SkylockConstant.BASE_URL).create(UserApiService.class);
        Call<TermsAndConditionResponse> mUpdateAccount = UserApiService.TermsAndCondition();

        mUpdateAccount.enqueue(new Callback<TermsAndConditionResponse>() {
            @Override
            public void onResponse(Call<TermsAndConditionResponse> call, Response<TermsAndConditionResponse> response) {
                mRelativeLayout_progress.setVisibility(View.GONE);
                if (response.code() == 200) {
                    description = response.body().getPayload().getTerms();
                    mTextView_description.setText(description);
                    mPrefUtil.setStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
                }
            }

            @Override
            public void onFailure(Call<TermsAndConditionResponse> call, Throwable t) {
                mRelativeLayout_progress.setVisibility(View.GONE);
                description = mPrefUtil.getStringPref(SkylockConstant.PREF_KEY__TERMS_AND_CONDITION, description);
                mTextView_description.setText(description);

            }
        });

    }


}
