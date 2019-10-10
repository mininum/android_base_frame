package org.rdengine.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import org.rdengine.log.DLOG;
import org.rdengine.runtime.RT;
import org.rdengine.runtime.RT.PhoneInfo;
import org.rdengine.widget.ToastHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 设备相关工具类.
 */
public class PhoneUtil
{

    /**
     * 获取基站定位,不存在返回-1. <br>
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     *
     * @return the gsm cell location
     */
    private static String getGsmCellLocation(Context context)
    {
        if (context != null)
        {
            try
            {
                TelephonyManager tm = (TelephonyManager) context.getApplicationContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
                CellLocation gcl = tm.getCellLocation();
                if (gcl != null)
                {
                    if (gcl instanceof GsmCellLocation)
                    {
                        return "latitude:".concat(String.valueOf(((GsmCellLocation) gcl).getCid()))
                                .concat(",longitude:").concat(String.valueOf(((GsmCellLocation) gcl).getLac()));
                    } else if (gcl instanceof CdmaCellLocation)
                    {
                        return "latitude:".concat(String.valueOf(((CdmaCellLocation) gcl).getBaseStationLatitude()))
                                .concat(",longitude:")
                                .concat(String.valueOf(((CdmaCellLocation) gcl).getBaseStationLongitude()));
                    }

                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    // private static Context getMCtx()
    // {
    // return RT.application;
    // }

    /**
     * 获取GPS定位.
     *
     * @return the gps
     */
    private static String getGPS(Context context)
    {
        if (context != null)
        {
            LocationManager locationManager = (LocationManager) context.getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
            // 查找到服务信息
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
            String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
            if (provider != null)
            {
                try
                {
                    Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
                    if (location != null)
                    {
                        return "latitude:".concat(String.valueOf(location.getLatitude())).concat(",longitude:")
                                .concat(String.valueOf(location.getLongitude()));
                    }
                } catch (SecurityException se)
                {
                    se.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        return " ";
    }

    /**
     * 规整手机号，去掉+8613XXXXXXXXX 前缀+86
     */
    public static void normalizePhoneNum()
    {
        String number = PhoneInfo.phoneNumber;
        if (!StringUtil.isEmpty(number) && number.length() == 14 && number.startsWith("+86"))
        {
            PhoneInfo.phoneNumber = number.substring(3);
        }
    }

    public static String phnfromSim = "";

    /**
     * 获取手机信息.
     * 
     * @return the phone set
     */
    public static void getPhoneSet(Context context)
    {
        if (context != null)
        {

            try
            {
                TelephonyManager tm = (TelephonyManager) context.getApplicationContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String dId = null, subId = null;
                if (context.getApplicationContext().checkCallingOrSelfPermission(
                        "android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED)
                {
                    dId = tm.getImei();
                    if (TextUtils.isEmpty(dId))
                        dId = tm.getMeid();// CDMA手机的标识
                    if (TextUtils.isEmpty(dId))
                        dId = tm.getDeviceId();
                    subId = tm.getSubscriberId();
                }
                PhoneInfo.imei = dId == null ? "" : dId;
                PhoneInfo.imsi = subId == null ? "" : subId;
                if (TelephonyManager.SIM_STATE_ABSENT != tm.getSimState())
                {
                    if (context.getApplicationContext().checkCallingOrSelfPermission(
                            "android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED)
                    {
                        PhoneInfo.phoneNumber = tm.getLine1Number();
                        PhoneInfo.phoneNumber = StringUtil.filterPhoneNumber(PhoneInfo.phoneNumber);
                    }
                    if (PhoneInfo.phoneNumber == null)
                        PhoneInfo.phoneNumber = "";
                } else
                {
                    PhoneInfo.phoneNumber = "";
                }
                phnfromSim = PhoneInfo.phoneNumber;// 从设备中获取的手机号 做个备份

                normalizePhoneNum();
                PhoneInfo.userAgent = Build.MANUFACTURER.trim() + Build.MODEL.trim();
                PhoneInfo.devi = dId == null ? "" : dId;
                PhoneInfo.mechineid = getSerialNumber();
                PhoneInfo.isDoubleSimcard = isDoubleSimcard(context);
                // PhoneInfo.gls = getGPS(context); //暂不需要GPS
                // PhoneInfo.cls = String.valueOf(getGsmCellLocation(context));

            } catch (Throwable e)
            {
                e.printStackTrace();
            }

        }
    }

    private static boolean isDoubleSimcard(Context context)
    {
        boolean isDouble = false;
        if (context != null)
        {
            Method method = null;
            Object result_0 = null;
            Object result_1 = null;
            try
            {
                isDouble = true;
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                // 只要在反射getSimStateGemini 这个函数时报了错就是单卡手机（这是我自己的经验，不一定全正确）
                method = TelephonyManager.class.getMethod("getSimStateGemini", new Class[]
                { int.class });
                // 获取SIM卡1
                result_0 = method.invoke(tm, new Object[]
                { Integer.valueOf(0) });
                // 获取SIM卡1
                result_1 = method.invoke(tm, new Object[]
                { Integer.valueOf(1) });
            } catch (SecurityException e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
                // System.out.println("1_ISSINGLETELEPHONE:"+e.toString());
            } catch (NoSuchMethodException e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
                // System.out.println("2_ISSINGLETELEPHONE:"+e.toString());
            } catch (IllegalArgumentException e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
            } catch (InvocationTargetException e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
            } catch (Exception e)
            {
                isDouble = false;
                // DLOG.w("isDoubleSimcard", e);
                // e.printStackTrace();
            }
            if (isDouble)
            {
                // 保存为双卡手机
                // editor.putBoolean(ISDOUBLE, true);
                // RT.PhoneInfo.isDoubleSimcard = true;
                // 保存双卡是否可用
                // 如下判断哪个卡可用.双卡都可以用
                if (result_0.toString().equals("5") && result_1.toString().equals("5"))
                {
                    // editor.putBoolean(SIMCARD_1, true);
                    // editor.putBoolean(SIMCARD_2, true);
                } else if (!result_0.toString().equals("5") && result_1.toString().equals("5"))
                {
                    // 卡二可用
                    // editor.putBoolean(SIMCARD_1, false);
                    // editor.putBoolean(SIMCARD_2, true);
                } else if (result_0.toString().equals("5") && !result_1.toString().equals("5"))
                {// 卡一可用

                    // editor.putBoolean(SIMCARD_1, true);
                    // editor.putBoolean(SIMCARD_2, false);
                } else
                {// 两个卡都不可用(飞行模式会出现这种种情况)
                 // editor.putBoolean(SIMCARD_1, false);
                 // editor.putBoolean(SIMCARD_2, false);
                }
            }

        }
        return isDouble;
    }

    /**
     * 进入系统默认短信页面发送短信，给一个人或多个人
     * 
     * @param context
     * @param phoneNumbers
     *            短话号码的List
     * @param sms_body
     *            要发送的短信内容
     */
    public static void sendSMS(Context context, List<String> phoneNumbers, String sms_body)
    {
        try
        {
            // 短信发送多个号码的分隔符 话说三星手机的分隔符是【,】
            String separator = ";";
            if (Build.MANUFACTURER.equalsIgnoreCase("samsung"))
                separator = ",";

            String smsto = "smsto:";
            for (int i = 0; i < phoneNumbers.size(); i++)
            {
                if (i != 0)
                    smsto = smsto.concat(separator);
                smsto = smsto.concat(phoneNumbers.get(i));
            }
            // smsto = smsto.concat(phnlist.get(0));
            Uri smsToUri = Uri.parse(smsto);
            Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
            intent.putExtra("sms_body", sms_body);
            context.startActivity(intent);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets the device id.
     *
     * @return the device id
     */
    private static String getDeviceId(Context context)
    {
        String android_id = "";
        if (context != null)
        {
            android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        }
        return android_id;
    }

    /**
     * cpu序列号
     *
     * @return the serial number
     */
    public static String getSerialNumber()
    {
        String serial = null;
        try
        {
            if (hasHoneycomb())
            {
                try
                {
                    serial = getSerialNum3();
                } catch (Exception e)
                {
                }
            }
            if (StringUtil.isEmpty(serial))
            {
                try
                {
                    Class<?> c = Class.forName("android.os.SystemProperties");
                    Method get = c.getMethod("get", String.class);
                    serial = (String) get.invoke(c, "ro.serialno");
                } catch (Exception e)
                {
                }
            }
            if (StringUtil.isEmpty(serial))
            {
                try
                {
                    serial = getCPUSerial();
                } catch (Exception e)
                {
                }
            }
        } catch (Exception ignored)
        {
        }
        return serial;
    }

    @SuppressLint("NewApi")
    public static String getSerialNum3()
    {
        return Build.SERIAL;
    }

    /**
     * 获取CPU序列号
     *
     * @return CPU序列号(16位) 读取失败为"0000000000000000"
     */
    public static String getCPUSerial()
    {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";
        try
        {
            // 读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            // 查找CPU序列号
            for (int i = 1; i < 100; i++)
            {
                str = input.readLine();
                if (str != null)
                {
                    // 查找到序列号所在行
                    if (str.indexOf("Serial") > -1)
                    {
                        // 提取序列号
                        strCPU = str.substring(str.indexOf(":") + 1, str.length());
                        // 去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else
                {
                    // 文件结尾
                    break;
                }
            }
        } catch (IOException ex)
        {
            // 赋予默认值
        }
        return cpuAddress;
    }

    /**
     * 检查耳机
     *
     * @return
     */
    public static boolean checkHeadSetState()
    {
        String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";
        FileReader file = null;
        try
        {
            file = new FileReader(HEADSET_STATE_PATH);
            char[] buffer = new char[1024];
            int len = file.read(buffer, 0, 1024);
            int headsetState = Integer.valueOf((new String(buffer, 0, len)).trim());
            // DLOG.e("headSet", "is useing handSet >>" + (headsetState == 1));
            if (headsetState == 1)
            {
                return true;
            } else
            {
                return false;
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        } finally
        {
            if (file != null)
            {
                try
                {
                    file.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("NewApi")
    public static void enableStrictMode()
    {
        try
        {
            if (hasGingerbread())
            {
                android.os.StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new android.os.StrictMode.ThreadPolicy.Builder()
                        .detectAll().penaltyLog();
                android.os.StrictMode.VmPolicy.Builder vmPolicyBuilder = new android.os.StrictMode.VmPolicy.Builder()
                        .detectAll().penaltyLog();

                if (hasHoneycomb())
                {
                    threadPolicyBuilder.penaltyFlashScreen();
                    // vmPolicyBuilder.setClassInstanceLimit(ImageGridActivity.class, 1).setClassInstanceLimit(
                    // ImageDetailActivity.class, 1);
                }
                android.os.StrictMode.setThreadPolicy(threadPolicyBuilder.build());
                android.os.StrictMode.setVmPolicy(vmPolicyBuilder.build());
            }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public static class MemInfo
    {
        public long availMem;
        public long threshold;
        public boolean lowMemory;
        public long maxMemory;// JVM
        public long freeMemory;// JVM

        public String toString()
        {
            return "".concat(String.valueOf(availMem)).concat(":").concat(String.valueOf(freeMemory)).concat(":")
                    .concat(String.valueOf(maxMemory)).concat(":").concat(String.valueOf(threshold)).concat(":")
                    .concat(String.valueOf(lowMemory));
        }
    }

    public static MemInfo availidMem(Context ct)
    {

        MemInfo mem = new MemInfo();
        try
        {
            mem.maxMemory = Runtime.getRuntime().maxMemory();
            mem.freeMemory = Runtime.getRuntime().freeMemory();
            ActivityManager am = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo mi = new MemoryInfo();
            am.getMemoryInfo(mi);
            mem.availMem = mi.availMem;
            mem.threshold = mi.threshold;
            mem.lowMemory = mi.lowMemory;
            // if (RT.DEBUG)
            // {
            // Log.e("max", "mi.availMem:" + mem.availMem + "mi.threshold:" + mem.threshold + "mi.lowMemory:"
            // + mem.lowMemory + "Runtime.getRuntime().maxMemory():" + mem.maxMemory
            // + "Runtime.getRuntime().freeMemory():" + mem.freeMemory);
            // }
        } catch (Throwable e)
        {
            e.printStackTrace();
        }
        return mem;

    }

    // public static String getTestDeviceId()
    // {
    // Class clz = null, cli = null, clt = null;
    // Method getServiceMeth = null, asInterface = null, getDeviceid = null;
    // try
    // {
    // clz = Class.forName("android.os.ServiceManager");
    // clt = Class.forName("com.android.internal.telephony.IPhoneSubInfo");
    // cli = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub");
    // // getServiceMeth = clz.getMethod("getService", new Class[]
    // // { String.class });
    // getServiceMeth = clz.getMethod("getService", String.class);
    // asInterface = cli.getMethod("asInterface", IBinder.class);
    // getDeviceid = clt.getMethod("getDeviceId", null);
    // } catch (NoSuchMethodException e)
    // {
    // getServiceMeth = null;
    // } catch (ClassNotFoundException e)
    // {
    // e.printStackTrace();
    // }
    // if (clz == null || getServiceMeth == null)
    // {
    // return "";
    // }
    // // IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    // Object isf = null;
    // try
    // {
    // // isf = IPhoneSubInfot.Stub.asInterface((IBinder) getServiceMeth.invoke(clz, "iphonesubinfo"));
    // isf = asInterface.invoke(cli, (IBinder) getServiceMeth.invoke(clz, "iphonesubinfo"));
    // } catch (IllegalArgumentException e)
    // {
    // e.printStackTrace();
    // } catch (IllegalAccessException e)
    // {
    // e.printStackTrace();
    // } catch (InvocationTargetException e)
    // {
    // e.printStackTrace();
    // }
    // try
    // {
    // Object ret = getDeviceid.invoke(isf, null);
    // return (String) ret;
    // } catch (IllegalArgumentException e)
    // {
    // e.printStackTrace();
    // } catch (IllegalAccessException e)
    // {
    // e.printStackTrace();
    // } catch (InvocationTargetException e)
    // {
    // e.printStackTrace();
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // return "";
    // }

    public static int getStatusBarHeight(Context context)
    {

        Class c = null;

        Object bj = null;

        Field field = null;

        int x = 0, statusBarHeight = 0;

        try
        {

            c = Class.forName("com.android.internal.R$dimen");

            bj = c.newInstance();

            field = c.getField("status_bar_height");

            x = Integer.parseInt(field.get(bj).toString());

            statusBarHeight = context.getResources().getDimensionPixelSize(x);

        } catch (Exception e1)
        {

            e1.printStackTrace();

        }

        return statusBarHeight;

    }

    // 获取指定Activity的截屏，保存到png文件
    public static Bitmap takeScreenShot(Activity activity)
    {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        // DLOG.i("TAG", "" + statusBarHeight);

        // 获取屏幕长和高
        int width = 0;
        int height = 0;
        int[] size = getPhoneScreenSize(activity);
        width = size[0];
        height = size[1];
        // 去掉标题栏
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    private static int[] ScreenSize = new int[]
    { 0, 0 };

    /**
     * 获取屏幕宽高
     *
     * @param activity
     *            the activity
     * @return the phone screen size
     */
    @SuppressLint("NewApi")
    public static int[] getPhoneScreenSize(Activity activity)
    {
        if (ScreenSize[0] > 0 && ScreenSize[1] > 0)
        {
            return ScreenSize;
        }
        int width = 0;
        int height = 0;
        if (PhoneUtil.hasHoneycombMR2())
        {
            Point size = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(size);
            // activity.getWindowManager().getDefaultDisplay().getRealSize(size);//真是尺寸 包含虚拟按键
            width = size.x;
            height = size.y;
        } else
        {
            width = activity.getWindowManager().getDefaultDisplay().getWidth();
            height = activity.getWindowManager().getDefaultDisplay().getHeight();
        }
        ScreenSize[0] = width;
        ScreenSize[1] = height;
        return ScreenSize;
    }

    /**
     * 得到屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context)
    {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int dipToPixel(float dp, Context mContext)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                mContext.getResources().getDisplayMetrics());
    }

    public static float pixelToDip(int pixel, Context mContext)
    {
        float scale = mContext.getResources().getDisplayMetrics().density;
        // float dp = (float) (pixel - 0.5f) / scale;
        float dp = (int) (pixel / scale + 0.5f * (pixel >= 0 ? 1 : -1));
        return dp;

    }

    /**
     * @param activity
     * @return 判断当前手机是否是全屏
     */
    public static boolean isFullScreen(Activity activity)
    {
        int flag = activity.getWindow().getAttributes().flags;
        if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public interface ScreenDifine
    {
        public static final int LowDpi240 = 1;
        public static final int midDpi480 = 2;
        public static final int highDpi720 = 3;
    }

    public static int getPhoneScreenDifine(Activity activity)
    {
        int size[] = getPhoneScreenSize(activity);
        int min = Math.min(size[0], size[1]);
        int ret;
        if (min >= 240 && min < 480)
        {
            ret = ScreenDifine.LowDpi240;
        } else if (min >= 480 && min < 720)
        {
            ret = ScreenDifine.midDpi480;
        } else if (min >= 720)
        {
            ret = ScreenDifine.highDpi720;
        } else
        {
            ret = ScreenDifine.LowDpi240;
        }

        return ret;
    }

    public static boolean isMIUI()
    {
        String line;
        BufferedReader input = null;
        try
        {
            Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            // DLOG.d("cccmax", "isMIUI : name = " + line);
        } catch (IOException ex)
        {
            return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi");
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                    // Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return !StringUtil.isEmpty(line);
    }

    public static int getMIUIVersion()
    {
        String line;
        BufferedReader input = null;
        try
        {
            Process p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            DLOG.i("getMIUIVersion", line);
        } catch (IOException ex)
        {
            return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi") ? 0 : -1;
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                }
            }
        }
        int ret = -1;
        if (StringUtil.isEmpty(line))
            ret = -1;
        else if (line.length() >= 2)
        {
            try
            {
                ret = Integer.parseInt(line.substring(1));
            } catch (Exception e)
            {
                ret = -1;
            }
        }
        return ret;
    }

    public static void toMarket(Activity activity, String packagename)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // 参数是应用程序的包名
            intent.setData(Uri.parse("market://details?id=" + packagename));
            // 通过隐式意图激活activity
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e)
        {
            ToastHelper.showToast("抱歉，你没有安装应用市场");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static int getAppVersionCode()
    {
        try
        {
            PackageInfo pi = RT.application.getPackageManager().getPackageInfo(RT.application.getPackageName(), 0);
            int ret = pi.versionCode;
            return ret;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppVersionName()
    {
        try
        {
            PackageInfo pi = RT.application.getPackageManager().getPackageInfo(RT.application.getPackageName(), 0);
            String ret = pi.versionName;
            return ret;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNotLowDpiScreen(Activity context)
    {
        return PhoneUtil.getPhoneScreenDifine(context) > ScreenDifine.midDpi480;
    }

    /**
     * 是否可以用低配图模式
     *
     * @param context
     * @return
     */
    public static boolean isLowImagePhone(Context context)
    {
        int w = getScreenWidth(context);
        int h = getScreenHeight(context);
        int min = Math.min(w, w);
        if (min <= 480 && UiUtil.getMaxTextureSize(context) <= 4096
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
        {
            // 屏幕小于等于480P
            // GPU图片支持小于等于4096分辨率
            // 系统低于4.4
            return true;
        }
        return false;
    }

    /**
     * 返回当前的应用是否处于前台显示状态
     *
     * @return true 当前应用在前台，false 应用进入后台
     */
    public static boolean isForeApp()
    {
        try
        {
            String packageNmae = RT.application.getPackageName();
            // _context是一个保存的上下文
            ActivityManager am = (ActivityManager) RT.application.getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> applist = am.getRunningAppProcesses();
            if (applist.size() == 0)
                return false;
            for (ActivityManager.RunningAppProcessInfo appProcess : applist)
            {
                // DLOG.d("cccmax", Integer.toString(appProcess.importance));
                // DLOG.d("cccmax", appProcess.processName);
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && appProcess.processName.equals(packageNmae))
                {
                    return true;
                }
            }
        } catch (Exception ex)
        {
        }
        return false;
    }

    public static boolean hasFroyo()
    {

        return Build.VERSION.SDK_INT >= 8;// Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread()
    {
        return Build.VERSION.SDK_INT >= 9;// Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasGingerbreadMR1()
    {
        return Build.VERSION.SDK_INT >= 10;// Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static boolean hasHoneycomb()
    {
        return Build.VERSION.SDK_INT >= 11; // Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1()
    {
        return Build.VERSION.SDK_INT >= 12; // Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasHoneycombMR2()
    {
        return Build.VERSION.SDK_INT >= 13; // Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasIceCreamSandwich()
    {
        return Build.VERSION.SDK_INT >= 14;// Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    public static boolean hasIceCreamSandwichMR1()
    {
        return Build.VERSION.SDK_INT >= 15;// Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    /** 4.1 */
    public static boolean hasJellyBean()
    {
        return Build.VERSION.SDK_INT >= 16;
    }

    /** 4.2 */
    public static boolean hasJellyBeanMR1()
    {
        // Build.VERSION_CODES.
        return Build.VERSION.SDK_INT >= 17;
    }

    /** 4.3 */
    public static boolean hasJellyBeanMR2()
    {
        return Build.VERSION.SDK_INT >= 18;
    }

    /** 4.4 */
    public static boolean hasKitkat()
    {
        return Build.VERSION.SDK_INT >= 19;
    }

    /** 5.0 */
    public static boolean hasLollipop()
    {
        return Build.VERSION.SDK_INT >= 21;// Build.VERSION_CODES.LOLLIPOP;
    }

    /** 5.1 */
    public static boolean hasLollipopMR1()
    {
        return Build.VERSION.SDK_INT >= 22;
    }

    /** 6.0 */
    public static boolean hasMarshmallow()
    {
        return Build.VERSION.SDK_INT >= 23;
    }

    /** 7.0 */
    public static boolean hasNougat()
    {
        return Build.VERSION.SDK_INT >= 24;
    }

    /** 8.0 */
    public static boolean hasOreo()
    {
        return Build.VERSION.SDK_INT >= 26;
    }

    /** 9.0 */
    public static boolean hasPie()
    {
        return Build.VERSION.SDK_INT >= 28;
    }

}