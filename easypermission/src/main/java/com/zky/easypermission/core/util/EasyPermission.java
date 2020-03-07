package com.zky.easypermission.core.util;

import android.util.Log;

import com.zky.easypermission.core.IPermissionRequest;

public class EasyPermission {
    public static void request(Object object,String[] permissions,int requestCode){
        Log.e("===","request前");
        String className = object.getClass().getName();
        String finalClassName = className+"$Permission";
        try {
            Class<?> aClass = Class.forName(finalClassName);
            Object context = aClass.newInstance();
            if (context instanceof IPermissionRequest){
                Log.e("===","request");
                IPermissionRequest iPermissionRequest = (IPermissionRequest) context;
                iPermissionRequest.requestPermission(object,permissions,requestCode);
            }
        } catch (Exception e) {
            Log.e("===",e.toString());
            e.printStackTrace();
        }
    }
}
