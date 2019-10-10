package org.rdengine.net;

//import org.apache.http.HttpEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.rdengine.runtime.PreferenceHelper;
import org.rdengine.runtime.RT;
import org.rdengine.util.PhoneUtil;
import org.rdengine.util.StringUtil;

public class Network
{

    public static boolean IS_ONLINE = true;

    public static final String CMWAP_PROXY = "10.0.0.172";
    public static final int CMWAP_PORT = 80;

    private static final String TAG = "network";

    public int connecTime = 1;

    public static boolean isFirstTip = true;

    /**
     * 标识网络状态
     * 
     * @author moon
     */
    public enum NetworkMode
    {
        OFF_LINE_MODE,

        NO_AVALIABLE_NETWORK,

        NET_WORK_OK;
    }

    /**
     * 判断是否有可用网络
     * 
     * @return
     */
    public static NetworkMode getNetworkState()
    {
        if (RT.PhoneInfo.netType == ITypeDef.DM_NETWORK_TYPE_NONE)
        {
            return NetworkMode.NO_AVALIABLE_NETWORK;
        } else
        {
            if (!IS_ONLINE)
            {
                return NetworkMode.OFF_LINE_MODE;
            }
        }
        return NetworkMode.NET_WORK_OK;
    }

    // DefaultHttpClient client = null;
    // HttpEntity entity = null;

    private boolean mUseProxy = true;

    private int mRetryConnectTimes = 0;

    public static boolean sUserDefaultProxy = true;

    private static String net_type_string;

    /**
     * 是否移动网络连接
     * 
     * @return true, if is connect by mobile
     */
    public static boolean isConnectByMobile()
    {
        if (RT.PhoneInfo.netType == ITypeDef.DM_NETWORK_TYPE_NONE
                || RT.PhoneInfo.netType == ITypeDef.DM_NETWORK_TYPE_WIFI)
            return false;
        else return true;
    }

    static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    // content://telephony/carriers/preferapn

    // 获取Mobile网络下的cmwap、cmnet

    public static String net_apn = "start";

