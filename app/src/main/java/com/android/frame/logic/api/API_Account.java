package com.android.frame.logic.api;

import com.android.frame.logic.UserMgr;
import com.android.frame.logic.bean.User;

import org.rdengine.http.HttpParam;
import org.rdengine.http.HttpUtil;
import org.rdengine.http.JSONResponse;

public class API_Account
{
    public static String getHost()
    {

        return API_Serviceinfo.host_user;
    }
    public static String getPayHost(){

        return API_Serviceinfo.host_pay;
    }

    /** 手机号注册 */
    public static final String REGIST = "regist";

    /** 获取短信验证码 */
    public static final String GETVCODE = "getvcode";

    /** 用户名密码登录 */
    public static final String LOGINPWD = "loginbypassword";

    /** 忘记密码 */
    public static final String MODIFYPASSWORD = "modifypassword";

    /** 定位页 */
    public static final String ADDMYPOSITION = "addmyposition";

    /** 获取用户信息 */
    public static final String INFO = "info";

    /** 设置资金密码 */
    public static final String SETFUNDPWD = "setfundpwd";

    /** 找回资金密码 */
    public static final String RETRIEVEFUNDPWD = "retrievefundpwd";

    /** 检测资金密码 */
    public static final String CHECKFUNDPWD = "checkfundpwd";

    /** 个人中心 */
    public static final String PERSONALCENTER = "personalcenter";

    /** 修改个人资料 */
    public static final String MODIFYPERSONALINFO = "modifypersonalinfo";
    /** 绑定身份证 */
    public static final String CERTIFICATECOMMIT = "certificatecommit";
    /** 身份证信息*/
    public static final  String CERTIFICATEINFO = "certificateinfo";

    /** 验证手机号 */
    public static final String VERFYPHONE = "verifyphone";

    /** 修改手机号 */
    public static final String MODIFYPHONE = "modifyphone";

    /** 获取余额 */
    public  static final String GETBALANCE = "getbalance";

    // 手机号注册
    public static void regist(String phone, String vcode, String account, String password, String invite_code,
            JSONResponse callback)
    {

        String url = getHost() + REGIST;

        HttpParam hp = new HttpParam();
        hp.put("phone", phone);
        hp.put("vcode", vcode);
        hp.put("account", account);
        hp.put("password", password);
        hp.put("invite_code", invite_code);

        HttpUtil.postAsync(url, hp, callback);

    }

    // 获取短信验证码
    public static void getVcode(String phone, String pcode, JSONResponse callback)
    {

        String url = getHost() + GETVCODE;
        HttpParam hp = new HttpParam();
        hp.put("phone", phone);
        hp.put("pcode", pcode);
        HttpUtil.postAsync(url, hp, callback);

    }

    // 用户名密码登录

    public static void loginPassword(String phone, String account, String password, JSONResponse callback)
    {

        String url = getHost() + LOGINPWD;

        HttpParam hp = new HttpParam();
        hp.put("phone", phone);
        hp.put("account", account);
        hp.put("password", password);
        HttpUtil.postAsync(url, hp, callback);

    }

    // 忘记密码
    public static void forgetPassword(String phone, String vcode, String password, String newPassword,
            JSONResponse callback)
    {

        String url = getHost() + MODIFYPASSWORD;
        HttpParam hp = new HttpParam();
        hp.put("phone", phone);
        hp.put("vcode", vcode);
        hp.put("password", password);
        hp.put("newpassword", newPassword);
        HttpUtil.postAsync(url, hp, callback);

    }

    // 获取用户信息
    public static void getUserInfo(int uid, JSONResponse callback)
    {
        String url = getHost() + INFO;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("uid", uid);
        HttpUtil.getAsync(url, hp, callback);

    }

    // 设置资金密码
    public static void setFundPwd(String old_fund_pwd, String new_fund_pwd, JSONResponse callback)
    {

        String url = getHost() + SETFUNDPWD;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("old_fund_pwd", old_fund_pwd);
        hp.put("new_fund_pwd", new_fund_pwd);
        HttpUtil.postAsync(url, hp, callback);
    }

    // 找回资金密码
    public static void retrieveFundPwd(int vcode, String login_pwd, JSONResponse callback)
    {
        String url = getHost() + RETRIEVEFUNDPWD;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("vcode", vcode);
        hp.put("login_pwd", login_pwd);

        HttpUtil.postAsync(url, hp, callback);
    }

    // 定位
    public static void addMyPosition(int areacode, JSONResponse callback)
    {
        String url = getHost() + ADDMYPOSITION;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("area_code", areacode);
        HttpUtil.postAsync(url, hp, callback);
    }

    // 个人中心
    public static void personalCenter(JSONResponse callback)
    {

        String url = getHost() + PERSONALCENTER;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        HttpUtil.getAsync(url, hp, callback);
    }

    // 修改个人资料
    public static void notifyPerson(User user, JSONResponse callback)
    {
        String url = getHost() + MODIFYPERSONALINFO;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("avatar", user.avatar);
        hp.put("nickname", user.nickname);
        hp.put("birthday", user.birthday);
        hp.put("gender", user.gender);
        HttpUtil.postAsync(url, hp, callback);

    }

    // 检测资金密码
    public static void checkFundPwd(String fund_pwd, JSONResponse callback)
    {
        String url = getHost() + CHECKFUNDPWD;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("fund_pwd", fund_pwd);
        HttpUtil.postAsync(url, hp, callback);

    }

    // 绑定身份证
    public static void bindRealUser(String certificate_no, String certificate_name, String face_path, String back_path,
            JSONResponse callback)
    {

        String url = getHost() + CERTIFICATECOMMIT;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("certificate_no", certificate_no);
        hp.put("certificate_name", certificate_name);
        HttpParam imgs = new HttpParam();
        imgs.put("face", face_path);
        imgs.put("back", back_path);
        hp.putOpt("certificate_images", imgs.getJson());

        HttpUtil.postAsync(url, hp, callback);
    }
    //获取身份证信息
    public static void getRealuserCode(JSONResponse callback){
        String url = getHost() + CERTIFICATEINFO;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        HttpUtil.getAsync(url,hp,callback);

    }
    //验证手机号
    public static void verifyPhone(String vcode, JSONResponse callback){
        String url = getHost() + VERFYPHONE;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("vcode",vcode);
        HttpUtil.postAsync(url,hp,callback);

    }
    //修改手机
    public static void modifyphone(String phone, String vcode, JSONResponse callback){
        String url = getHost() + MODIFYPHONE;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        hp.put("phone",phone);
        hp.put("vcode",vcode);
        HttpUtil.postAsync(url,hp,callback);

    }
    //获取余额
    public static void getBalance(JSONResponse callback){
        String url = getPayHost() + GETBALANCE;
        url = UserMgr.addUD(url);
        HttpParam hp = new HttpParam();
        HttpUtil.getAsync(url,hp,callback);

    }

}
