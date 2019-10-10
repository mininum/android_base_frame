package org.rdengine.runtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.android.frame.logic.TimerPlan;

import org.rdengine.log.DLOG;
import org.rdengine.net.Network;
import org.rdengine.net.Network.NetworkMode;
import org.rdengine.runtime.event.EventManager;
import org.rdengine.runtime.event.EventTag;

public class RTReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent == null)
        {
            return;
        }

        try
        {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                Network.netWorkState(context);
                final NetworkMode mode = Network.getNetworkState();
                if (mode == NetworkMode.NET_WORK_OK)
                {

                } else
                {

                }
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()))
            {
                DLOG.d("RTReceiver", "screen is on...");
                EventManager.ins().sendEvent(EventTag.APP_SCREEN_POWER_CHANGE, 1, 0, null);
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()))
            {
                DLOG.d("RTReceiver", "`screen is off...");
                EventManager.ins().sendEvent(EventTag.APP_SCREEN_POWER_CHANGE, 0, 0, null);
            } else if (Intent.ACTION_TIME_TICK.equals(intent.getAction()))
            {
                // 时间 每分钟 接收一次 驱动定时计划
                DLOG.d("RTReceiver", "ACTION_TIME_TICK");
                TimerPlan.notifyTimeTick();
            }
        } catch (Throwable ex)
        {
            ex.printStackTrace();
        }
    }
}
