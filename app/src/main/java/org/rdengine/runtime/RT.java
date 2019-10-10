package org.rdengine.runtime;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;

import com.android.frame.BuildConfig;
import com.android.frame.logic.UserMgr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rdengine.log.DLOG;
import org.rdengine.util.DeviceConfig;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class DMRT.
 */
public class RT
{


    /** 腾讯防水墙 */
    public static final String TCAPTCHA = "2046349567";

    // --应用配置
    /** The Constant DEBUG. */
    // #define debugenabled='${debug.enabled}'
    // #ifdef debugenabled
    // #expand public static final boolean DEBUG = %debugenabled%;
    // #else
    public static boolean DEBUG = true;
    // #endif

    /** debug图片开关 可以看到图片的log */
    public static boolean IMAGELOG = false;

    /** true 生产环境， false 测试环境 */
    public static boolean PUBLISH = false;

    /** log写入文件 */
    public static boolean WriteLog = false;

    private static RT self = null;

    public static Application application = null;

    /** The m local external path. */
    public static String mLocalExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public final static String _RT = "mgstation";

    // 应用的根目录
    /** The Constant ROOT. */
    public static String ROOT = _RT;

    // 缺省根目录
    /** The default root path. */
    public static String defaultRootPath = mLocalExternalPath.concat("/").concat(ROOT);

