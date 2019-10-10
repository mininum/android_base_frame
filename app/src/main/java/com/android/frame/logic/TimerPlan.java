package com.android.frame.logic;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.rdengine.log.DLOG;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 定时器 计划任务 驱动执行，指定固定时间的任务 可以添加在这执行
 * 
 * @author CCCMAX
 */
public class TimerPlan
{
    public static final String TAG = "TimerPlan";

    private volatile static TimerPlan mself;

    public static TimerPlan ins()
    {
        if (mself == null)
            synchronized (TimerPlan.class)
            {
                if (mself == null)
                    mself = new TimerPlan();
            }
        return mself;
    }

    /** 线程安全的 时间计划表 */
    private ConcurrentHashMap<TimeRule, ArrayList<TPItem>> planMap = null;

    private LinkedList<TPItem> distributePool = null;

    private Object lock = new Object();

    private TimerPlan()
    {
        planMap = new ConcurrentHashMap<TimeRule, ArrayList<TPItem>>();
        distributePool = new LinkedList<TPItem>();

        synchronized (DistributeThread)
        {
            if (!DistributeThread.isAlive())
            {
                DistributeThread.start();
            }
        }
    }

    private Thread DistributeThread = new Thread()
    {
        public void run()
        {
            try
            {
                while (true)
                {
                    TPItem item = null;
                    while (!distributePool.isEmpty())
                    {
                        synchronized (distributePool)
                        {
                            item = distributePool.getFirst();
                            distributePool.removeFirst();
                        }

                        try
                        {
                            item.listener.onTime(item.timeRule);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    synchronized (lock)
                    {
                        lock.wait();
                    }
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    };

    private static Handler MainHandler = new Handler(Looper.getMainLooper())
    {
        public void handleMessage(Message msg)
        {
            if (mself == null || msg == null || msg.obj == null)
                return;
            TimeRule tr = (TimeRule) msg.obj;
            mself.executePlan(tr);
        }
    };

    /** 时间改变时（每分钟） 调用到这里 ， 接收到调用时 一定是进程启动的情况下 */
    public static void notifyTimeTick()
    {
        if (mself == null)
            return;

        long currenttime = System.currentTimeMillis();// 当前时间
        Iterator iter = mself.planMap.entrySet().iterator();
        while (iter.hasNext())
        {
            // 遍历所有注册的时间
            try
            {
                Map.Entry entry = (Map.Entry) iter.next();
                TimeRule tr = (TimeRule) entry.getKey();
                {
                    if (tr.isTimeMatching(currenttime))
                    {
                        // 时间匹配
                        Message msg = new Message();
                        msg.obj = tr;
                        MainHandler.sendMessage(msg);
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * 注册时间计划
     *
     * @param tr
     *            某个时间计划
     * @param listener
     *            回调，其中如引用页面view对象需要注意引用方式和注销计划
     * @return
     */
    public boolean registListener(TimeRule tr, TimerPlanListener listener)
    {
        return registListener(tr, listener, true);
    }

    /**
     * 注册时间计划
     *
     * @param tr
     *            某个时间计划
     * @param listener
     *            回调，其中如引用页面view对象需要注意引用方式和注销计划
     * @param callmainthread
     *            是否在主线程回调
     * @return
     */
    public boolean registListener(TimeRule tr, TimerPlanListener listener, boolean callmainthread)
    {
        if (tr == null || listener == null)
            return false;

        TPItem item = new TPItem(tr, listener, callmainthread);

        ArrayList<TPItem> list = planMap.get(tr);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (item.isSameListener(list.get(i)))
                {
                    return false;
                }
            }

            list.add(item);
        } else
        {
            list = new ArrayList<TPItem>();
            list.add(item);
            planMap.put(tr, list);
        }

        return true;
    }

    /**
     * 注销某种时间的全部计划
     */
    public void reomoveTimeRule(TimeRule tr)
    {
        planMap.remove(tr);
    }

    /**
     * 注销指定时间计划
     *
     * @param tr
     * @param listener
     */
    public void removeListener(TimeRule tr, TimerPlanListener listener)
    {
        ArrayList<TPItem> list = planMap.get(tr);
        if (list != null)
        {
            TPItem item = null;
            for (int i = 0; i < list.size(); i++)
            {
                item = list.get(i);
                if (item.timeRule == tr && item.listener == listener)
                {
                    list.remove(i);
                    return;
                }
            }
        }
    }

    private void executePlan(TimeRule tr)
    {
        ArrayList<TPItem> itemlist = planMap.get(tr);

        if (tr.loopmode == TimeRule.LoopMode.EVERYDAY)
        {
            // 每天循环
            tr.makeNextTimeTick();
        } else
        {
            // 只执行一次的 自动注销
            reomoveTimeRule(tr);
        }

        DLOG.e("cccmax", "planMap.size=" + planMap.size());

        if (itemlist != null)
        {
            for (TPItem item : itemlist)
            {
                if (item.callMainThread)
                {
                    // 主线程
                    item.listener.onTime(item.timeRule);
                } else
                {
                    // 非主线程
                    TPItem tempitem = new TPItem(item.timeRule, item.listener, item.callMainThread);
                    synchronized (distributePool)
                    {
                        distributePool.add(tempitem);
                    }
                }
            }

            if (!distributePool.isEmpty())
            {
                try
                {
                    synchronized (lock)
                    {
                        lock.notify();
                    }

                } catch (Exception ex)
                {
                    DLOG.d(TAG, "notify error");
                }
            }
        }

    }

    /**
     * 时间规则
     * 
     * @author CCCMAX
     */
    public static class TimeRule
    {
        public enum LoopMode
        {
            /** 只执行一次 */
            ONCE,
            /** 每天 */
            EVERYDAY,
        };

        /** 循环模式 默认一次 */
        public LoopMode loopmode = LoopMode.ONCE;

        /** 小时 */
        public int hour = 0;
        /** 分钟 */
        public int minute = 0;

        /** 下一次 检测时间 单位毫秒 */
        public AtomicLong nextTimeTick = new AtomicLong(-1);

        public final int key;

        /**
         * 时间规则 几点几分 每天循环或者单次
         * 
         * @param hour
         *            几点 0-23
         * @param minute
         *            几分 0-59
         * @param loopmode
         *            循环模式
         */
        public TimeRule(int hour, int minute, LoopMode loopmode)
        {
            if (hour < 0)
                hour = 0;
            if (hour > 23)
                hour = 23;

            if (minute < 0)
                minute = 0;
            if (minute > 59)
                minute = 59;

            if (loopmode == null)
                loopmode = LoopMode.ONCE;

            this.hour = hour;
            this.minute = minute;
            this.loopmode = loopmode;

            makeNextTimeTick();

            key = ("" + hour + minute + loopmode.name()).hashCode();
            DLOG.e("cccmax", "new TimeRule = " + toString());
        }

        private long makeNextTimeTick()
        {
            long currenttime = System.currentTimeMillis();// 当前时间
            long time = -1;
            long ntt = nextTimeTick.get();
            if (ntt <= 0)
            {
                Calendar ca = Calendar.getInstance();
                ca.setTimeInMillis(currenttime);
                ca.set(Calendar.HOUR_OF_DAY, hour);// 小时
                ca.set(Calendar.MINUTE, minute);// 分钟
                ca.set(Calendar.SECOND, 0);// 0秒
                ca.set(Calendar.MILLISECOND, 0);// 0毫秒
                time = ca.getTimeInMillis();

                // 已经过期了
                if ((time - currenttime) / 60 <= 0)
                {
                    time += 86400000;// 加一天的毫秒数
                }
                ntt = time;
            } else if ((ntt - currenttime) / 60 <= 0)
            {
                Calendar ca = Calendar.getInstance();
                ca.setTimeInMillis(currenttime);
                ca.set(Calendar.HOUR_OF_DAY, hour);// 小时
                ca.set(Calendar.MINUTE, minute);// 分钟
                ca.set(Calendar.SECOND, 0);// 0秒
                ca.set(Calendar.MILLISECOND, 0);// 0毫秒
                time = ca.getTimeInMillis();
                time += 86400000;// 加一天的毫秒数
                ntt = time;
            }

            nextTimeTick.set(ntt);
            return ntt;
        }

        public boolean isTimeMatching()
        {
            long currenttime = System.currentTimeMillis();// 当前时间
            return isTimeMatching(currenttime);
        }

        /**
         * 时间是否匹配
         * 
         * @return
         */
        public boolean isTimeMatching(long currenttime)
        {
            if ((nextTimeTick.get() - currenttime) / 60 == 0)
            {
                return true;
            }
            return false;
        }

        public final boolean equals(Object other)
        {
            if (other != null && other instanceof TimeRule)
            {
                TimeRule tp = (TimeRule) other;
                // if (this.hour == tp.hour && this.minute == tp.minute && this.loopmode == tp.loopmode)
                // {
                // return true;
                // }
                if (key == tp.key)
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            // return super.hashCode();
            return key;
        }

        public String toString()
        {
            return "key=" + key + " " + hour + ":" + minute + "," + loopmode.name();
        }
    }

    public static interface TimerPlanListener
    {
        public void onTime(TimeRule timerule);
    }

    private class TPItem
    {
        private TimeRule timeRule = null;
        private TimerPlanListener listener = null;
        private boolean callMainThread = false;

        public TPItem(TimeRule timeRule, TimerPlanListener listener, boolean callMainThread)
        {
            this.timeRule = timeRule;
            this.listener = listener;
            this.callMainThread = callMainThread;
        }

        public boolean isSameListener(TPItem item)
        {
            if (item != null && timeRule.equals(item) && listener == item.listener)
            {
                return true;
            } else
            {
                return false;
            }
        }
    }
}
