package org.rdengine;

import android.app.Application;

import com.facebook.fresco.FrescoConfigConstants;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.runtime.RT.AppInfo;
import org.rdengine.util.DMUtil;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.RomUtil;
import org.rdengine.util.UiUtil;

public class RDApplication extends Application
{
    public static final boolean CHANGE_FONT = false;

    @Override
    public void onCreate()
    {
        super.onCreate();
        RT.application = this;

        String pName = DMUtil.getAppNameByPID(this, android.os.Process.myPid());
        DLOG.d("push", "pName:" + pName);
        if (!pName.equals(getApplicationContext().getPackageName()))
        {
            return;
        }

        if (CHANGE_FONT && PhoneUtil.hasJellyBean())
        {
            // CalligraphyUtils.textViewIncludeFontPadding = false;
            // CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
            // .setDefaultFontPath("fonts/SourceHanSansCN-Normal.ttf").setFontAttrId(R.attr.fontPath).build());
        }

        RT.ins().init();

        // 友盟统计初始化
        initUMengSDK();

        // fresco图片工具初始化
        FrescoConfigConstants.initialize(this);

        // 极光push
        // JPushInterface.setDebugMode(RT.DEBUG);
        // JPushInterface.init(this);
        // 极光设置别名
        // UserMgr.jpushSetAlias();

        int a = UiUtil.getMaxTextureSize(this);
        DLOG.e("cccmax", "gpuMaxImagePixelSize = " + a);

        try
        {
            DLOG.e("cccmax", "ROM = " + RomUtil.getName());
        } catch (Exception ex)
        {
        }

    }

    /**
     * 友盟统计初始化
     */
    private void initUMengSDK()
    {
        // String appkey:官方申请的Appkey
        String appkey = "5cf0eab34ca35710ab0011d6";
        if (!RT.PUBLISH)
            appkey = "5cf0eab34ca35710ab0011d6";// 测试key

        // String channel: 渠道号
        String channelId = AppInfo.Channel;

        // 使用友盟push时填写
        String pushSecret = null;

        // 设备类型
        int deviceType = UMConfigure.DEVICE_TYPE_PHONE;

        // Boolean isCrashEnable: 可选初始化. 是否开启crash模式
        Boolean isCrashEnable = true;

        // 初始化
        /*
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(RDApplication.this, appkey, channelId, deviceType, pushSecret);

        // 场景类型 EScenarioType eType: 场景模式，包含统计、游戏、统计盒子、游戏盒子
        // MobclickAgent.EScenarioType eType = MobclickAgent.EScenarioType.E_UM_NORMAL;
        // MobclickAgent.setScenarioType(RDApplication.this, eType);

        // 禁止默认的页面统计方式，这样将不会再自动统计Activity。
        // MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);

        // debug模式 跟随RT.DEBUG开关
        UMConfigure.setLogEnabled(RT.DEBUG);

        // 支持在子进程中统计自定义事件
        UMConfigure.setProcessEvent(true);

        // isEnable: true-回传错误信息, false-不回传错误信息。
        MobclickAgent.setCatchUncaughtExceptions(true);

        // interval 单位为毫秒，如果想设定为40秒，interval应为 40*1000.
        MobclickAgent.setSessionContinueMillis(60 * 1000);

    }

}
