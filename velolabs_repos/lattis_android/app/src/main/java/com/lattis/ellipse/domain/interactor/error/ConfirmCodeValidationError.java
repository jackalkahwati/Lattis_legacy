package com.lattis.ellipse.domain.interactor.error;

import java.util.ArrayList;
import java.util.List;

public class ConfirmCodeValidationError extends Throwable {

    private List<Status> status = new ArrayList<>();

    public ConfirmCodeValidationError(List<Status> status) {
        this.status = status;
    }

    public List<Status> getStatus() {
        return status;
    }

    public enum Status {
        INVALID_CONFIRMATION_CODE
    }

}