    public static int netWorkState(Context context)
    {
        int type = ITypeDef.DM_NETWORK_TYPE_NONE;

        try
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null)
            {
                net_type_string = info.getSubtypeName();
                // if (info.getType() == ConnectivityManager.TYPE_WIFI)

                if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == 7 || info.getType() == 8
                        || info.getType() == 9 || info.getType() == 6)
                {
                    type = ITypeDef.DM_NETWORK_TYPE_WIFI;
                    net_type_string = "WIFI";
                }
            }

            if (type != ITypeDef.DM_NETWORK_TYPE_WIFI && info != null)
            {

                String apn = null;
                String proxy = "";
                if (PhoneUtil.hasJellyBeanMR1())
                {
                    // android4.2封了apn权限
                    apn = info.getExtraInfo();
                    proxy = android.net.Proxy.getDefaultHost();
                } else
                {
                    Cursor cursor = null;
                    try
                    {
                        cursor = context.getContentResolver().query(PREFERRED_APN_URI, new String[]
                        { "_id", "apn", "type", "proxy" }, null, null, null);
                        cursor.moveToFirst();
                        int counts = cursor.getCount();
                        if (RT.PhoneInfo.isDoubleSimcard || counts > 1)
                        {
                            apn = info.getExtraInfo();
                            proxy = android.net.Proxy.getDefaultHost();
                        } else if (counts != 0)
                        {
                            // 适配平板外挂3G模块情况
                            if (!cursor.isAfterLast())
                            {
                                apn = cursor.getString(1);
                                proxy = cursor.getString(3);

                            } else
                            {
                                // 适配中国电信定制机,如海信EG968,上面方式获取的cursor为空，所以换种方式
                                Cursor c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null,
                                        null);
                                try
                                {
                                    c.moveToFirst();
                                    apn = c.getString(c.getColumnIndex("user"));
                                    // if ("ctnet".equalsIgnoreCase(user))
                                    // {
                                    // type = ITypeDef.DM_NETWORK_TYPE_3G;
                                    // }
                                } catch (Exception e)
                                {
                                } finally
                                {
                                    if (c != null)
                                        c.close();
                                }
                            }
                        } else
                        {
                            type = ITypeDef.DM_NETWORK_TYPE_3G;
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    } finally
                    {

                        try
                        {
                            if (cursor != null)
                                cursor.close();
                        } catch (Exception e)
                        {
                        }
                    }
                }
                if (apn != null)
                {
                    // DLOG.i("network", "apn=" + apn);
                    // 777、ctnet
                    // 都是中国电信定制机接入点名称,中国电信的接入点：Net、Wap都采用Net即非代理方式联网即可
                    // internet 是模拟器上模拟接入点名称
                    if (!StringUtil.isEmpty(proxy) && proxy.trim().contains(CMWAP_PROXY))
                    {
                        // 移动、联通的wap回走代理 代理IP都是10.0.0.172 优先判断代理地址
                        type = ITypeDef.DM_NETWORK_TYPE_CMWAP;
                        if (RT.DEBUG)
                        {
                            Log.d("network", "手机网络有代理地址 10.0.0.172");
                        }
                    } else if ("cmnet".equalsIgnoreCase(apn) || "3gnet".equalsIgnoreCase(apn)
                            || "uninet".equalsIgnoreCase(apn) || "#777".equalsIgnoreCase(apn)
                            || "ctnet".equalsIgnoreCase(apn) || "internet".equalsIgnoreCase(apn))
                    {
                        type = ITypeDef.DM_NETWORK_TYPE_3G;

                    } else if ("ctwap".equalsIgnoreCase(apn))
                    {
                        type = ITypeDef.DM_NETWORK_TYPE_CTWAP;
                    } else if ("cmwap".equalsIgnoreCase(apn) || "3gwap".equalsIgnoreCase(apn)
                            || "uniwap".equalsIgnoreCase(apn)
                            || (apn != null && apn.toLowerCase().indexOf("wap") != -1))
                    {
                        type = ITypeDef.DM_NETWORK_TYPE_CMWAP;
                    } else
                    {
                        type = ITypeDef.DM_NETWORK_TYPE_3G;
                    }
                }
                // DLOG.d(Network.class.getSimpleName(), "networkinfo>>" + info.toString());
                net_type_string = info.getSubtypeName();
                // DLOG.d(Network.class.getSimpleName(), "net_type_string>>" + net_type_string);
                // 1.联通3g 3GWAP 发送成 CMWAP
                // 2.联通2g UNIWAP 发送成CMWAP
                if (type == ITypeDef.DM_NETWORK_TYPE_3G)
                {
                    // 根据子类型判断一次2g
                    if (net_type_string != null && net_type_string.toLowerCase().indexOf("cdma") != -1)
                    {
                        if (net_type_string.toLowerCase().indexOf("evdo") != -1)
                        {
                            type = ITypeDef.DM_NETWORK_TYPE_3G;// "3G";
                        } else
                        {
                            type = ITypeDef.DM_NETWORK_TYPE_2G;
                        }
                    } else
                    {
                        if (net_type_string != null && (net_type_string.toLowerCase().indexOf("gprs") != -1
                                || net_type_string.toLowerCase().indexOf("edge") != -1))
                        {
                            type = ITypeDef.DM_NETWORK_TYPE_2G; // type = "GPRS";
                        }
                    }
                }

                if (type == ITypeDef.DM_NETWORK_TYPE_NONE)
                {
                    type = ITypeDef.DM_NETWORK_TYPE_3G;
                }
            }
            if (type == ITypeDef.DM_NETWORK_TYPE_NONE && info != null && info.isConnected())
            {
                // 如果是usb共享上网可能没有网络类型,默认wifi
                type = ITypeDef.DM_NETWORK_TYPE_WIFI;
            }
            if (type != RT.PhoneInfo.netType)
            {
                // DLOG.d(Network.class.getSimpleName(), "find network type>>" + type);
                // 切换网络连接
                RT.PhoneInfo.netType = type;
                sUserDefaultProxy = true;
            }
            if (type != ITypeDef.DM_NETWORK_TYPE_WIFI)
            {
                // LOG.d("GlobalEvent", "here>>>>>>>>>>>>");
                if (PreferenceHelper.ins() != null && type != ITypeDef.DM_NETWORK_TYPE_NONE)
                {
                    int f = PreferenceHelper.ins().getIntShareData("con_mobile_net_first", -1);
                    if (f == -1)
                    {
                        PreferenceHelper.ins().storeIntShareData("con_mobile_net_first", 1);
                        PreferenceHelper.ins().commit();
                    }
                }
            }

            final String apn1 = getNetApn(type);
            boolean cSC = false;
            boolean getuniphn = false;
            // 检测imsi的改变 以及phn 只在切换到 wap和net 才检测
            if ("wap".equals(apn1) || "net".equals(apn1))
            {

            }

            if (!"".equals(apn1))
            {
                net_apn = apn1;
            } else
            {
                // Log.e("cccmaxN", "不做处理 上次apn=[" + net_apn + "]本次apn=[" + apn1 + "]");
            }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }

        // LOG.d("GlobalEvent", "net type:"+net_state);
        return type;
    }

    /**
     * 给埋点报告网络类型
     * 
     * @return the net type string
     */
    public static String getNetTypeString()
    {
        if (!StringUtil.isEmpty(net_type_string))
        {
            return net_type_string;
        } else
        {
            switch (RT.PhoneInfo.netType)
            {
            case ITypeDef.DM_NETWORK_TYPE_WIFI :
                return "WIFI";
            case ITypeDef.DM_NETWORK_TYPE_2G :
                return "GPRS";
            case ITypeDef.DM_NETWORK_TYPE_3G :
                return "3G";
            case ITypeDef.DM_NETWORK_TYPE_CMWAP :
                return "CMWAP";
            case ITypeDef.DM_NETWORK_TYPE_CTWAP :
                return "CTWAP";
            case ITypeDef.DM_NETWORK_TYPE_NONE :
                return "NONE";
            default:
                return "UNKNOWN";

            }
        }
    }

    /**
     * 根据NetType判断APN
     * 
     * @return the net Apn string
     */
    public static String getNetApn(int type)
    {
        switch (type)
        {
        case ITypeDef.DM_NETWORK_TYPE_WIFI :
        case ITypeDef.DM_NETWORK_TYPE_WIRED :
            return "wifi";
        case ITypeDef.DM_NETWORK_TYPE_2G :
        case ITypeDef.DM_NETWORK_TYPE_3G :
            return "net";
        case ITypeDef.DM_NETWORK_TYPE_CTWAP :
        case ITypeDef.DM_NETWORK_TYPE_CMWAP :
            return "wap";
        case ITypeDef.DM_NETWORK_TYPE_NONE :
        case ITypeDef.DM_NETWORK_TYPE_UNKNOWN :
        default:
            return "";
        }
    }
}
