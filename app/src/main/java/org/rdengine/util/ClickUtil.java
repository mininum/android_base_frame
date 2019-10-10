package org.rdengine.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

/**
 * 防止用户多次连续暴力点击
 */

public class ClickUtil
{
    private static long lastClickTime;

    public static boolean isFastDoubleClick()
    {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 <= timeD && timeD < 300)
        {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static boolean isFastDoubleClick(long t)
    {
        if (t <= 0)
            t = 100;
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 <= timeD && timeD < t)
        {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void reset()
    {
        lastClickTime = 0;
    }

    /**
     * 单机双击事件
     */
    public static abstract class DoubleClickListener implements View.OnClickListener
    {

        long mLastTime = 0;
        long mCurTime = 0;

        private Handler handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                switch (msg.what)
                {
                case 1 :
                    // "这是单击事件"
                    singleClick();
                    break;
                case 2 :
                    // "这是双击事件"
                    doubleClick();
                    break;
                }
            }
        };

        @Override
        public void onClick(View v)
        {
            mLastTime = mCurTime;
            mCurTime = System.currentTimeMillis();
            if (mCurTime - mLastTime < 200)
            {// 双击事件
                mCurTime = 0;
                mLastTime = 0;
                handler.removeMessages(1);
                handler.sendEmptyMessage(2);
            } else
            {// 单击事件
                handler.sendEmptyMessageDelayed(1, 210);
            }
        }

        public abstract void singleClick();

        public abstract void doubleClick();

    }
}
