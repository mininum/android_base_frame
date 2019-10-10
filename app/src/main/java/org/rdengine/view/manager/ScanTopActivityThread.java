package org.rdengine.view.manager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.rdengine.log.DLOG;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测当前最上层Activity的线程
 * 
 * @author yangyu
 */
public class ScanTopActivityThread extends Thread
{

    private Context mContext;

    public ScanTopActivityThread(Context context)
    {
        mContext = context;
        isListening = true;

        getHomes();
    }

    boolean isListening = false;

    private int lastAPKNameHashCode = 0;

    @Override
    public void run()
    {
        String apkname = "";
        int apknamehashcode = 0;
        long starttime = 0;
        int screenLockState = isScreenLocked(mContext);
        while (isListening)
        {
            try
            {
                Thread.sleep(100);
                starttime = System.currentTimeMillis();
                ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
                if (runningTaskInfos != null)
                {
                    apkname = (runningTaskInfos.get(0).topActivity).getPackageName();

                    // AppUseStatistics.ins().updateApp(apkname);

                    int sls = isScreenLocked(mContext);
                    if (screenLockState != sls)
                    {
                        screenLockState = sls;
                        DLOG.d("service", "screenLockState=" + screenLockState);
                        // EventManager.ins().sendEvent(EventTag.SCREEN_LOCK_STATE_CHANGE, screenLockState, 0, apkname);
                    }

                    if (apkname == null)
                    {
                        continue;
                    }
                    apknamehashcode = apkname.hashCode();
                    if (apknamehashcode != lastAPKNameHashCode)
                    {
                        // DLOG.d("service", "apkname:" + apkname + ":" + (System.currentTimeMillis() - starttime));
                        // // if (isDesktop(apknamehashcode))
                        // if (isDesktop(apkname))
                        // {
                        // EventManager.ins().sendEvent(EventTag.SCAN_ACTIVITY_DESKTOP, 0, 0, apkname);
                        // } else
                        // {
                        // Game game = GameManager.ins().getGameByPackageName(apkname);
                        // if (game != null)
                        // {
                        // GameManager.ins().currentGame = game;
                        // } else
                        // {
                        // GameManager.ins().currentGame = null;
                        // }
                        // EventManager.ins().sendEvent(EventTag.SCAN_ACTIVITY_APP, 0, 0, apkname);
                        // }
                        lastAPKNameHashCode = apknamehashcode;
                    }
                }
            } catch (Exception ex)
            {
                Log.d("service", ex.toString());
            }
        }
    }

    public void stopScan()
    {
        this.isListening = false;
    }

    // private static HashMap<Integer, Integer> DeskTopTable = new HashMap<Integer, Integer>();
    // static
    // {
    // DeskTopTable.put("com.miui.home".hashCode(), 0);
    // }
    //
    // private boolean isDesktop(int hashcode)
    // {
    // if (DeskTopTable.containsKey(hashcode))
    // {
    // return true;
    // } else
    // {
    // return false;
    // }
    // }

    private boolean isDesktop(String pkgname)
    {
        if (homespkgnamelist != null && homespkgnamelist.contains(pkgname))
            return true;
        else return false;
    }

    private List<String> homespkgnamelist;

    /**
     * 获得属于桌面的应用的应用包名称
     * 
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes()
    {
        if (homespkgnamelist == null)
            homespkgnamelist = new ArrayList<String>();
        try
        {
            PackageManager packageManager = mContext.getPackageManager();
            // 属性
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo ri : resolveInfo)
            {
                homespkgnamelist.add(ri.activityInfo.packageName);
                System.out.println(ri.activityInfo.packageName);
            }
        } catch (Exception e)
        {
        }
        return homespkgnamelist;
    }

    /**
     * @param c
     * @return -1异常 0解锁 1锁屏
     */
    public final static int isScreenLocked(Context c)
    {
        int ret = -1;
        try
        {
            KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(c.KEYGUARD_SERVICE);
            ret = mKeyguardManager.inKeyguardRestrictedInputMode() ? 1 : 0;
        } catch (Exception e)
        {
        }
        return ret;
    }
}
