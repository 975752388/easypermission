package com.zky.easypermission.core;

public interface IPermissionRequest {
    void requestPermission(Object object, String[] permissions, int requestCode);
}
