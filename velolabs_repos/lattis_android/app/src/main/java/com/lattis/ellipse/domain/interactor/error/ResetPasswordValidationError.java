package com.lattis.ellipse.domain.interactor.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raverat on 2/23/17.
 */

public class ResetPasswordValidationError extends Throwable {

    public enum Status {
        INVALID_PASSWORD
    }

    private List<Status> status = new ArrayList<>();

    public ResetPasswordValidationError(List<Status> status) {
        this.status = status;
    }

    public List<Status> getStatus() {
        return this.status;
    }

}
