package org.rdengine.util;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import org.rdengine.log.DLOG;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ProcessUtil
{
    /**
     * 可用作清理手机内存
     * 
     * @param context
     */
    public static void killAllProcess(Context context)
    {
        // 拿到这个包管理器
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 拿到所有正在运行的进程信息
        List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        // 进行遍历，然后杀死它们
        int count = 0;
        for (RunningAppProcessInfo runningAppProcessInfo : list)
        {
            if (runningAppProcessInfo.processName.startsWith("com.mofang.mgassistant"))
            {
            } else
            {
                // activityManager.killBackgroundProcesses(runningAppProcessInfo.processName);

                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                if (runningAppProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE)// ----------根据重要性来kill
                {
                    String[] pkgList = runningAppProcessInfo.pkgList; // pkgList 得到该进程下运行的包名
                    for (int j = 0; j < pkgList.length; ++j)
                    {
                        DLOG.d("ProcessUtil", "killAllProcess kill pkgname=" + pkgList[j]);
                        activityManager.killBackgroundProcesses(pkgList[j]);
                        count++;
                    }
                }
            }
        }
        DLOG.d("ProcessUtil", "killAllProcess kill pkgcount=" + count);
    }

    public static String getProcessCount(Context context)
    {
        // 拿到这个包管理器
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 拿到所有正在运行的进程信息
        List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        return list.size() + "";
    }

    /**
     * 系统剩余可用内存
     * 
     * @param context
     * @return
     */
    public static long getAvailMemory(Context context)
    {
        // 拿到这个包管理器
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // new一个内存的对象
        MemoryInfo memoryInfo = new MemoryInfo();
        // 拿到现在系统里面的内存信息
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
        // return dataSizeFormat(memoryInfo.availMem);
    }

    /**
     * 获取已使用内存的比例 0～1.0F
     * 
     * @param context
     * @return
     */
    public static float getMemoryUsedProportion(Context context)
    {
        float ret = 0;
        try
        {
            long am = getAvailMemory(context);
            long tm = getTotalMemory(context);
            ret = ((tm - am) * 1.0F / tm);
        } catch (Exception e)
        {
            ret = 0;
        }
        return ret;
    }

    /**
     * 系统总内存
     * 
     * @param context
     * @return
     */
    public static long getTotalMemory(Context context)
    {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try
        {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString)
            {
                // DLOG.i(str2, num + "/t");
            }

            long v = Long.valueOf(arrayOfString[1]).longValue();
            initial_memory = v * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e)
        {
        }
        // return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
        return initial_memory;
    }

    public static String dataSizeFormat(long size)
    {
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024)
        {
            return size + "byte";
        } else if (size < (1 << 20)) // 左移20位，相当于1024 * 1024
        {
            float kSize = size >> 10; // 右移10位，相当于除以1024
            return formater.format(kSize) + "KB";
        } else if (size < (1 << 30)) // 左移30位，相当于1024 * 1024 * 1024
        {
            float mSize = size >> 20; // 右移20位，相当于除以1024再除以1024
            return formater.format(mSize) + "MB";
        } else if (size < (1 << 40))
        {
            float gSize = size >> 30;
            return formater.format(gSize) + "GB";
        } else
        {
            return "size : error";
        }
    }

    public static String getSizeFromKB(long kSize)
    {
        return dataSizeFormat(kSize << 10);
    }

}
