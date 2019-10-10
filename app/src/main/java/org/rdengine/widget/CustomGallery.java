package org.rdengine.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomGallery extends Gallery
{

    private final static int TIME = 5 * 1000;

    private boolean down = false;
    private float mLastMotionY;
    private float mLastMotionX;

    public CustomGallery(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setUnselectedAlpha(1f);
        setAnimationCacheEnabled(false);
        setDrawingCacheEnabled(false);
        setChildrenDrawingCacheEnabled(false);
        setAlwaysDrawnWithCacheEnabled(false);
        setFadingEdgeLength(0);
        setHorizontalFadingEdgeEnabled(false);
        setStaticTransformationsEnabled(false);
        setSoundEffectsEnabled(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        final float y = ev.getY();
        final float x = ev.getX();
        switch (ev.getAction())
        {
        case MotionEvent.ACTION_DOWN :
            getParent().requestDisallowInterceptTouchEvent(true);
            down = true;
            mLastMotionY = y;
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE :
            if (down)
            {
                int dey = (int) Math.abs(y - mLastMotionY);
                int dex = (int) Math.abs(x - mLastMotionX);
                if (dey > 0 && dey > dex)
                {
                    down = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL :
        case MotionEvent.ACTION_UP :
            getParent().requestDisallowInterceptTouchEvent(false);
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void next()
    {

        if (getRelativeTop() + getMeasuredHeight() <= 5 * getResources().getDisplayMetrics().density)
        {
            // removeLoop();
            return;
        }

        mHandler.removeMessages(0, this);
        Message msg = mHandler.obtainMessage(0, this);
        mHandler.sendMessageDelayed(msg, 200);
    }

    int getRelativeTop()
    {
        int top = 0;
        View current = this;
        // do
        // {
        //
        // } while ();

        while (!(current instanceof ListView))
        {
            if (current == null)
            {
                return 0 - getMeasuredHeight();
            }
            top += current.getTop();
            if (current.getParent() instanceof View)
            {
                current = (View) current.getParent();
            } else
            {
                return top;
            }

        }

        return top;
    }

    private static Handler mHandler = new Handler()
    {

        public void handleMessage(Message msg)
        {

            CustomGallery gallery = (CustomGallery) msg.obj;

            gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
        };
    };

    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        switch (action)
        {
        case MotionEvent.ACTION_DOWN :
            removeLoop();
        case MotionEvent.ACTION_MOVE :
            break;
        default:
            startLoop();
            break;
        }
        try
        {
            return super.onTouchEvent(event);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    };

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        // TODO Auto-generated method stub
        int keyCode;
        mHandler.removeMessages(0, this);
        if (isScrollingLeft(e1, e2))
        {
            keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
        } else
        {
            keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        onKeyDown(keyCode, null);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub
        super.onKeyDown(keyCode, event);
        return true;
    }

    private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2)
    {
        if (e2 == null || e1 == null)
        {
            return false;
        }
        return e2.getX() > e1.getX();
    }

    public void setAdapter(SpinnerAdapter adapter)
    {
        // TODO Auto-generated method stub
        flag = true;
        super.setAdapter(adapter);
        if (adapter.getCount() > 0)
        {
            startLoop();
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        // TODO Auto-generated method stub
        super.onDetachedFromWindow();
        removeLoop();
    }

    private boolean flag = true;

    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        if (!changed && !flag)
        {
            return;
        }
        flag = false;

        super.onLayout(changed, l, t, r, b);
    }

    private ScheduledExecutorService service;

    private boolean removed = false;

    public void startLoop()
    {
        removed = false;
        if (service == null)
        {
            service = Executors.newSingleThreadScheduledExecutor();
            Runnable task = new Runnable()
            {

                @Override
                public void run()
                {
                    if (!removed)
                    {
                        CustomGallery.this.next();
                    }
                }
            };

            service.scheduleAtFixedRate(task, TIME, TIME, TimeUnit.MILLISECONDS);
        }

    }

    public void removeLoop()
    {
        removed = true;

        if (service != null)
        {
            service.shutdownNow();
            service = null;
        }

    }

}
