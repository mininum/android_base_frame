package org.rdengine.widget.viewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.rdengine.log.DLOG;

import java.util.ArrayList;

public class RDViewPager extends ViewPager
{

    private boolean down = false;
    private float mLastMotionX;
    String TAG = "RDViewPager";

    private float firstDownX;
    private float firstDownY;
    private boolean flag = false;

    public RDViewPager(Context context)
    {
        super(context);
        super.setOnPageChangeListener(_onpagechangelistener);
    }

    public RDViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        super.setOnPageChangeListener(_onpagechangelistener);
    }

    private boolean isScrollable = true;

    public void setScrollable(boolean scrollable)
    {
        this.isScrollable = scrollable;
    }

    // public void setParentSwipeView(ViewGroup mParentSwipeView)
    // {
    // this.mParentSwipeView = mParentSwipeView;
    // }

    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (getParent() == null)
        {
            return super.dispatchTouchEvent(ev);
        }
        if (getAdapter() == null || getAdapter().getCount() <= 0)
        {
            getParent().requestDisallowInterceptTouchEvent(false);
            return super.dispatchTouchEvent(ev);
        }
        final float x = ev.getX();
        switch (ev.getAction())
        {
        case MotionEvent.ACTION_DOWN :
            getParent().requestDisallowInterceptTouchEvent(true);
            down = true;
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_MOVE :
            if (down)
            {
                if (x - mLastMotionX > 5 && getCurrentItem() == 0)
                {
                    down = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                }

                if (x - mLastMotionX < -5 && getCurrentItem() == getAdapter().getCount() - 1)
                {
                    down = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            break;
        case MotionEvent.ACTION_UP :
        case MotionEvent.ACTION_CANCEL :
            getParent().requestDisallowInterceptTouchEvent(false);
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0)
    {
        if (!isScrollable)
        {
            return false;
        }
        try
        {
            return super.onInterceptTouchEvent(arg0);

        } catch (Throwable e)
        {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0)
    {
        if (!isScrollable)
        {
            return false;
        }
        return super.onTouchEvent(arg0);
    }

    @Override
    public void setAdapter(PagerAdapter arg0)
    {
        super.setAdapter(arg0);
    }

    OnPageChangeListener userlistener;

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener)
    {
        userlistener = listener;
    }

    OnPageChangeListener _onpagechangelistener = new OnPageChangeListener()
    {
        int old_position = 0;
        int state = 0;
        int to_position = -1;
        ArrayList<Integer> positionlist = new ArrayList<Integer>();

        int xx = getCurrentItem();

        public void onPageSelected(int position)
        {
            if (userlistener != null)
                userlistener.onPageSelected(position);
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            // DLOG.d(TAG, "onPageScrolled position=" + position + " positionOffset=" + positionOffset
            // + " positionOffsetPixels=" + positionOffsetPixels);
            try
            {
                switch (state)
                {
                case 0 :
                    old_position = getCurrentItem();
                    break;
                case 1 :
                    if (to_position != position)
                    {
                        to_position = position;
                        if (position < old_position && positionOffset != 0)
                        {
                            // 向左
                            if (!positionlist.contains(position))
                            {
                                positionlist.add(position);
                                DLOG.d(TAG, "向左滑动 " + (position + 1) + ">" + position);
                                if (getAdapter() != null && getAdapter() instanceof RDPagerAdapter)
                                {
                                    RDPagerAdapter mfadapter = (RDPagerAdapter) getAdapter();
                                    if (mfadapter.getItem(position) != null)
                                    {
                                        mfadapter.getItem(position).onLoadResource();
                                        mfadapter.getItem(position).onShow();
                                    }
                                }
                            }
                        } else if (position == old_position && positionOffset != 0)
                        {
                            // 向右
                            if (!positionlist.contains(position + 1))
                            {
                                positionlist.add(position + 1);
                                DLOG.d(TAG, "向右滑动 " + position + ">" + (position + 1));
                                if (getAdapter() != null && getAdapter() instanceof RDPagerAdapter)
                                {
                                    RDPagerAdapter mfadapter = (RDPagerAdapter) getAdapter();
                                    if (mfadapter.getItem(position + 1) != null)
                                    {
                                        mfadapter.getItem(position + 1).onLoadResource();
                                        mfadapter.getItem(position + 1).onShow();
                                    }
                                }
                            }
                        }
                    }
                    break;
                case 2 :
                    break;
                }
            } catch (Exception e)
            {
            }

            if (userlistener != null)
                userlistener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        /**
         * public static final int SCROLL_STATE_IDLE = 0; //空闲的<br>
         * public static final int SCROLL_STATE_DRAGGING = 1;//正在拖动<br>
         * public static final int SCROLL_STATE_SETTLING = 2;//
         */
        public void onPageScrollStateChanged(int state)
        {
            DLOG.d(TAG, "onPageScrollStateChanged  state=" + state);

            this.state = state;
            old_position = getCurrentItem();
            if (state == 0)
            {
                to_position = -1;

                if (xx == old_position)
                {
                    if (getAdapter() != null && getAdapter() instanceof RDPagerAdapter)
                    {
                        RDPagerAdapter mfadapter = (RDPagerAdapter) getAdapter();
                        for (int i = 0; i < positionlist.size(); i++)
                        {
                            if (mfadapter.getItem(positionlist.get(i)) != null)
                                mfadapter.getItem(positionlist.get(i)).onHide();
                        }
                    }
                }
                xx = old_position;

                positionlist = new ArrayList<Integer>();
            }

            if (userlistener != null)
                userlistener.onPageScrollStateChanged(state);
        }
    };

}
