package com.zky.easypermission.core


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zky.easypermission.R
import com.zky.easypermission.core.util.PermissionUtils


class PermissionActivity : AppCompatActivity() {
    companion object{
        private const val PERMISSON_KEY="permissons"
        private const val REQUEST_CODE="request_code"
        internal const val REQUEST_CODE_DEFAULT=-1
        private lateinit var listener:IPermisson

        fun start(context:Context,permissions: Array<out String>,
                  requestCode:Int,iPermission: IPermisson
        ){
            val bundle = Bundle()
            bundle.putInt(REQUEST_CODE,requestCode)
            bundle.putStringArray(PERMISSON_KEY,permissions)
            listener=iPermission
            val intent=Intent(context,PermissionActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        permisson()
    }
    private fun permisson(){
        var permissons= intent.getStringArrayExtra(PERMISSON_KEY)
        val requestCode = intent.getIntExtra(REQUEST_CODE, REQUEST_CODE_DEFAULT)
        if (permissons.isNullOrEmpty()||requestCode<0){
            finish()
            return
        }
        val b = PermissionUtils.hasPermissionRequest(this,permissons)
        if (b){
            listener.granted()
            finish()
            return
        }
        ActivityCompat.requestPermissions(this, permissons, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.requestPermissionSuccess(grantResults)) {
            listener.granted(); // 已经授权成功了

            this.finish();
            return;
        }

        // 没有成功，可能是用户 不听话
        // 如果用户点击了，拒绝（勾选了”不再提醒“） 等操作
        if (!PermissionUtils.shouldShowRequestPermissionRationale(this, permissions)) {
            // 用户拒绝，不再提醒
            listener.denied();

            this.finish();
            return;
        }

        listener.cancel()
        this.finish();
        return;
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0,0)
    }
}
