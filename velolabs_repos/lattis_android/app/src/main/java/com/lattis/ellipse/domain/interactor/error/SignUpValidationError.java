package com.lattis.ellipse.domain.interactor.error;

import java.util.ArrayList;
import java.util.List;

public class SignUpValidationError extends Throwable {

    private List<Status> status = new ArrayList<>();

    public SignUpValidationError(List<Status> status) {
        this.status = status;
    }

    public List<Status> getStatus() {
        return status;
    }

    public enum Status{
        INVALID_EMAIL,
        INVALID_PHONE_NUMBER,
        INVALID_PASSWORD
    }
}
