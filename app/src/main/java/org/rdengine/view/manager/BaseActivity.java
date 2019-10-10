package org.rdengine.view.manager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import com.android.frame.R;
import com.android.frame.dialog.BaseDialog;
import com.android.frame.dialog.GbTipDialog;
import com.umeng.analytics.MobclickAgent;

import org.rdengine.RDApplication;
import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.UiUtil;
import org.rdengine.widget.CustomInsetFrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * 使用RDEngine的Activity
 *
 * @author yangyu
 */
public class BaseActivity extends FragmentActivity implements ViewController
{

    final static int PERMISSIONS_REQUEST_CODE_ACTIVITY = 0x01;
    /**
     * 权限正在申请中
     */
    public static boolean permissionRequesting = false;
    // 横屏SCREEN_ORIENTATION_LANDSCAPE
    private final int ORIENTATION_L = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    // 竖屏SCREEN_ORIENTATION_PORTRAIT
    private final int ORIENTATION_P = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public int screen_width, screen_height;
    public float density;
    public int densityDpi;
    protected ViewManager mViewManager;
    protected ActivityContainer container;
    PermissionRequestObj currentPermissionRequest;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        // 状态栏颜色
        // UiUtil.setTransparentStatus(this);
        UiUtil.setStatusBarColor(this, android.R.color.transparent);// 不用android下的颜色 有的手机不支持
        // 虚拟按键背景色
        // UiUtil.setNavigationBarColor(this, R.color.view_background);

