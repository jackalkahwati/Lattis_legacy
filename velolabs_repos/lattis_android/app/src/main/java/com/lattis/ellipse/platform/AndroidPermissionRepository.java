package com.lattis.ellipse.platform;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.lattis.ellipse.domain.repository.PermissionRepository;

import javax.inject.Inject;

import io.reactivex.Observable;

public class AndroidPermissionRepository implements PermissionRepository {

    public Context context;

    @Inject
    public AndroidPermissionRepository(Context context) {
        this.context = context;
    }

    @Override
    public Observable<Permission> checkPermission(Permission permission) {
        switch (permission){
            case LOCATION:return checkSelfPermission(permission,Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return null;
    }

    @Override
    public Observable<Permission> checkPermissions(Permission... permissions) {
        return Observable.create(emitter ->  {
                for(Permission permission:permissions){
                    //TODO
                }
        });
    }

    private Observable<Permission> checkSelfPermission(Permission permission, String androidPermission){
        return Observable.create(emitter ->  {
                int result = ContextCompat.checkSelfPermission(context,androidPermission);
                if(result == PackageManager.PERMISSION_GRANTED){
                    permission.setStatus(Permission.Status.ACCEPTED);
                } else if(result == PackageManager.PERMISSION_DENIED){
                    permission.setStatus(Permission.Status.REJECTED);
                }
            emitter.onNext(permission);

        });
    }
}
