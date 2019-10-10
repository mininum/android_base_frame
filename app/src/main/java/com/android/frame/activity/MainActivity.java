package com.android.frame.activity;

import android.content.Intent;
import android.os.Bundle;


import com.android.frame.R;
import com.android.frame.view.MainView;

import org.rdengine.runtime.event.EventListener;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.util.UiUtil;
import org.rdengine.view.manager.BaseActivity;
import org.rdengine.widget.ToastHelper;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity
{
    private static WeakReference<MainActivity> self;

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        self = new WeakReference<MainActivity>(this);

        // 虚拟按键背景色
        UiUtil.setNavigationBarColor(this, R.color.view_background);

        showView(MainView.class, null);
        checkUpdate();

        EventManager.ins().registListener(EventTag.ACCOUNT_LOGIN, mEventListener);
        EventManager.ins().registListener(EventTag.ACCOUNT_LOGOUT, mEventListener);
    }

    EventListener mEventListener = new EventListener()
    {
        @Override
        public void handleMessage(int what, int arg1, int arg2, Object dataobj)
        {
            switch (what)
            {
                case EventTag.ACCOUNT_LOGIN :
                case EventTag.ACCOUNT_LOGOUT :
                {
                    // todo
                }
                break;
            }
        }
    };

    boolean doubleClickBack = true;
    long lastbackclicktime = 0;

    public void onBackPressed()
    {
        if (container.backInMask())
            if (this.backView())
            {
                // super.onBackPressed();

                if (doubleClickBack)
                {
                    long t = System.currentTimeMillis();
                    if (t - lastbackclicktime < 1000)
                    {
                        this.finish();
                    } else
                    {
                        ToastHelper.showToast("再次点击退出");
                        lastbackclicktime = t;
                    }
                } else
                {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }
    }

    boolean isPause = false;

    protected void onResume()
    {
        super.onResume();


        isPause = false;

//        if (RT.dealUrl != null)
//        {
//            if (RT.dealUrlOutside)
//            {
//                // 外部打开
//                RT.dealUrlOutside = false;
//                ViewGT.openWebViewOutside(this, RT.dealUrl);
//            } else
//            {
//                LinkScheme.dealLink(this, RT.dealUrl);
//            }
//
//            if (ActivityRecord.ins().getCount() > 0)
//                RT.getMainHandler().postDelayed(new Runnable()
//                {
//                    public void run()
//                    {
//                        DLOG.d("MainActivity", "onResume ActivityRecord clear mtn=" + ActivityRecord.ins().getCount());
//                        ActivityRecord.ins().finishAllActivitys();
//                    }
//                }, 200);
//        } else if (isResumeFromBackground)
//        {
//            // 从后台恢复到前台显示 ,判断时间和开屏广告物料，再次显示开屏广告
//            /** 1小时 3600000=60*60*1000 */
//            try
//            {
//                // 刷新服务配置的开关
//                // RT.loadAppServiceSwitch();
//
//                long splash_ad_space = 3600000;// 10*1000;
//                if (lastPauseTime != 0 && System.currentTimeMillis() - lastPauseTime >= splash_ad_space)
//                {
//                    // if (ADSplashMgr.getInstance().getNowAD(false) != null)
//                    // {
//                    // Intent it = new Intent();
//                    // it.setClass(this, SplashActivity.class);
//                    // it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    // startActivity(it);
//                    // }
//                }
//            } catch (Exception ex)
//            {
//            }
//
//        }
//        RT.dealUrl = null;
//        RT.dealUrlOutside = false;
//
//        JPushInterface.onResume(this);
    }

    @Override
    protected void onDestroy()
    {
        self = null;
        super.onDestroy();
        isPause = true;


        EventManager.ins().removeListener(EventTag.ACCOUNT_LOGIN, mEventListener);
        EventManager.ins().removeListener(EventTag.ACCOUNT_LOGOUT, mEventListener);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        isPause = true;
    }

    boolean hasCheckUpdate = false;

    public void checkUpdate()
    {
        if (hasCheckUpdate)
            return;
        hasCheckUpdate = true;

//        API_System.getServerConfig(new JSONResponse()
//        {
//            @Override
//            public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
//            {
//
//                try
//                {
//                    UpdateBean update = null;
//                    if (errCode == ErrorCode.ERROR_OK)
//                    {
//                        JSONObject j_config = json.optJSONObject("config");
//                        if (j_config != null)
//                        {
//                            // API_Serviceinfo.config = new API_Serviceinfo.ServerConfigBean().jsonParse(j_config);
//                            // API_Serviceinfo.saveConfig();
//                            // API_Serviceinfo.setHost();
//
//                            update = new UpdateBean().jsonParse(j_config);
//                            UpdateBean.saveUpdate(update);
//                        }
//                    }
//                    if (update == null)
//                        update = UpdateBean.loadUpdate();
//                    if (update != null && update.needUpgrade)
//                    {
//                        UpdateBean.showUpdateDialog(MainActivity.this, update);
//                    }
//                } catch (Exception e)
//                {
//                }
//            }
//        });

    }

    public static MainActivity getSelf()
    {
        if (self != null && self.get() != null)
        {
            return self.get();
        }
        return null;
    }
}
