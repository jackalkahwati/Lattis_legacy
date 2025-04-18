package com.lattis.ellipse.domain.repository;

import io.reactivex.Observable;

public interface PermissionRepository {

    Observable<Permission> checkPermission(Permission permission);

    Observable<Permission> checkPermissions(Permission... permission);

    enum Permission{

        LOCATION;

        private Status status;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public enum Status {
            ACCEPTED,REJECTED;
        }
    }
}
