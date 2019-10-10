package com.android.frame.activity;

import android.Manifest;
import android.os.Bundle;

import com.android.frame.R;

import com.android.frame.view.SplashView;
import com.umeng.analytics.MobclickAgent;

import org.rdengine.runtime.RT;
import org.rdengine.util.UiUtil;
import org.rdengine.view.manager.BaseActivity;
import org.rdengine.view.manager.ViewParam;

import java.util.ArrayList;
import java.util.List;

/**
 * 闪屏页面
 * 
 * @author CCCMAX
 */
public class SplashActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // if (getIntent() != null && RT.dealUrl != null)
        // {
        // // 有scheme跳转事件
        // Intent intent = new Intent();
        // intent.setClass(SplashActivity.this, MainActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        // startActivity(intent);
        // finish();
        // return;
        // }

        UiUtil.setNavigationBarColor(this, R.color.white);

        showView(SplashView.class, new ViewParam());

        check();
    }

    private void check()
    {
        ArrayList<String> permissions = new ArrayList<String>();

        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);// 外部数据-读取
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);// 外部数据-写入

        // permissions.add(Manifest.permission.READ_CONTACTS); // 联系人-读取
        // permissions.add(Manifest.permission.WRITE_CONTACTS); // 联系人-写入

        permissions.add(Manifest.permission.CAMERA);// 摄像头

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);// 定位
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);// 定位

        permissions.add(Manifest.permission.READ_PHONE_STATE);// 读取手机信息

        // permissions.add(Manifest.permission.RECORD_AUDIO);// 录音

        // permissions.add(Manifest.permission.SEND_SMS);//短信
        // permissions.add(Manifest.permission.READ_SMS);//短信
        // permissions.add(Manifest.permission.RECEIVE_SMS);//短信

        permissions.add(Manifest.permission.USE_FINGERPRINT);// 指纹，正常不需要 但是有的傻逼手机需要

        doRequestPermissions(new PermissionRequestObj(permissions)
        {

            public void callback(boolean allGranted, List<String> permissionsList_denied, PermissionRequestObj pro)
            {
                if (allGranted)
                {
                    // 所有权限通过
                    go();
                } else
                {
                    if (permissionsList_denied != null)
                    {
                        boolean storage = permissionsList_denied.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
                                || permissionsList_denied.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                        boolean camera = permissionsList_denied.contains(Manifest.permission.CAMERA);

                        boolean location = permissionsList_denied.contains(Manifest.permission.ACCESS_FINE_LOCATION)
                                && permissionsList_denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION);

                        if (!storage && !camera)
                        {
                            // 必要条件通过 必须有存储权限
                            go();
                        } else
                        {
                            String pstr = "";
                            if (storage)
                                pstr += "【存储】";
                            if (camera)
                                pstr += "【拍照】";
                            // if (contacts)
                            // pstr += pstr.length() > 0 ? "、" : "" + "通讯录";
                            // if (location)
                            // pstr += pstr.length() > 0 ? "、" : "" + "位置";
                            pro.showManualSetupDialog(SplashActivity.this, pstr + "权限");
                        }
                    }
                }
            }

            public void onSettingCannel()
            {
                super.onSettingCannel();
                SplashActivity.this.finish();
                MobclickAgent.onKillProcess(RT.application);
                System.exit(0);
            }

            public void onSettingGoto()
            {
                super.onSettingGoto();
                SplashActivity.this.finish();
                MobclickAgent.onKillProcess(RT.application);
                System.exit(0);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        // super.onBackPressed();
    }

    public void go()
    {
        // 通知子页面 倒计时
        try
        {
            SplashView view = (SplashView) getTopView();
            view.openGo();
        } catch (Exception ex)
        {
        }
    }
}
