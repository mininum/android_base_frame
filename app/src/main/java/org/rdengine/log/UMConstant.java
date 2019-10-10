package org.rdengine.log;

public enum UMConstant
{
    // 账号
    AppRegist, // 注册帐号 只有手机注册
    AppLogin, // 登录帐号
    AppLogout, // 退出账号
    AppTokenInvalid, // token失效

    // 分享
    ShareClass, // 点击分享哪种类型 feed、topic、user、image、web、app、topic_timeline
    ShareTo, // 分享到 Wechat、WechatMoments、SinaWeibo、QQ、QZone

    // 下载更新
    AppUpgradeOk, // 确定下载新版本
    AppUpgradeDialogShow, // 展示了升级提示

    SdkMtiPay, // success,cancel,fail
    H5GameMtiPay,// success,cancel,fail

    // 主界面 tab按钮点击
    motion_app_main_tab_click, // 首页home,发现discover,消息msg,我的mine
}
