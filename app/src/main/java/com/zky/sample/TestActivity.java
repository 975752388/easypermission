package com.zky.sample;

import androidx.appcompat.app.AppCompatActivity;

import com.zky.annotation.annotation.PermissionGranted;

public class TestActivity extends AppCompatActivity {


    @PermissionGranted(requestCode = 100)
    public void test(){

    }
}
