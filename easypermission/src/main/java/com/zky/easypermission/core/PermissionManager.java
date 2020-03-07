package com.zky.easypermission.core;



public class PermissionManager  {
    public static void request(Object object, String[] permissions, int requestCode){
        String name = object.getClass().getName();
        String finalName = name+"$Permission";
        try {
            Class<?> aClass = Class.forName(finalName);
            IPermissionRequest iPermissionRequest = (IPermissionRequest) aClass.newInstance();
            iPermissionRequest.requestPermission(object,permissions,requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
