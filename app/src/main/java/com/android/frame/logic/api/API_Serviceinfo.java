package com.android.frame.logic.api;

import android.text.TextUtils;


import com.android.frame.logic.model.JsonParser;

import org.json.JSONObject;
import org.rdengine.log.DLOG;
import org.rdengine.runtime.PreferenceHelper;
import org.rdengine.runtime.RT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class API_Serviceinfo
{
    public static String host_main = "https://api.maoshen.com/api/v1";
    public static String host_games = "https://gamesdk.maoshen.com/gamesdk/api/v1/client/";

    /** 用户接口 */
    public static String host_user;
    /** 文章、图文、视频 */
    public static String host_feed;
    /** 话题 */
    public static String host_topic;
    /** 标签 */
    public static String host_tag;
    /** 评论 */
    public static String host_comment;
    /** 游戏 */
    public static String host_game;
    /** 发现、推荐 */
    public static String host_discover;
    /** MTN */
    public static String host_mtn;
    /** 活跃 */
    public static String host_active;
    public static String host_exp;
    /** 系统 */
    public static String host_system;
    /** 商城 */
    public static String host_mall;
    /** 日志 */
    public static String host_log;
    /** 订单 */
    public static String host_order;
    /** 商品 */
    public static String host_commodity;
    /** 推荐内容 20190315 */
    public static String host_recommend;

    public static ServerConfigBean config;
    /** 付款 */
    public static String host_pay;
    public static String host_config;

    static
    {
        setHost();
    }

    public static void setHost()
    {
        loadConfig();

        if (RT.PUBLISH)
        {
            // 正式
            host_main = "https://api.maoshen.com/api/v1";

            if (config != null)
            {
                if (!TextUtils.isEmpty(config.serverurl))
                {
                    host_main = config.serverurl;
                }
            }
        } else
        {
            // 测试
            host_main = "http://test.maoshen.com/gamesdk/api";
        }

        if (!host_main.endsWith("/"))
            host_main += "/";

        host_user = host_main + "user/";
        host_feed = host_main + "feed/";
        host_topic = host_main + "topic/";
        host_tag = host_main + "tag/";
        host_comment = host_main + "comment/";
        host_game = host_games + "game/";
        host_discover = host_main + "discover/";
        host_mtn = host_main + "mtn/";
        host_active = host_main + "active/";
        host_exp = host_main + "exp/";
        host_system = host_main + "system/";
        host_mall = host_main + "mall/";
        host_log = host_main + "log/";
        host_order = host_main + "order/";
        host_commodity = host_main + "commodity/";
        host_recommend = host_main + "recommend/";
        host_pay = host_main + "pay/";
        host_config = host_main+"config/";
    }

    public static class ServerConfigBean extends JsonParser
    {
        String serverurl;
        String cdnurl;
        String weburl;

        @Override
        public ServerConfigBean jsonParse(JSONObject json)
        {
            try
            {
                serverurl = json.optString("serverurl");
                cdnurl = json.optString("cdnurl");
                weburl = json.optString("weburl");
            } catch (Exception ex)
            {
            }
            return this;
        }

        public JSONObject toJson()
        {
            JSONObject json = new JSONObject();
            try
            {
                json.put("serverurl", serverurl);
                json.put("cdnurl", cdnurl);
                json.put("weburl", weburl);
            } catch (Exception ex)
            {
            }
            return json;
        }
    }

    public static void saveConfig()
    {
        try
        {
            String jsonstr = "";
            if (config != null)
            {
                jsonstr = config.toJson().toString();
            }
            PreferenceHelper.ins().storeShareStringData(RT.PUBLISH ? "serviceinfo_config" : "serviceinfo_config_test",
                    jsonstr);
            PreferenceHelper.ins().commit();
            DLOG.e("serciveinfo", "save config  " + jsonstr);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void loadConfig()
    {
        try
        {
            String jsonstr = PreferenceHelper.ins()
                    .getStringShareData(RT.PUBLISH ? "serviceinfo_config" : "serviceinfo_config_test", "");
            JSONObject json = new JSONObject(jsonstr);
            config = new ServerConfigBean().jsonParse(json);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getMotionWebHost()
    {
        String host = "";

        if (RT.PUBLISH)
        {
            // 正式地址

            if (config == null)
            {
                loadConfig();
            }
            if (config != null && !TextUtils.isEmpty(config.weburl))
            {
                host = config.weburl;
            }

            if (TextUtils.isEmpty(host))
                host = "https://www.maoshen.com/";
        } else
        {
            host = "https://test.maoshen.com/";
        }

        if (!host.endsWith("/"))
            host += "/";
        return host;
    }

    public static void requestServerConfig()
    {
        // API_System.getServerConfig(new JSONResponse()
        // {
        // @Override
        // public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
        // {
        // if (errCode == ErrorCode.ERROR_OK)
        // {
        // if (json.has("setid"))
        // {
        // int setid = json.optInt("setid", 0);
        // if (UserMgr.getInstance().getSetid() == 0)
        // UserMgr.getInstance().saveSetid(setid);
        // }
        // JSONObject j_config = json.optJSONObject("config");
        // if (j_config != null)
        // {
        // API_Serviceinfo.config = new API_Serviceinfo.ServerConfigBean().jsonParse(j_config);
        // API_Serviceinfo.saveConfig();
        // API_Serviceinfo.setHost();
        // }
        // }
        // }
        // });
    }

    public final static boolean localTest = false;

    /**
     * 获取本地Assets下的api假数据
     * 
     * @param apiname
     * @return
     */
    public static JSONObject getLocalTestApiResponse(String apiname)
    {
        try
        {
            String jsonstr = getLocalAssetsText("test_api/" + apiname + ".txt");
            JSONObject json = new JSONObject(jsonstr);
            return json;
        } catch (Exception ex)
        {
        }
        return null;
    }

    /**
     * 获取本地Assets下的文件 转json
     *
     * @return
     */
    public static String getLocalAssetsText(String path)
    {
        try
        {
            InputStream is = RT.application.getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    is.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            String jsonstr = sb.toString();

            return jsonstr;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
