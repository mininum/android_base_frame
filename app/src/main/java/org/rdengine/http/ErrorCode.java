package org.rdengine.http;

public class ErrorCode
{

    public static final int ERROR_OK = 0;// 成功
    public static final int ERROR_FAIL = -1;// 失败
    public static final int ERROR_NO_NET = -2;// 无网络

    /** 验证码获取失败 */
    public static final int SERVICE_VCOD_EERROR = 301;
    /** 找不到用户 */
    public static final int SERVICE_USER_LOGIN = 302;
    /** 创建新用户失败 */
    public static final int SERVICE_REGIST_ERROR = 303;

    /** 用户未登录 */
    public static final int SERVICE_USER_UNLOGIN = 305;
    /** 修改用户信息失败 */
    public static final int SERVICE_UPDATE_INFO_ERROR = 306;
    /** 手机验证码不正确 */
    public static final int SERVICE_USER_VCODE_FAILD = 307;
    /** 用户已注册 */
    public static final int SERVICE_USER_IS_CREATED = 308;
    /** 修改密码失败 */
    public static final int SERVICE_FIX_PASSWORD_FAILD = 309;

    /** 用户被删或封禁 */
    public static final int SERVICE_USER_BLACK = 311;
    /** 权限不足 */
    public static final int SERVICE_AUTH_ERROR = 312;

    /** not ok */
    public static final int SERVICE_NOT_OK = 400;
    /** 请求参数不正确 */
    public static final int SERVICE_PARAMS_NOT_MATCH = 401;

    /** 签名失败 */
    public static final int SERVICE_SIGN_ERROR = 501;
    /** 创建PIN Code失败 */
    public static final int SERVICE_SET_PINCODE_ERROR = 502;
    /** 获取资产失败 */
    public static final int SERVICE_GET_ASSET_FAILED = 503;
    /** 找不到提现地址 */
    public static final int SERVICE_ADDRESS_LIST_FAILED = 504;
    /** 创建提现地址失败 */
    public static final int SERVICE_ADD_ADDRESS_FAILED = 505;
    /** 找不到注册应用 */
    public static final int SERVICE_NOT_FOUND_APP = 506;
    /** 创建订单失败 */
    public static final int SERVICE_CREATE_ORDER_FAILED = 507;
    /** 钱包余额不足 */
    public static final int SERVICE_NO_ENOUGH_BALANCE = 508;
    /** 转账失败 */
    public static final int SERVICE_TRANSFER_FAILED = 509;
    /** 可交易库存不足 */
    public static final int SERVICE_ETM_STOCK_NOT_ENOUGH = 514;

}
