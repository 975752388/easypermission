package com.zky.sample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.zky.annotation.annotation.PermissionCancel
import com.zky.annotation.annotation.PermissionDenied
import com.zky.annotation.annotation.PermissionGranted
import com.zky.easypermission.core.PermissionManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv.setOnClickListener {
            PermissionManager.request(this, arrayOf(Manifest.permission.CAMERA),100)
        }
    }

    @PermissionGranted(requestCode = 100)
    fun requestSuccess(){
        Toast.makeText(this,"相机权限申请成功",Toast.LENGTH_SHORT).show()
    }
    @PermissionCancel(requestCode = 100)
    fun permissionCancel(){
        Toast.makeText(this,"相机权限被拒绝",Toast.LENGTH_SHORT).show()
    }
    @PermissionDenied(requestCode = 100)
    fun permissionDenied(){
        Toast.makeText(this,"相机权限被拒绝（勾选了不再询问）",Toast.LENGTH_SHORT).show()
    }
}
