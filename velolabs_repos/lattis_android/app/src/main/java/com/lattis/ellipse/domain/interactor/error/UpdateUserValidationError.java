package com.lattis.ellipse.domain.interactor.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raverat on 2/20/17.
 */

public class UpdateUserValidationError extends Throwable {

    public enum Status {
        INVALID_FIRST_NAME,
        INVALID_LAST_NAME,
        INVALID_PHONE_NUMBER,
        INVALID_EMAIL
    }

    private List<Status> status = new ArrayList<>();

    public UpdateUserValidationError(List<Status> status) {
        this.status = status;
    }

    public List<Status> getStatus() {
        return status;
    }

}