        makeViewManager();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        screen_width = metric.widthPixels; // 宽度（PX）
        screen_height = metric.heightPixels; // 高度（PX）
        density = metric.density; // 密度（0.75 / 1.0 / 1.5）
        densityDpi = metric.densityDpi; // 密度DPI（120 / 160 / 240）
        DLOG.e("screen", "w=" + screen_width + " h=" + screen_height + " density=" + density + " Dpi=" + densityDpi
                + " Metrics=" + metric);

    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        if (RDApplication.CHANGE_FONT)
            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        else super.attachBaseContext(newBase);
    }

    protected void makeViewManager()
    {

        if (UiUtil.TransparentStatusbar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            CustomInsetFrameLayout layout = new CustomInsetFrameLayout(this);
            container = new ActivityContainer(this, null);
            layout.addView(container);
            this.setContentView(layout);
        } else
        {
            container = new ActivityContainer(this, null);
            this.setContentView(container);
        }
        mViewManager = new ViewManager(this, container.viewStackContainer);
    }

    public ViewManager getViewManager()
    {
        return mViewManager;
    }

    public ActivityContainer getActivityContainer()
    {
        return container;
    }

    @Override
    public FrameLayout getMaskContainer()
    {
        return container.maskContainer;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    /** activity最后一次pause的时间 */
    public long lastPauseTime = 0;

    @Override
    protected void onPause()
    {
        super.onPause();

        isFront = false;

        lastPauseTime = System.currentTimeMillis();

        try
        {
            if (mViewManager != null && mViewManager.getTopView() != null)
            {
                mViewManager.getTopView().onHide();
            }
            MobclickAgent.onPause(this);
        } catch (Throwable ex)
        {
            ex.printStackTrace();
        }

        RT.getMainHandler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean isForeGround = PhoneUtil.isForeApp();
                    RT.setAppIsBackground(!isForeGround);
                    DLOG.e("BaseActivity", "onPause Delayed:1000ms appisForeGround=" + isForeGround);
                } catch (Exception ex)
                {
                }
            }
        }, 1000);

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        isFront = true;

        try
        {
            if (mViewManager != null && mViewManager.getTopView() != null)
            {
                mViewManager.getTopView().onShow();
            }

            MobclickAgent.onResume(this);
        } catch (Throwable ex)
        {
            ex.printStackTrace();
        }

        try
        {
            boolean isAppIsBackground = RT.isAppIsBackground();

            RT.setAppIsBackground(false);
            DLOG.e("BaseActivity", "onResume appisForeGround=" + true);

            if (isAppIsBackground)
            {
                // app 从后台恢复到前台显示
                isResumeFromBackground = true;
                onResumeFromAppBackground();
            } else
            {
                isResumeFromBackground = false;
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private boolean isFront = false;
    protected boolean isResumeFromBackground = false;

    public boolean isFront()
    {
        return isFront;
    }

    /** 当应用从后台恢复显示 */
    protected void onResumeFromAppBackground()
    {
        DLOG.e("BaseActivity", "onResumeFromAppBackground  应用从后台恢复到前台显示");
    }

    @Override
    public void showView(Class<? extends BaseView> clazz, ViewParam param)
    {
        if (mViewManager == null)
        {
            throw new Error("Container not set!!!!!!");
        }
        mViewManager.showView(clazz, param);
    }

    @Override
    public boolean backView()
    {
        return mViewManager.backView();
    }

    @Override
    public void killViewAt(int index)
    {
        mViewManager.killViewAt(index);
    }

    @Override
    public int getViewSize()
    {
        return mViewManager.getViewSize();
    }

    @Override
    public int getViewIndex(BaseView baseview)
    {
        return mViewManager.getViewIndex(baseview);
    }

    @Override
    public void killAllHistoryView()
    {
        mViewManager.killAllHistoryView();
    }

    @Override
    public BaseView getViewAt(int index)
    {
        return mViewManager.getViewAt(index);
    }

    @Override
    public void killAllSameView(BaseView view)
    {
        mViewManager.killAllSameView(view);
    }

    @Override
    public void killSameViewAndKeepSome(BaseView view, int keepCount)
    {
        mViewManager.killSameViewAndKeepSome(view, keepCount);
    }

    @Override
    public List<BaseView> findClazzViews(Class<? extends BaseView> clazz)
    {
        return mViewManager.findClazzViews(clazz);
    }

    @Override
    public void onBackPressed()
    {
        if (container.backInMask())
        {
            if (this.backView())
            {
                super.onBackPressed();
            }
        }
    }

    // public void swipeviewOnDismiss(SwipeBackView sbv)
    // {
    // mViewManager.swipeviewOnDismiss(sbv);
    // }

    @Override
    public boolean getWindowVisibility()
    {
        return false;
    }

    @Override
    public void setWindowVisibility(boolean visibility)
    {}

    @Override
    public void setDefaultLayoutParams()
    {}

    @Override
    public void updateWindowLayoutParams(int x, int y, int w, int h, int inputType, boolean topOnInput)
    {}

    @Override
    public BaseView getTopView()
    {
        int index = mViewManager.getViewSize() - 1;
        if (index < 0)
            return null;
        return mViewManager.getViewAt(index);

    }

    @Override
    public void moveToTop(BaseView view)
    {
        mViewManager.moveToTop(view);
    }

    // ANDROID 6.0（SDK23）开始手动申请权限----------------------------------------------------------------------Start

    public void moveToBottom(BaseView view)
    {
        mViewManager.moveToBottom(view);
    }

    @Override
    public void changeScreenOrientation(boolean isPortrait)
    {
        try
        {
            setRequestedOrientation(isPortrait ? ORIENTATION_P : ORIENTATION_L);
            EventManager.ins().sendEvent(EventTag.APP_ON_SCREEN_ORIENTATION_CHANGE, isPortrait ? 1 : 0, 0, null);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        EventManager.ins().sendEvent(EventTag.APP_ON_ACTIVITY_RESULT, requestCode, resultCode, data);
    }

    /**
     * 权限申请
     */
    public void doRequestPermissions(PermissionRequestObj pro)
    {
        final int targetsdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < 23 || targetsdkVersion < 23)
        {
            if (pro != null)
            {
                pro.callback(true, null, pro);
            }
            return;
        }

        // 需要请求的权限列表
        final List<String> permissions_NeedRequest = new ArrayList<String>();
        // 用户设置不在提醒的权限列表 需要给出提示
        final List<String> permissions_NeedTip = new ArrayList<String>();

        for (int i = 0; i < pro.size(); i++)
        {
            String permission = pro.get(i);
            if (!filterPermission(permissions_NeedRequest, permission, pro))
            {
                // 添加到需要提示内容中
                permissions_NeedTip.add(permission);
            }
        }

        if (permissions_NeedRequest.size() > 0)
        {

            // if (permissions_NeedTip.size() > 0) {
            // permissionRequesting = false;
            // // 请求权限之前 显示dialog 告知用户用权限的原因
            // showPermissionTipDialog(pro.makeReasonTips(permissions_NeedTip), new View.OnClickListener() {
            //
            // public void onClick(View view) {
            // switch (view.getId()) {
            // case R.id.ok : {
            // // ok确定后 重新请求权限
            // permissionRequesting = true;
            // ActivityCompat.requestPermissions(BaseActivity.this,
            // permissions_NeedRequest.toArray(new String[permissions_NeedRequest.size()]),
            // PERMISSIONS_REQUEST_CODE_ACTIVITY);
            // }
            // break;
            // case R.id.cancel :
            // // 取消
            // try {
            // if (currentPermissionRequest != null)
            // currentPermissionRequest.callback(false, null);
            // currentPermissionRequest = null;
            // } catch (Exception e) {
            // LogUtils.e("PermissionRequest", "Callback Exception tipcancel", e);
            // }
            // break;
            // }
            //
            // }
            // });
            // currentPermissionRequest = pro;
            // return;
            // }

            permissionRequesting = true;
            currentPermissionRequest = pro;
            ActivityCompat.requestPermissions(BaseActivity.this,
                    permissions_NeedRequest.toArray(new String[permissions_NeedRequest.size()]),
                    PERMISSIONS_REQUEST_CODE_ACTIVITY);
            return;
        }

        // do something
        currentPermissionRequest = null;
        permissionRequesting = false;
        try
        {
            pro.callback(true, null, pro);
        } catch (Exception e)
        {
            DLOG.e("PermissionRequest", "Callback Exception", e);
        }
    }

    /**
     * 过滤权限 是否已经授权，没有授权的添加到请求列表中
     *
     * @param permissions_NeedRequest
     *            需要请求的权限列表
     * @param permission
     *            权限
     * @param pro
     * @return true 请求权限就好了；false 用户设置了不在提醒 需要给出提示
     */
    private boolean filterPermission(List<String> permissions_NeedRequest, String permission, PermissionRequestObj pro)
    {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        {
            permissions_NeedRequest.add(permission);// 没有权限的添加到需要申请的列表中
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        } else
        {
            pro.authItem(permission);
        }
        return true;
    }

    private void showPermissionTipDialog(CharSequence message, View.OnClickListener clickListener)
    {
        // Dialog dlg = MyDialog.showTipsDialogs(this, message, "取消", "确定", clickListener);
        // dlg.setCancelable(false);
        // dlg.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        // 权限请求的返回结果
        permissionRequesting = false;
        switch (requestCode)
        {
        case PERMISSIONS_REQUEST_CODE_ACTIVITY :
        {
            try
            {
                if (currentPermissionRequest != null)
                {
                    for (int i = 0; i < permissions.length; i++)
                    {
                        int r = grantResults[i];
                        if (r == PackageManager.PERMISSION_GRANTED)
                        {
                            String p = permissions[i];
                            currentPermissionRequest.authItem(p);
                        }
                    }
                    boolean AllGranted = currentPermissionRequest.isAllGranted();
                    currentPermissionRequest.isAsyn = true;
                    currentPermissionRequest.callback(AllGranted, currentPermissionRequest.permissionsList_denied,
                            currentPermissionRequest);
                }
            } catch (Exception e)
            {
                DLOG.e("PermissionRequest", "Callback Exception onRequestPermissionsResult", e);
            }
        }
            break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static abstract class PermissionRequestObj
    {

        // 是否是异步执行
        public boolean isAsyn = false;

        List<String> permissionsList;
        Map<String, Integer> perms = new HashMap<String, Integer>();

        // 请求结果 没有通过的权限列表
        List<String> permissionsList_denied;

        public PermissionRequestObj(List<String> permissionsList)
        {
            this.permissionsList = permissionsList;
            for (String p : permissionsList)
            {
                perms.put(p, PackageManager.PERMISSION_DENIED);
            }
        }

        public int size()
        {
            return permissionsList.size();
        }

        public String get(int index)
        {
            return permissionsList.get(index);
        }

        private void authItem(String permission)
        {
            perms.put(permission, PackageManager.PERMISSION_GRANTED);
        }

        /**
         * 判断是否所有权限申请都通过了
         *
         * @return
         */
        private boolean isAllGranted()
        {
            boolean ret = true;
            for (String p : permissionsList)
            {
                int auth = perms.get(p);
                if (auth != PackageManager.PERMISSION_GRANTED)
                {

                    ret = false;

                    if (permissionsList_denied == null)
                        permissionsList_denied = new ArrayList<String>();
                    permissionsList_denied.add(p);
                }
            }
            return ret;
        }

        public CharSequence makeReasonTips(List<String> permissions_NeedTip)
        {
            String appname = RT.application.getResources().getString(R.string.app_name);
            String ret = "您的手机已禁用了当前需要使用的功能权限，请到手机“<font color=#ff0000>设置--权限管理</font>”中设置找到\"<font color=#ff0000>"
                    + appname + "</font>\",并设置“<font color=#ff0000>信任此应用</font>”后点确定。";
            return Html.fromHtml(ret);
        }

        /**
         * 获取权限失败之后 显示手动设置的Dialog 引导用户去系统设置里设置权限
         */
        public void showManualSetupDialog(final Activity activity, String permission)
        {
            String appname = RT.application.getResources().getString(R.string.app_name);
            String msg = "请在<font color=#ff0000>设置</font>-<font color=#ff0000>应用</font>-<font color=#ff0000>" + appname
                    + "</font>-<font color=#ff0000>权限</font>中开启" + permission + "，否则将无法正常使用。";

            GbTipDialog td = new GbTipDialog(activity);
            td.setTitle(Html.fromHtml(msg));
            // td.setBtnOKText("去设置");
            // td.setBtnCancelText("取消");
            td.setBaseDialogOnclicklistener(new BaseDialog.BaseDialogOnclicklistener()
            {

                public void onOkClick(Dialog dialog)
                {
                    // ok确定后 引导用户去设置
                    Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    activity.startActivity(intent);
                    onSettingGoto();
                }

                public void onCancleClick(Dialog dialog)
                {
                    onSettingCannel();
                }
            });
            td.show();

        }

        /**
         * 权限申请的结果回调
         *
         * @param allGranted
         *            是否所有请求的权限都获取到了
         * @param permissionsList_denied
         *            没有被授权的权限列表
         */
        public abstract void callback(boolean allGranted, List<String> permissionsList_denied,
                PermissionRequestObj pro);

        public void onSettingCannel()
        {}

        public void onSettingGoto()
        {}
    }

    // ANDROID 6.0（SDK23）开始手动申请权限----------------------------------------------------End
}