    static
    {
        try
        {
            File file = new File(defaultRootPath.endsWith("/") ? defaultRootPath : defaultRootPath.concat("/"));
            boolean ismakedir = false;
            if (!file.exists())
            {
                String tmp = mLocalExternalPath.concat("/").concat(ROOT);
                File t = new File(tmp);
                if (t.exists())
                {
                    if (!t.renameTo(file))
                    {
                        // 有小写，换成大写
                        defaultRootPath = tmp;
                    }
                } else
                {
                    ismakedir = file.mkdirs();
                    if (!ismakedir)
                    {
                        if (RT.isMount())
                        {
                            // 目录被强制占用
                            ROOT = _RT;
                            defaultRootPath = mLocalExternalPath.concat("/").concat(ROOT);
                        }
                    }
                }
            }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public static String defaultCache = defaultRootPath.concat("/cache/");
    public static String defaultImage = defaultRootPath.concat("/images/");
    public static String defaultVoice = defaultRootPath.concat("/voices/");
    public static String defaultError = defaultRootPath.concat("/error/");
    public static String defaultLog = defaultRootPath.concat("/logs");
    public static String defaultScreenshot = defaultRootPath.concat("/screenshot");
    public static String tempImage = defaultImage.concat("temp/");
    public static String adImage = defaultImage.concat("ad/");
    public static String defaultVersion = defaultRootPath.concat("/version/");

    public static boolean hasGetHome = false;

    /**
     * Instantiates a new dmrt.
     */
    private RT()
    {
        loadConfig();
    }

    public static String dealUrl = null;
    public static boolean dealUrlOutside = false;

    /**
     * Ins.
     * 
     * @return the dmrt
     */
    public static synchronized RT ins()
    {
        if (self == null)
        {
            self = new RT();
        }
        return self;
    }

    // 是否初始化完
    /** The m is init. */
    public static boolean mIsInit = false;

    /**
     * 初始化 Inits the.
     */
    public void init()
    {
        // 构造对象
        synchronized (this)
        {
            if (!mIsInit)
            {

                try
                {
                    DLOG.d("cccmax", "RT.init()");

                    mkdirs();
                    // 初始化对象数据
                    RT.initTDInfo();
                    PhoneUtil.getPhoneSet(application);// 获取手机信息
                    ins().regReceivers();
                    UserMgr.getInstance();// .loadLoginUser();// 加载已经登录的用户
                    // UserMgr.getInstance().checkLoginState();// 判断登录状态 正常的话再刷新用户信息
                    // UserMgr.getInstance().requestMineInfo();// 更新用户信息 来校验登录状态

                    // startServices();
                } catch (Exception e)
                {
                    e.printStackTrace();
                } catch (UnsatisfiedLinkError e)
                {
                    e.printStackTrace();
                } catch (NoClassDefFoundError e)
                {
                    e.printStackTrace();
                }

                mIsInit = true;

                // CProcess.startCProcess(application);

                // loadAppServiceSwitch();

                // API_Serviceinfo.requestServerConfig();
            }
        }

        // CheckErrorThread cethread = new CheckErrorThread();
        // cethread.start();
    }

    private RTReceiver mReceiver;

    private void regReceivers()
    {
        // 注册
        if (mReceiver == null)
        {
            mFilter = new IntentFilter();
            mFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mFilter.addAction(Intent.ACTION_SCREEN_ON);
            // mFilter.addAction(Intent.ACTION_TIME_TICK);

            mFilter.setPriority(Integer.MAX_VALUE);
            mReceiver = new RTReceiver();
            RT.application.registerReceiver(mReceiver, mFilter);
        }
    }

    // private static AppSwitchBean appServiceSwitch;
    //
    // public static AppSwitchBean getAppSwitch()
    // {
    // if (appServiceSwitch == null)
    // appServiceSwitch = new AppSwitchBean();
    // return appServiceSwitch;
    // }

    // public static void loadAppServiceSwitch()
    // {
    // API_Serviceinfo.getAppSwitch(new JSONResponse()
    // {
    // @Override
    // public void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached)
    // {
    // if (json != null && errCode == ErrorCode.ERROR_OK)
    // {
    // try
    // {
    // getAppSwitch().jsonParse(json.optJSONObject("data"));
    // EventManager.ins().sendEvent(EventTag.APP_SWITCH_SERVICE_CHANGE, 0, 0, null);
    // } catch (Exception ex)
    // {
    // }
    // }
    // }
    // });
    // }

    private IntentFilter pFilter;

    private IntentFilter mFilter;

    /**
     * Mkdirs.
     */
    public static void mkdirs()
    {
        try
        {

            File file = new File(defaultImage);
            if (!file.exists())
            {
                file.mkdirs();
            }
            file = new File(defaultVoice);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // try
        // {
        // File file = new File(defaultChat);
        // if (!file.exists())
        // {
        // file.mkdirs();
        // }
        //
        // } catch (Exception e)
        // {
        // e.printStackTrace();
        // }

        try
        {
            File file = new File(defaultImage);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            File file = new File(tempImage);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
        }

        try
        {
            File file = new File(adImage);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception ex)
        {
        }

        try
        {
            File file = new File(defaultCache);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            File file = new File(defaultLog);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            File file = new File(defaultVersion);
            if (!file.exists())
            {
                file.mkdirs();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // try
        // {
        // File file = new File(defaultScreenshot);
        // if (!file.exists())
        // {
        // file.mkdirs();
        // }
        // } catch (Exception e)
        // {
        // e.printStackTrace();
        // }

        CacheMgr.getInstance().clearTempImage(null);// 清理临时图片
        CacheMgr.getInstance().scanAllCacheSize(null);// 扫描缓存大小
    }

    public static String getString(int res)
    {
        return RT.application.getString(res);
    }

    public static String getAtom()
    {
        String atom = "";
        // atom += "uid=" + UserManager.ins().getUid();
        // atom += "&sid=" + UserManager.ins().getSessionId();
        atom += "&cc=" + AppInfo.Channel;
        atom += "&pf=android";
        // atom += "&cn=" + Network.getNetTypeString();
        atom += "&cv=" + AppInfo.Version;
        atom += "&dt=" + PhoneInfo.userAgent;
        atom += "&imei=" + PhoneInfo.imei;
        atom += "&imsi=" + PhoneInfo.imsi;
        atom += "&pn=" + PhoneInfo.phoneNumber;
        // DLOG.d("atom", "uid=" + UserManager.ins().getUid());
        // try
        // {
        // return URLEncoder.encode(Base64.encodeToString(atom.getBytes(), Base64.DEFAULT), "UTF-8");
        // } catch (UnsupportedEncodingException e)
        // {
        // e.printStackTrace();
        // }
        return Base64.encodeToString(atom.getBytes(), Base64.DEFAULT);
    }

    /**
     * The Class PhoneInfo.
     */
    public static class PhoneInfo
    {

        /** 手机号. */
        public static String phoneNumber = "";
        /** 设备号.暂留 */
        public static String devi = "";

        /** 手机 imei. */
        public static String imei = "";

        /** 手机卡 imsi. */
        public static String imsi = "";

        /** 手机机型. */
        public static String userAgent = "";

        /** 这里使用了cpu序列号. */
        public static String mechineid = "";

        /** 基站定位字符串（经纬度）. */
        public static String cls = "";

        /** GPS定位字符串（经纬度）. */
        public static String gls = "";

        /** 网络类型. #ITypeDef */
        public static int netType = 0;

        /** 是否双卡双待机. */
        public static boolean isDoubleSimcard = false;

        public static boolean isInHandSet = false;

        /** 网络类型字串. */
        // public static String netTypeSubType;
    }

    public static class AppInfo
    {
        public static String Version = BuildConfig.VERSION_NAME;
        public static String Channel = "10000";

        static
        {
            // // 打包时从project.properties文件${project.package.name}预编译过程读取
            //
            // // #define clientversion='${project.package.name}'
            // // #ifdef clientversion
            // // #expand Version = "%clientversion%";
            // // #else
            // Version = "1.0.1";
            // // #endif
            //
            // if (Version != null)
            // {
            // try
            // {
            // // #define Areacode='${project.package.name.areacode}' //地区码 用来放开部分代码和功能
            // String[] splitstrs = Version.split("_");
            // } catch (Exception e)
            // {
            // }
            // }

            // Version = PhoneUtil.getAppVersionName();
        }
    }

    /**
     * 初始化渠道号<br>
     * 读asset目录下td文件
     */
    public static void initTDInfo()
    {
        // if (!PUBLISH)
        // {
        // return;
        // }

        Map<String, String> infos = new HashMap<String, String>();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader sr = null;
        try
        {
            is = application.getAssets().open("td");
            isr = new InputStreamReader(is);
            sr = new BufferedReader(isr);
            String line = null;
            while ((line = sr.readLine()) != null)
            {
                line = line.trim();
                String[] pair = line.split("=");
                if (pair != null && pair.length > 1)
                {
                    String key = pair[0];
                    String value = pair[1];
                    infos.put(key, value);
                }
            }
            String channelcode = infos.get("channelcode");
            if (!TextUtils.isEmpty(channelcode))
            {
                AppInfo.Channel = channelcode;
            }
            // String lc = infos.get("lc");
            // if (!TextUtils.isEmpty(lc))
            // {
            // licenceid = lc;
            // }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (sr != null)
                {
                    sr.close();
                }
                if (isr != null)
                {
                    isr.close();
                }
                if (is != null)
                {
                    is.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static boolean isMount()
    {
        return new File(RT.mLocalExternalPath).canWrite();
    }

    private static Handler mHandler;

    public static Handler getMainHandler()
    {
        if (mHandler == null)
        {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    private static boolean appIsBackground = false;

    /** 应用当前是否是后台状态 */
    public static boolean isAppIsBackground()
    {
        return appIsBackground;
    }

    public static void setAppIsBackground(boolean appIsBackground)
    {
        RT.appIsBackground = appIsBackground;
    }

    // -------------------------------------------------

    public JSONObject makeConfigJson()
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("DEBUG", DEBUG);
            json.put("IMAGELOG", IMAGELOG);
            json.put("PUBLISH", PUBLISH);
        } catch (JSONException e)
        {
        }
        return json;
    }

    /**
     * 加载、初始化设置
     */
    private void loadConfig()
    {
        // if (loadConfigOriginal() == null)
        {
            saveConfigOriginal();// 保存代码中原始设置
        }

        JSONObject configjson = null;
        String savestr = PreferenceHelper.ins().getStringShareData("RTCONFIG", "");
        if (!StringUtil.isEmpty(savestr))
        {
            try
            {
                JSONObject j = new JSONObject(savestr);
                if (j != null && j.length() > 0)
                {
                    configjson = j;
                }
            } catch (Exception e)
            {
            }
        }

        if (configjson == null)
        {
            // 没有保存的设置
        } else
        {
            // 有保存的设置
            DEBUG = configjson.optBoolean("DEBUG", DEBUG);
            IMAGELOG = configjson.optBoolean("IMAGELOG", IMAGELOG);
            if (PUBLISH)
            {
                saveConfig();// 代码中是生产状态 强制保存设置为生产 不可切换
            } else
            {
                PUBLISH = configjson.optBoolean("PUBLISH", PUBLISH);
            }
        }
    }

    /**
     * 修改设置
     * 
     * @param debug
     * @param imagelog
     * @param publish
     */
    public void changeConfig(boolean debug, boolean imagelog, boolean publish)
    {
        DEBUG = debug;
        IMAGELOG = imagelog;
        PUBLISH = publish;
        saveConfig();
    }

    /**
     * 保存用户设置
     */
    private void saveConfig()
    {
        JSONObject json = makeConfigJson();
        PreferenceHelper.ins().storeShareStringData("RTCONFIG", json.toString());
        PreferenceHelper.ins().commit();
    }

    /**
     * 保存代码原始设置
     */
    private void saveConfigOriginal()
    {
        if ("release".equals(BuildConfig.BUILD_TYPE) && BuildConfig.BUILD_RELEASE)
        {
            // 正式编译发布，强制修改开关，避免发版时忘记修改配置
            // 需要release测试环境 可修改 【build.gradle】- buildTypes - release - BUILD_RELEASE - false
            DEBUG = false;
            IMAGELOG = false;
            PUBLISH = true;
        }
        JSONObject json = makeConfigJson();
        PreferenceHelper.ins().storeShareStringData("RTCONFIG_ORIGINAL", json.toString());
        PreferenceHelper.ins().commit();
    }

    /**
     * 读取代码原始设置
     * 
     * @return
     */
    public JSONObject loadConfigOriginal()
    {
        String savestr = PreferenceHelper.ins().getStringShareData("RTCONFIG_ORIGINAL", "");
        if (!StringUtil.isEmpty(savestr))
        {
            try
            {
                JSONObject json = new JSONObject(savestr);
                if (json != null && json.length() > 0)
                {
                    return json;
                }
            } catch (Exception e)
            {
            }
        }
        return null;
    }

    /**
     * 当前是原始设置
     * 
     * @return
     */
    public boolean isOriginalConfig()
    {
        boolean ret = false;
        JSONObject json = loadConfigOriginal();
        if (json != null)
        {
            boolean _DEBUG = json.optBoolean("DEBUG");
            boolean _IMAGELOG = json.optBoolean("IMAGELOG");
            boolean _PUBLISH = json.optBoolean("PUBLISH");
            if (_DEBUG == DEBUG && _IMAGELOG == IMAGELOG && _PUBLISH == PUBLISH)
            {
                ret = true;
            }
        }
        return ret;
    }

    public static boolean isFirstInstall()
    {
        // if (!RT.PUBLISH && RT.DEBUG)
        // return true;
        return isFirstInstallByVersion("");
    }

    public static boolean isFirstInstallByVersion(String version)
    {
        boolean ret = false;
        String key = "FirstInstall_" + version;

        ArrayList<String> versionlist = new ArrayList<>();
        try
        {
            String dataStr = PreferenceHelper.ins().getStringShareData(key, "[]");
            JSONArray ja = new JSONArray(dataStr);
            for (int i = 0; i < ja.length(); i++)
            {
                versionlist.add(ja.optString(i));
            }
        } catch (Exception ex)
        {
        }

        if (TextUtils.isEmpty(version))
        {
            // 全版本 是否首次安装
            if (versionlist == null || versionlist.size() == 0)
            {
                ret = true; // 任何版本记录都没有 就是首次安装
            } else
            {
                ret = false;
            }
        } else
        {
            // 指定版本是否首次安装
            ret = versionlist.contains(version);
        }

        if (ret)
        {
            try
            {
                if (versionlist == null)
                    versionlist = new ArrayList<>();
                if (TextUtils.isEmpty(version))
                    version = AppInfo.Version;
                versionlist.add(version);
                JSONArray ja = new JSONArray();
                for (String ver : versionlist)
                {
                    ja.put(ver);
                }
                PreferenceHelper.ins().storeShareStringData(key, ja.toString());
                PreferenceHelper.ins().commit();
            } catch (Exception ex)
            {
            }
        }

        return ret;
    }

    private String uuid;

    public String getUuid()
    {
        if (TextUtils.isEmpty(uuid))
        {
            this.uuid = DeviceConfig.getDeviceId(application);
        }
        return uuid;
    }
    // 测试环境账号
    public static final String appid = "d94f86d9f86b9b3b424f9b1c775588dc";
    // String secret = "7f084cdd7969ef54e4b6b3822eea24e2";
    public String getAppid(){

        return appid;
    }

}
