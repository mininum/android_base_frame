package com.android.frame.logic;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.android.frame.BuildConfig;
import com.android.frame.logic.bean.User;
import com.android.frame.view.ViewGT;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.rdengine.http.HttpParam;
import org.rdengine.log.DLOG;
import org.rdengine.runtime.PreferenceHelper;
import org.rdengine.runtime.RT;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;
import org.rdengine.util.StringUtil;

import java.net.URLEncoder;

/**
 * 登录用户管理
 * 
 * @author CCCMAX
 */
public class UserMgr
{
    private static String TAG = "UserMgr";
    private static final String NAME = "LOGIN_USER";

    private User mLoginUser;

    /** volatile 保证【可见性】、防止【指令重排】，此处作用是防止【指令重排】 */
    private volatile static UserMgr mMgr;

    private UserMgr()
    {
        mLoginUser = new User();
    }

    public static UserMgr getInstance()
    {
        // 双重校验单例
        if (mMgr == null)
        {
            synchronized (UserMgr.class)
            {
                if (mMgr == null)
                {
                    mMgr = new UserMgr();
                }
            }
        }
        return mMgr;
    }

    public User getLoginUser()
    {
        return mLoginUser;
    }

    public boolean isLogined()
    {
        return !StringUtil.isEmpty(getSession()) && getUid() != 0;
    }

    public long getUid()
    {
        return mLoginUser.id;
    }

    public String getSession()
    {
        return mLoginUser.session;
    }

    /**
     * 登录成功之后调用此方法 设置全局已登录账号
     *
     * @param user
     */
    public void setLoginUser(User user)
    {
        DLOG.e(TAG, "setLoginUser " + user.nickname);
        mLoginUser = user;
        saveLoginUser();
        EventManager.ins().sendEvent(EventTag.ACCOUNT_LOGIN, 0, 0, getLoginUser());

    }

