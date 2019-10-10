package org.rdengine.runtime.event;

public class EventTag
{
    public static final int APP_SCREEN_POWER_CHANGE = 0x0001;// 屏幕电源开关 0关闭 1打开

    /**
     * BaseActivity中转<br>
     * onActivityResult(int requestCode, int resultCode, Intent data)
     */
    public static final int APP_ON_ACTIVITY_RESULT = 0x0002;

    /**
     * 屏幕横竖切换 arg1: 1:isPortrait 竖屏
     */
    public static final int APP_ON_SCREEN_ORIENTATION_CHANGE = 0x0003;

    /**
     * 导航tabhost显示状态切换 arg1 0隐藏 1显示
     */
    public static final int MAIN_HOST_SET_VISIBILITY = 0x0004;

    /** 用户登录 */
    public static final int ACCOUNT_LOGIN = 0x1001;
    /** 用户注销登录 */
    public static final int ACCOUNT_LOGOUT = 0x1002;
    /** 登录的用户刷新信息 */
    public static final int ACCOUNT_UPDATE_INFO = 0x1003;

    /** 首页资产隐藏显示的变化 arg1 0显示 1隐藏 */
    public static final int HOME_ASSETS_EYES_STATE_CHENGE = 0x2001;
    /** 法币显示类型切换 */
    public static final int LEGAL_CURRENCY_TYPE_CHENGE = 0x2002;

    /** zxing识别二维码 */
    public static final int ZXING_REQUEST_CODE = 0x3001;

    /** 修改地址成功 */
    public static final int ADDRESS_EDITD = 0x3002;
    /** 添加地址成功 */
    public static final int ADDRESS_ADDED = 0x3003;

    /** 提币 事件 */
    public static final int CURRENCY_WITHDRAW = 0x4001;

}
