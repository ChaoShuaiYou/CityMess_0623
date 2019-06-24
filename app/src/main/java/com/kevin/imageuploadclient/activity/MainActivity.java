package com.kevin.imageuploadclient.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.kevin.imageuploadclient.R;
import com.kevin.imageuploadclient.activity.basic.BaseActivity;
import com.kevin.imageuploadclient.fragment.MainFragment;
import com.kevin.imageuploadclient.fragment.basic.BaseFragment;


/**
 * 版权所有：XXX有限公司
 *
 * MainActivity
 *
 * @author zhou.wenkai ,Created on 2016-5-5 10:24:35
 * 		   Major Function：<b>主界面</b>
 *
 *         注:如果您修改了本类请填写以下内容作为记录，如非本人操作劳烦通知，谢谢！！！
 * @author mender，Modified Date Modify Content:
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void initContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initViews() {
        initMainFragment();
    }

    /**
     * 初始化内容Fragment
     *
     * @return void
     */
    public void initMainFragment() {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Toast.makeText(MainActivity.this,"已开启定位权限",Toast.LENGTH_LONG).show();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BaseFragment mFragment = MainFragment.newInstance();
            transaction.replace(R.id.main_act_container, mFragment, mFragment.getFragmentName());
            transaction.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    BaseFragment mFragment = MainFragment.newInstance();
                    transaction.replace(R.id.main_act_container, mFragment, mFragment.getFragmentName());
                    transaction.commit();
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(MainActivity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }


    @Override
    protected void initEvents() {

    }
}
