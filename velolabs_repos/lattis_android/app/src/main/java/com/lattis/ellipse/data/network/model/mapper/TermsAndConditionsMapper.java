package com.lattis.ellipse.data.network.model.mapper;

import androidx.annotation.NonNull;

import com.lattis.ellipse.data.network.base.AbstractDataMapper;
import com.lattis.ellipse.data.network.model.response.GetTermsAndConditionsResponse;
import com.lattis.ellipse.domain.model.TermsAndConditions;

import javax.inject.Inject;

public class TermsAndConditionsMapper extends AbstractDataMapper<GetTermsAndConditionsResponse, TermsAndConditions> {

    @Inject
    public TermsAndConditionsMapper() {}

    @NonNull
    @Override
    public TermsAndConditions mapIn(@NonNull GetTermsAndConditionsResponse in) {
        TermsAndConditions out = new TermsAndConditions();
        out.setVersion(in.getVersion());
        out.setContent(in.getTerms());
        return out;
    }

    @NonNull
    @Override
    public GetTermsAndConditionsResponse mapOut(@NonNull TermsAndConditions termsAndConditions) {
        return null;
    }

}
