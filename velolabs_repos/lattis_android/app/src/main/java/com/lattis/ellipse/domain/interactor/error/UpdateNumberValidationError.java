package com.lattis.ellipse.domain.interactor.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raverat on 2/20/17.
 */

public class UpdateNumberValidationError extends Throwable {

    private List<Status> status = new ArrayList<>();

    public UpdateNumberValidationError(List<Status> status) {
        this.status = status;
    }

    public List<Status> getStatus() {
        return status;
    }

    public enum Status {
        INVALID_PHONE_NUMBER
    }

}