    /**
     * 加载本地保存的登录用户
     */
    public void loadLoginUser()
    {
        String content = PreferenceHelper.ins().getStringShareData(NAME, "");
        if (!TextUtils.isEmpty(content))
        {
            try
            {
                JSONObject json = new JSONObject(content);
                mLoginUser.jsonParse(json);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        DLOG.e(TAG, "loadLoginUser " + mLoginUser.nickname);
    }

    /**
     * 保存登录用户
     */
    public void saveLoginUser()
    {
        PreferenceHelper.ins().storeShareStringData(NAME, mLoginUser.toJsonString());
        PreferenceHelper.ins().commit();

        try
        {
            // 友盟统计中 设置用户登录登出
            if (!StringUtil.isEmpty(mLoginUser.session) && mLoginUser.id > 0)
            {
                MobclickAgent.onProfileSignIn("uid" + mLoginUser.id);
            } else
            {
                MobclickAgent.onProfileSignOff();
            }
        } catch (Exception ex)
        {
        }
    }

    /**
     * 注销
     */
    public void unlogin()
    {
        boolean ilogin = isLogined();
        DLOG.e(TAG, "unlogin " + mLoginUser.nickname);
        mLoginUser = new User();
        saveLoginUser();
        if (ilogin)
        {
            // 避免服务问题造成 注销死循环
            EventManager.ins().sendEvent(EventTag.ACCOUNT_LOGOUT, 0, 0, null);
            // UserMgr.jpushSetAlias();
        }
    }

    /**
     * 刷新登录用户的信息
     */
    public void requestMineInfo()
    {
        if (isLogined())
        {
            // TODO
            // API_User.getUserinfo(getUid(), new JSONResponse()
            // {
            // public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
            // {
            // if (json != null && errCode == ErrorCode.ERROR_OK)
            // {
            // JSONObject j_user = json.optJSONObject("user");
            // if (j_user != null && j_user.length() > 0)
            // {
            // String session = mLoginUser.session;
            // mLoginUser.jsonParse(j_user);
            // mLoginUser.session = session;
            // saveLoginUser();
            // EventManager.ins().sendEvent(EventTag.ACCOUNT_UPDATE_INFO, 0, 0, null);
            // }
            // } else if (errCode > 0)
            // {
            // // 登录失效
            // } else
            // {
            // // 网络错误
            // }
            //
            // DLOG.e(TAG, "requestMineInfo " + mLoginUser.nickname);
            //
            // }
            // }, false);
        }
    }

    /**
     * 向服务器检测当前登录的状态，登录正常 就刷新用户信息，登录失效 做账户注销处理
     */
    public void checkLoginState()
    {
        if (isLogined())
        {
            // TODO
            // API_User.refreshSession(new JSONResponse()
            // {
            // public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
            // {
            // if (errCode == ErrorCode.ERROR_OK)
            // {
            // // 登录状态
            // String session = json.optString("session");
            // if (!TextUtils.isEmpty(session))
            // {
            // getLoginUser().session = session;
            // saveLoginUser();
            // }
            // } else if (errCode > 0)
            // {
            // // 登录失败 网络层处理
            // } else
            // {
            // // 网络错误
            // }
            //
            // requestMineInfo(); // 刷新用户信息
            // API_Serviceinfo.requestServerConfig();
            // }
            // });
        } else
        {
            // API_Serviceinfo.requestServerConfig();
        }
    }

    /**
     * 检测是否登录了，如果没有登录 用dialog提示用户去登录页面
     *
     * @param context
     * @param loginListener
     * @return true已经登录;false未登录
     */
    public static boolean checkLoginedAndTipGotoLoginView(Context context, final LoginListener loginListener)
    {
        try
        {
            boolean islogined = UserMgr.getInstance().isLogined();
            if (!islogined && context instanceof Activity)
            {
                // Activity activity = (Activity) context;
                // GbTipDialog td = new GbTipDialog(activity);
                // td.setContent("还没有登录，是否现在就去登录？");
                // td.setBaseDialogOnclicklistener(new BaseDialogOnclicklistener()
                // {
                // public void onOkClick(Dialog dialog)
                // {
                // ViewGT.showLoginView(dialog.getOwnerActivity(), loginListener);
                // }
                //
                // public void onCancleClick(Dialog dialog)
                // {
                //
                // }
                // });
                // td.show();


            }

            return islogined;
        } catch (Exception e)
        {
            return false;
        }
    }

    public static String getUD()
    {
        String dv = "";
        try
        {
            dv = URLEncoder.encode(RT.PhoneInfo.userAgent, "UTF-8");
        } catch (Exception ex)
        {
        }

        String ud = "";
        StringBuffer sb = new StringBuffer();
        sb.append("pf=android");// 平台
      //  sb.append("&").append("dv=" + dv);// 手机机型
       // sb.append("&").append("cv=" + RT.AppInfo.Version);// 客户端版本
        sb.append("&").append("channel=" + RT.AppInfo.Channel);// 渠道号
        sb.append("&").append("uuid=" + RT.ins().getUuid());// 渠道号

        sb.append("&").append("appid=" + RT.ins().getAppid());// appid
        if (BuildConfig.VERSION_CODE >= 110)
            sb.append("&").append("version=" + RT.AppInfo.Version);// uuid

        getInstance();
        if (mMgr.isLogined())
        {
            sb.append("&").append("uid=" + mMgr.getUid());
            sb.append("&").append("session=" + mMgr.getSession());
        }
        // sb.append("&").append("requestId=" + UUID.randomUUID().toString().replace("-", ""));// 跟随每个请求 带不同的id 用 随机uuid
        // ud = Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
        // RFC2045,Base64一行不能超过76字符，超过则添加回车换行符。而httpheader中是不能换行的
        // ud = ud.replaceAll("\\s", "");

        ud = sb.toString();
        return ud;
    }

    public static String addUD(String url)
    {
        url = url.trim();
        String ret = url;
        String ud = UserMgr.getUD();
        if (!StringUtil.isEmpty(ud))
        {
            // ud = "UD=" + ud;
            if (url.equals("?"))
            {
                ret = url + ud;
            } else if (url.contains("?"))
            {
                ret = url + "&" + ud;
            } else
            {
                ret = url + "?" + ud;
            }
        }
        return ret;
    }

    public static void addUDtoHttpparam(HttpParam param)
    {
        if (param != null)
        {
            // param.addHeaderItem("AUTH_UD", getUD()); // 加到header中
        }
    }

    public static interface LoginListener
    {

        public void onLogined();

        public void onError();

        public void onCanceled();
    }

}
